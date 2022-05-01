package de.sweller.musicservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class CoverArtArchiveClient(
    private val webClient: WebClient,
    @Value("\${app.cover-art-archive.baseurl}") val baseUrl: String,
) {
    suspend fun getImageUrl(mbid: String): String? {
        return webClient.get()
            .uri("${baseUrl}/release-group/${mbid}")
            .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
            .retrieve()
            .awaitBody<CoverArtArchiveResponse>()
            .images
            .firstOrNull { it.front }
            ?.image
    }
}

data class CoverArtArchiveResponse(
    val images: List<CoverArtArchiveImage>
) {
    data class CoverArtArchiveImage(
        val front: Boolean,
        val image: String,
    )
}
