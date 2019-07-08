/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.engine.WebSocketController.websocketEnabled
import fr.vsct.tock.bot.engine.config.UploadedFilesService
import fr.vsct.tock.bot.engine.nlp.NlpProxyBotService
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.listProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.security.auth.TockAuthProvider
import fr.vsct.tock.shared.security.initEncryptor
import fr.vsct.tock.shared.vertx.WebVerticle
import fr.vsct.tock.translator.Translator.initTranslator
import io.vertx.core.Future
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 *
 */
internal class BotVerticle(
    private val nlpProxyOnBot: Boolean = booleanProperty("tock_nlp_proxy_on_bot", false),
    private val serveUploadedFiles: Boolean = booleanProperty("tock_bot_serve_files", true)
) : WebVerticle() {

    inner class ServiceInstaller(
        val serviceId: String,
        private val installer: (Router) -> Unit,
        var routes: MutableList<Route> = CopyOnWriteArrayList(),
        @Volatile
        var installed: Boolean = false,
        val registrationDate: Instant = Instant.now()
    ) {

        fun install() {
            if (!installed) {
                installed = true
                try {
                    logger.debug("install $serviceId")
                    val registeredRoutes = router.routes
                    installer.invoke(router)
                    routes.addAll(router.routes.subtract(registeredRoutes))
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        fun uninstall() {
            routes.forEach { it.remove() }
        }
    }

    override val logger: KLogger = KotlinLogging.logger {}

    private val handlers: MutableMap<String, ServiceInstaller> = ConcurrentHashMap()
    private val secondaryInstallers: MutableSet<ServiceInstaller> = CopyOnWriteArraySet()
    private var initialized: Boolean = false

    override fun authProvider(): TockAuthProvider? = defaultAuthProvider()

    fun registerServices(serviceIdentifier: String, installer: (Router) -> Unit): ServiceInstaller {
        return ServiceInstaller(serviceIdentifier, installer).also {
            if (!handlers.containsKey(serviceIdentifier)) {
                handlers[serviceIdentifier] = it
            } else {
                logger.debug("service $serviceIdentifier already registered - skip it for now")
                secondaryInstallers.add(it)
            }
        }
    }

    fun unregisterServices(installer: ServiceInstaller) {
        if (secondaryInstallers.contains(installer)) {
            secondaryInstallers.remove(installer)
        }
        if (handlers[installer.serviceId] == installer) {
            handlers.remove(installer.serviceId)
                ?.also {
                    val s = secondaryInstallers.find {
                        it.serviceId == installer.serviceId
                    }

                    logger.debug { "remove service ${it.serviceId}" }
                    it.uninstall()
                    if (s != null) {
                        s.install()
                        secondaryInstallers.remove(s)
                        handlers[it.serviceId] = s
                    }
                    return
                }
        }
    }

    override fun protectedPaths(): Set<String> {
        //TODO remove deprecated tock_bot_protected_path property
        val path = property("tock_bot_protected_path", "/admin")
        val paths = listProperty("tock_bot_protected_paths", listOf("/admin"))

        return (paths + path).map { it.trim() }.toSet()
    }

    @Synchronized
    override fun configure() {
        if (!initialized) {
            initialized = true
            initEncryptor()
            initTranslator()
            if (nlpProxyOnBot) {
                registerServices("nlp_proxy_bot", NlpProxyBotService.configure(vertx))
            }
            if (serveUploadedFiles) {
                registerServices("serve_files", UploadedFilesService.configure())
            }
        }

        install()
    }

    private fun install() {
        if (handlers.any { !it.value.installed }) {
            logger.info { "Install Bot Services / ${handlers.size} registered" }
            //sort installers by registration date to keep registration order
            handlers.values.sortedBy { it.registrationDate }.forEach {
                it.install()
            }
        }
    }

    override fun healthcheck(): (RoutingContext) -> Unit {
        return BotRepository.healthcheckHandler
    }

    override fun startServer(startFuture: Future<Void>, port: Int) {
        if (websocketEnabled) {
            logger.info { "Install WebSocket handler" }
            server.websocketHandler { context ->
                val key = context.path().let { if (it.startsWith("/")) it.substring(1) else null }

                if (WebSocketController.isAuthorizedKey(key)) {
                    logger.info { "Install WebSocket push handler for ${context.path()}" }
                    WebSocketController.setPushHandler(key!!) { context.writeTextMessage(it) }

                    context.textMessageHandler { json ->
                        WebSocketController.getReceiveHandler(key)?.invoke(json)
                    }.closeHandler {
                        WebSocketController.removePushHandler(key)
                    }
                } else {
                    logger.warn { "unknown key: $key" }
                    context.reject()
                }
            }
        }
        super.startServer(startFuture, port)

    }
}