package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class MusicBrainzClient(
    private val webClient: WebClient,
    @Value("\${app.music-brainz.baseurl}") val baseUrl: String,
) {
    suspend fun getArtistInfo(mbid: String): MusicBrainzResponse {
        return webClient.get()
            .uri("${baseUrl}/artist/${mbid}?fmt=json&inc=url-rels+release-groups")
            .retrieve()
            .awaitBody()

    }
}

data class MusicBrainzResponse(
    val id: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
    val relations: List<MusicBrainzRelation>
) {
    val wikidataEntityId: String
        get() = relations.first { it.type == "wikidata" }.url.resource.substringAfterLast("/")
}

data class MusicBrainzRelation(
    val type: String,
    val url: MusicBrainzRelationUrl
)

data class MusicBrainzRelationUrl(
    val resource: String,
    val id: String,
)
