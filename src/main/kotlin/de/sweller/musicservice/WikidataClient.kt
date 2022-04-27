package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WikidataClient(
    val webClient: WebClient,
    @Value("\${app.wikidata.baseurl}") val baseUrl: String,
) {
    fun getSiteLinkTitle(wikiId: String): Mono<String?> {
        return webClient
            .get()
            .uri("${baseUrl}/Special:EntityData/${wikiId}.json")
            .retrieve()
            .bodyToMono(WikidataResponse::class.java)
            .mapNotNull { it.entities[wikiId]?.sitelinks?.get("enwiki")?.title?.substringAfter("/") }
    }
}

data class WikidataResponse(
    val entities: Map<String, WikidataEntity>
)

data class WikidataEntity(
    val sitelinks: Map<String, WikidataSiteLink>
)

data class WikidataSiteLink(
    val title: String
)
