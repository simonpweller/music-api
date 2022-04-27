package de.sweller.musicservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class Controller(
    val musicBrainzClient: MusicBrainzClient,
    val wikidataClient: WikidataClient,
) {
    @GetMapping("/musify/music-artist/details/{mbid}")
    fun getArtist(@PathVariable mbid: String): Mono<DetailsResponse> {
        return musicBrainzClient
            .getArtistInfo(mbid)
            .flatMap { musicBrainzResponse ->
                wikidataClient.getSiteLinkTitle(musicBrainzResponse.wikidataEntityId)
                    .map {
                        DetailsResponse.of(musicBrainzResponse, it)
                    }
            }
    }
}

data class DetailsResponse(
    val mbid: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
    val wikiTitle: String?,
) {
    companion object {
        fun of(musicBrainzResponse: MusicBrainzResponse, siteLinkTitle: String?): DetailsResponse {
            return DetailsResponse(
                musicBrainzResponse.id,
                musicBrainzResponse.name,
                musicBrainzResponse.gender,
                musicBrainzResponse.country,
                musicBrainzResponse.disambiguation,
                siteLinkTitle
            )
        }
    }
}
