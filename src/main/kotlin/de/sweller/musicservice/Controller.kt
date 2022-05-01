package de.sweller.musicservice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    val musicBrainzClient: MusicBrainzClient,
    val wikidataClient: WikidataClient,
    val wikipediaClient: WikipediaClient,
    val coverArtArchiveClient: CoverArtArchiveClient,
) {
    @GetMapping("/musify/music-artist/details/{mbid}")
    suspend fun getArtist(@PathVariable mbid: String): DetailsResponse {
        val musicBrainzResponse = musicBrainzClient.getArtistInfo(mbid)
        return withContext(Dispatchers.IO) {
            val description = async { getDescription(musicBrainzResponse) }
            val albumCoverMap = async { getAlbumCoverMap(musicBrainzResponse) }
            DetailsResponse.of(musicBrainzResponse, description.await(), albumCoverMap.await())
        }
    }

    private suspend fun getDescription(musicBrainzResponse: MusicBrainzResponse): String? {
        val siteLinkTitle = wikidataClient.getSiteLinkTitle(musicBrainzResponse.wikidataEntityId)
        return siteLinkTitle?.let { wikipediaClient.getDescription(siteLinkTitle) }
    }

    private suspend fun getAlbumCoverMap(musicBrainzResponse: MusicBrainzResponse): Map<String, String?> {
        return withContext(Dispatchers.IO) {
            musicBrainzResponse.releaseGroups.map {
                async {
                    it.id to coverArtArchiveClient.getImageUrl(it.id)
                }
            }.awaitAll().toMap()
        }
    }
}

data class DetailsResponse(
    val mbid: String,
    val name: String,
    val gender: String?,
    val country: String?,
    val disambiguation: String?,
    val albums: List<Album>,
    val description: String?,
) {

    data class Album(
        val id: String,
        val title: String,
        val imageUrl: String?,
    )

    companion object {
        fun of(musicBrainzResponse: MusicBrainzResponse, description: String?, albumCoverMap: Map<String, String?>): DetailsResponse {
            return DetailsResponse(
                musicBrainzResponse.id,
                musicBrainzResponse.name,
                musicBrainzResponse.gender,
                musicBrainzResponse.country,
                musicBrainzResponse.disambiguation,
                musicBrainzResponse.releaseGroups.map { Album(it.id, it.title, albumCoverMap[it.id]) },
                description,
            )
        }
    }
}
