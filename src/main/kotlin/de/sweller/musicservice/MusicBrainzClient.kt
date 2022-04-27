package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class MusicBrainzClient(
    private val webClient: WebClient,
    @Value("\${app.music-brainz.baseurl}") val baseUrl: String,
) {
    fun getArtistInfo(mbid: String): Mono<MusicBrainzResponse> {
        return webClient.get()
            .uri("${baseUrl}/artist/${mbid}?fmt=json")
            .retrieve()
            .bodyToMono(MusicBrainzResponse::class.java)
    }
}

data class MusicBrainzResponse(
    val id: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
)