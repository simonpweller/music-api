package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WikipediaClient(
    val webClient: WebClient,
    @Value("\${app.wikipedia.baseurl}") val baseUrl: String,
) {
    fun getDescription(title: String): Mono<String> {
        return webClient
            .get()
            .uri("${baseUrl}/page/summary/${title.replace(" ", "_")}")
            .retrieve()
            .bodyToMono(WikipediaResponse::class.java)
            .map { it.extract }
    }
}

data class WikipediaResponse(
    val extract: String
)
