/*
 * Copyright (C) 2017/2019 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.api.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import fr.vsct.tock.bot.admin.bot.BotConfiguration
import fr.vsct.tock.bot.api.model.BotResponse
import fr.vsct.tock.bot.api.model.UserRequest
import fr.vsct.tock.bot.api.model.configuration.ClientConfiguration
import fr.vsct.tock.bot.api.model.message.bot.BotMessage
import fr.vsct.tock.bot.api.model.message.bot.Card
import fr.vsct.tock.bot.api.model.message.bot.CustomMessage
import fr.vsct.tock.bot.api.model.message.bot.I18nText
import fr.vsct.tock.bot.api.model.message.bot.Sentence
import fr.vsct.tock.bot.api.model.websocket.RequestData
import fr.vsct.tock.bot.api.model.websocket.ResponseData
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.media.MediaAction
import fr.vsct.tock.bot.connector.media.MediaCard
import fr.vsct.tock.bot.connector.media.MediaFile
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.WebSocketController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.config.UploadedFilesService
import fr.vsct.tock.bot.engine.message.ActionWrappedMessage
import fr.vsct.tock.bot.engine.message.MessagesList
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityType
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.translator.I18nContext
import fr.vsct.tock.translator.Translator
import mu.KotlinLogging
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

private val timeoutInSeconds: Long = longProperty("tock_api_timout_in_s", 10)

private class WSHolder(
    @Volatile
    private var response: ResponseData? = null,
    private val latch: CountDownLatch = CountDownLatch(1)) {

    fun receive(response: ResponseData) {
        this.response = response
        latch.countDown()
    }

    fun wait(): ResponseData? {
        latch.await(timeoutInSeconds, SECONDS)
        return response
    }
}

private val wsRepository: Cache<String, WSHolder> =
    CacheBuilder.newBuilder().expireAfterWrite(timeoutInSeconds + 1, SECONDS).build()


internal class BotApiHandler(
    private val provider: BotApiDefinitionProvider,
    configuration: BotConfiguration) {

    private val logger = KotlinLogging.logger {}

    private val apiKey: String = configuration.apiKey
    private val webhookUrl: String? = configuration.webhookUrl

    private val client = webhookUrl?.takeUnless { it.isBlank() }?.let {
        try {
            BotApiClient(it)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
    }

    init {
        if (WebSocketController.websocketEnabled) {
            logger.debug { "register $apiKey" }
            WebSocketController.registerAuthorizedKey(apiKey)
            WebSocketController.setReceiveHandler(apiKey) { content: String ->
                val response: ResponseData? = mapper.readValue(content)
                if (response != null) {
                    val holder = wsRepository.getIfPresent(response.requestId)
                    if (holder == null) {
                        logger.warn { "unknown request ${response.requestId}" }
                    }
                    holder?.receive(response)
                    if (response.botConfiguration != null) {
                        provider.updateIfConfigurationChange(response.botConfiguration!!)
                    }
                } else {
                    logger.warn { "null response: $content" }
                }
            }
        }
    }

    fun configuration(): ClientConfiguration? =
        client?.send(RequestData(configuration = true))?.botConfiguration
            ?: sendWithWebSocket(RequestData(configuration = true))?.botConfiguration

    fun send(bus: BotBus) {
        val request = bus.toUserRequest()
        if (client != null) {
            val response = client.send(RequestData(request))
            bus.handleResponse(request, response?.botResponse)
        } else {
            val response = sendWithWebSocket(RequestData(request))
            if (response != null) {
                bus.handleResponse(request, response.botResponse)
            } else {
                error("no webhook set and no response from websocket")
            }
        }
    }

    private fun sendWithWebSocket(request: RequestData): ResponseData? {
        val pushHandler = WebSocketController.getPushHandler(apiKey)
        return if (pushHandler != null) {
            val holder = WSHolder()
            wsRepository.put(request.requestId, holder)
            logger.debug { "send request ${request.requestId}" }
            pushHandler.invoke(mapper.writeValueAsString(request))
            holder.wait()
        } else {
            null
        }
    }

    private fun BotBus.handleResponse(request: UserRequest, response: BotResponse?) {
        if (response != null) {
            val messages = response.messages
            if (messages.isNullOrEmpty()) {
                error("no response for $request")
            }
            messages.subList(0, messages.size - 1)
                .forEach { a ->
                    send(a)
                }
            messages.last().apply {
                send(this, true)
            }
            //handle entity changes
            entities
                .entries
                //new collection
                .toList()
                .forEach { (role, entity) ->
                    val result = response.entities.find { it.role == role }
                    val value = entity.value
                    //remove not present
                    if (result == null) {
                        removeEntityValue(role)
                    } else if (value != null) {

                        if (result.content != value.content) {
                            changeEntityText(value.entity, result.content)
                        }
                        if (result.value != value.value) {
                            changeEntityValue(value.entity, result.value)
                        }
                    }
                }
            //handle entity add
            response.entities.forEach {
                if (entityValueDetails(it.role) == null) {
                    val entity = Entity(EntityType(it.type), it.role)
                    changeEntityText(entity, it.content)
                    changeEntityValue(entity, it.value)
                }
            }

            //switch story if new story
            if (response.storyId != request.storyId) {
                botDefinition.stories.find { it.id == response.storyId }
                    ?.also {
                        switchStory(it)
                    }

            }
            //set step
            if (response.step != null) {
                step = story.definition.steps.find { it.name == response.step }
            }
        }
    }

    private fun BotBus.send(message: BotMessage, end: Boolean = false) {
        val actions =
            when (message) {
                is Sentence -> listOf(toAction(message))
                is Card -> toActions(message)
                is CustomMessage -> listOf(toAction(message))
                else -> error("unsupported message $message")
            }

        if (actions.isEmpty()) {
            error("no message find in $message")
        }
        val messagesList = MessagesList(actions.map { ActionWrappedMessage(it, 0) })
        val delay = botDefinition.defaultDelay(currentAnswerIndex)
        if (end) {
            end(messagesList, delay)
        } else {
            send(messagesList, delay)
        }
    }

    private fun BotBus.toAction(message: CustomMessage): Action {
        return SendSentence(
            botId,
            applicationId,
            userId,
            null,
            mutableListOf(message.message.value as ConnectorMessage)
        )
    }

    private fun BotBus.toAction(sentence: Sentence): Action {
        val text = translateText(sentence.text)
        if (sentence.suggestions.isNotEmpty() && text != null) {
            val message = underlyingConnector.addSuggestions(text, sentence.suggestions.mapNotNull { translateText(it.title) }).invoke(this)
            if (message != null) {
                return SendSentence(
                    botId,
                    applicationId,
                    userId,
                    null,
                    mutableListOf(message)
                )
            }
        }
        return SendSentence(
            botId,
            applicationId,
            userId,
            text
        )
    }

    private fun BotBus.toActions(card: Card): List<Action> {
        val connectorMessages =
            toMediaCard(card)
                .takeIf { it.isValid() }
                ?.let {
                    underlyingConnector.toConnectorMessage(it).invoke(this)
                }

        return connectorMessages?.map {
            SendSentence(
                botId,
                applicationId,
                userId,
                null,
                mutableListOf(it)
            )
        } ?: emptyList()
    }

    private fun BotBus.toMediaCard(card: Card): MediaCard =
        MediaCard(
            translateText(card.title),
            translateText(card.subTitle),
            card.attachment?.let {
                MediaFile(
                    it.url,
                    it.url,
                    it.type?.let { AttachmentType.valueOf(it.name) } ?: UploadedFilesService.attachmentType(it.url))
            },
            card.actions.map {
                MediaAction(
                    translateText(it.title) ?: "",
                    it.url
                )
            }
        )

}

private fun BotBus.translateText(i18n: I18nText?): CharSequence? =
    when {
        i18n == null -> null
        i18n.toBeTranslated -> translate(i18n.text, i18n.args)
        else -> Translator.formatMessage(
            i18n.text,
            I18nContext(userLocale,
                userInterfaceType,
                targetConnectorType.id,
                contextId),
            i18n.args
        )
    }
