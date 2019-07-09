package fr.vsct.tock.bot.admin.bundled

import com.github.salomonbrys.kodein.Kodein
import fr.vsct.tock.bot.BotIoc
import fr.vsct.tock.bot.admin.BotAdminVerticle
import fr.vsct.tock.nlp.build.BuildModelWorkerVerticle
import fr.vsct.tock.nlp.build.CleanupModelWorkerVerticle
import fr.vsct.tock.nlp.build.HealthCheckVerticle
import fr.vsct.tock.nlp.front.ioc.FrontIoc
import fr.vsct.tock.shared.vertx.vertx
import io.vertx.core.DeploymentOptions

fun main() {
    startAdminServerWithBuildWorker()
}

fun startAdminServerWithBuildWorker(vararg modules: Kodein.Module) {
    //setup ioc
    FrontIoc.setup(BotIoc.coreModules + modules.toList())
    //deploy verticle
    vertx.deployVerticle(BotAdminVerticle())

    val buildModelWorkerVerticle = BuildModelWorkerVerticle()
    vertx.deployVerticle(buildModelWorkerVerticle, DeploymentOptions().setWorker(true))
    vertx.deployVerticle(CleanupModelWorkerVerticle(), DeploymentOptions().setWorker(true))
    vertx.deployVerticle(HealthCheckVerticle(buildModelWorkerVerticle))
}
