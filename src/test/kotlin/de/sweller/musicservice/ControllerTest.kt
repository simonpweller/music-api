package de.sweller.musicservice

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 8082)
internal class ControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    private val musicBrainzApiResponse: String? =
        this::class.java.classLoader.getResource("music-brainz-response.json")?.readText()

    @Test
    fun getArtist() {
        stubFor(
            get("/artist/f27ec8db-af05-4f36-916e-3d57f91ecf5e?fmt=json").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(musicBrainzApiResponse)
            )
        )

        val expectedResponse = """
            {
                "mbid": "f27ec8db-af05-4f36-916e-3d57f91ecf5e",
                "name": "Michael Jackson",
                "gender": "Male",
                "country": "US",
                "disambiguation": "“King of Pop”"
            }
        """.trimIndent()

        client.get().uri("/musify/music-artist/details/f27ec8db-af05-4f36-916e-3d57f91ecf5e")
            .exchange()
            .expectStatus().isOk
            .expectBody().json(expectedResponse)
    }
}
