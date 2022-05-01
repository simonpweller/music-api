package de.sweller.musicservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    val musicBrainzClient: MusicBrainzClient,
    val wikidataClient: WikidataClient,
    val wikipediaClient: WikipediaClient,
) {
    @GetMapping("/musify/music-artist/details/{mbid}")
    suspend fun getArtist(@PathVariable mbid: String): DetailsResponse {
        val musicBrainzResponse = musicBrainzClient.getArtistInfo(mbid)
        val siteLinkTitle = wikidataClient.getSiteLinkTitle(musicBrainzResponse.wikidataEntityId)
        val description = siteLinkTitle?.let { wikipediaClient.getDescription(siteLinkTitle) }
        return DetailsResponse.of(musicBrainzResponse, description)
    }
}

data class DetailsResponse(
    val mbid: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
    val description: String?,
) {
    companion object {
        fun of(musicBrainzResponse: MusicBrainzResponse, description: String?): DetailsResponse {
            return DetailsResponse(
                musicBrainzResponse.id,
                musicBrainzResponse.name,
                musicBrainzResponse.gender,
                musicBrainzResponse.country,
                musicBrainzResponse.disambiguation,
                description
            )
        }
    }
}
