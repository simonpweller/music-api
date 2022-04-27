package de.sweller.musicservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class Controller(
    val client: MusicBrainzClient
) {
    @GetMapping("/musify/music-artist/details/{mbid}")
    fun getArtist(@PathVariable mbid: String): Mono<DetailsResponse> {
        return client.getArtistInfo(mbid).map { DetailsResponse.of(it) }
    }
}

data class DetailsResponse(
    val mbid: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
) {
    companion object {
        fun of(musicBrainzResponse: MusicBrainzResponse): DetailsResponse {
            return DetailsResponse(
                musicBrainzResponse.id,
                musicBrainzResponse.name,
                musicBrainzResponse.gender,
                musicBrainzResponse.country,
                musicBrainzResponse.disambiguation,
            )
        }
    }
}
