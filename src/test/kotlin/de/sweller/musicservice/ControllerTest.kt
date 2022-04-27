package de.sweller.musicservice

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebTestClient
internal class ControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    private lateinit var mockMusicBrainzServer: WireMockServer
    private lateinit var mockWikidataServer: WireMockServer
    private lateinit var mockWikipediaServer: WireMockServer


    private val musicBrainzApiResponse: String? =
        this::class.java.classLoader.getResource("music-brainz-response.json")?.readText()

    private val wikidataApiResponse: String? =
        this::class.java.classLoader.getResource("wikidata-response.json")?.readText()

    private val wikipediaApiResponse: String? =
        this::class.java.classLoader.getResource("wikipedia-response.json")?.readText()

    @BeforeAll
    fun oneTimeSetUp() {
        mockMusicBrainzServer = WireMockServer(8082)
        mockWikidataServer = WireMockServer(8083)
        mockWikipediaServer = WireMockServer(8084)
    }

    @BeforeEach
    fun setUp() {
        mockMusicBrainzServer.start()
        mockWikidataServer.start()
        mockWikipediaServer.start()
    }

    @AfterEach
    fun tearDown() {
        mockMusicBrainzServer.stop()
        mockWikidataServer.stop()
        mockWikipediaServer.stop()
    }

    @Test
    fun getArtist() {
        mockMusicBrainzServer.stubFor(
            get("/artist/f27ec8db-af05-4f36-916e-3d57f91ecf5e?fmt=json&inc=url-rels+release-groups").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(musicBrainzApiResponse)
            )
        )

        mockWikidataServer.stubFor(
            get("/Special:EntityData/Q2831.json").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(wikidataApiResponse)
            )
        )

        mockWikipediaServer.stubFor(
            get("/page/summary/Michael_Jackson").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(wikipediaApiResponse)
            )
        )


        val expectedResponse = """
            {
                "mbid": "f27ec8db-af05-4f36-916e-3d57f91ecf5e",
                "name": "Michael Jackson",
                "gender": "Male",
                "country": "US",
                "disambiguation": "“King of Pop”",
                "description": "Michael Joseph Jackson was an American singer, songwriter, and dancer. Dubbed the \"King of Pop\", he is regarded as one of the most significant cultural figures of the 20th century. Over a four-decade career, his contributions to music, dance, and fashion, along with his publicized personal life, made him a global figure in popular culture. Jackson influenced artists across many music genres; through stage and video performances, he popularized complicated dance moves such as the moonwalk, to which he gave the name, as well as the robot. He is the most awarded individual music artist in history."
            }
        """.trimIndent()

        client.get().uri("/musify/music-artist/details/f27ec8db-af05-4f36-916e-3d57f91ecf5e")
            .exchange()
            .expectStatus().isOk
            .expectBody().json(expectedResponse)
    }
}
