package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WikipediaClient(
    val webClient: WebClient,
    @Value("\${app.wikipedia.baseurl}") val baseUrl: String,
) {
    suspend fun getDescription(title: String): String {
        return webClient
            .get()
            .uri("${baseUrl}/page/summary/${title.replace(" ", "_")}")
            .retrieve()
            .awaitBody<WikipediaResponse>()
            .extract
    }
}

data class WikipediaResponse(
    val extract: String
)
