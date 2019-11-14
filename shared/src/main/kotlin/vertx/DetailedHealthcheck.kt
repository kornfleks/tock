package ai.tock.shared.vertx

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.ext.web.RoutingContext

private data class DetailedHealthcheckResult(
    val id: String,
    val status: Int
)

private data class DetailedHealthcheckResults(
    val results: List<DetailedHealthcheckResult>
)

fun makeDetailedHealthcheck(
    tasks: List<Pair<String, () -> Boolean>> = listOf(),
    selfCheck: () -> Boolean = { true }
) : (RoutingContext) -> Unit {
    val mapper = jacksonObjectMapper()

    return {
        val response = it.response()
        val results = mutableListOf<DetailedHealthcheckResult>()
        try {
            selfCheck()
            for (task in tasks) {
                val result = DetailedHealthcheckResult(
                    task.first,
                    if (task.second.invoke()) 200 else 503
                )
                results.add(result)
            }
            response
                .setStatusCode(207)
                .end(mapper.writeValueAsString(DetailedHealthcheckResults(results)))
        } catch (e: Exception) {
            response.setStatusCode(503).end()
        }
    }
}