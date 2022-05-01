package de.sweller.musicservice

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
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
    private lateinit var mockCoverArtArchiveServer: WireMockServer

    private val musicBrainzApiResponse: String? =
        this::class.java.classLoader.getResource("music-brainz-response.json")?.readText()

    private val wikidataApiResponse: String? =
        this::class.java.classLoader.getResource("wikidata-response.json")?.readText()

    private val wikipediaApiResponse: String? =
        this::class.java.classLoader.getResource("wikipedia-response.json")?.readText()

    private val gotToBeThereCoverArtArchiveResponse: String? =
        this::class.java.classLoader.getResource("got-to-be-there-response.json")?.readText()

    private val benCoverArtArchiveResponse: String? =
        this::class.java.classLoader.getResource("ben-response.json")?.readText()

    private val musicAndMeCoverArtArchiveResponse: String? =
        this::class.java.classLoader.getResource("music-and-me-response.json")?.readText()


    @BeforeAll
    fun oneTimeSetUp() {
        mockMusicBrainzServer = WireMockServer(8082)
        mockWikidataServer = WireMockServer(8083)
        mockWikipediaServer = WireMockServer(8084)
        mockCoverArtArchiveServer = WireMockServer(8085)
    }

    @BeforeEach
    fun setUp() {
        mockMusicBrainzServer.start()
        mockWikidataServer.start()
        mockWikipediaServer.start()
        mockCoverArtArchiveServer.start()
    }

    @AfterEach
    fun tearDown() {
        mockMusicBrainzServer.stop()
        mockWikidataServer.stop()
        mockWikipediaServer.stop()
        mockCoverArtArchiveServer.stop()
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

        mockCoverArtArchiveServer.stubFor(
            get("/release-group/97e0014d-a267-33a0-a868-bb4e2552918a").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(gotToBeThereCoverArtArchiveResponse)
            )
        )

        mockCoverArtArchiveServer.stubFor(
            get("/release-group/51343255-0ad3-3635-9aa2-548ba939b23e").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(benCoverArtArchiveResponse)
            )
        )

        mockCoverArtArchiveServer.stubFor(
            get("/release-group/06b064b9-01e7-32d8-b585-86404584e795").willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(musicAndMeCoverArtArchiveResponse)
            )
        )

        val expectedResponse = """
            {
                "mbid": "f27ec8db-af05-4f36-916e-3d57f91ecf5e",
                "name": "Michael Jackson",
                "gender": "Male",
                "country": "US",
                "disambiguation": "“King of Pop”",
                "description": "Michael Joseph Jackson was an American singer, songwriter, and dancer. Dubbed the \"King of Pop\", he is regarded as one of the most significant cultural figures of the 20th century. Over a four-decade career, his contributions to music, dance, and fashion, along with his publicized personal life, made him a global figure in popular culture. Jackson influenced artists across many music genres; through stage and video performances, he popularized complicated dance moves such as the moonwalk, to which he gave the name, as well as the robot. He is the most awarded individual music artist in history.",
                "albums": [
                    {
                      "id": "97e0014d-a267-33a0-a868-bb4e2552918a",
                      "title": "Got to Be There",
                      "imageUrl": "http://coverartarchive.org/release/7d65853b-d547-4885-86a6-51df4005768c/1619682960.jpg"
                    },
                    {
                      "id": "51343255-0ad3-3635-9aa2-548ba939b23e",
                      "title": "Ben",
                      "imageUrl": "http://coverartarchive.org/release/cf81f5db-6b4d-493b-8f8f-c0f8c51442f9/11670488852.jpg"
                    },
                    {
                      "title": "Music & Me",
                      "id": "06b064b9-01e7-32d8-b585-86404584e795",
                      "imageUrl": "http://coverartarchive.org/release/7c73f72d-8fa2-45a7-9125-a04696f64f3a/1620517729.jpg"
                    }
                ]
            }
        """.trimIndent()

        client.get().uri("/musify/music-artist/details/f27ec8db-af05-4f36-916e-3d57f91ecf5e")
            .exchange()
            .expectStatus().isOk
            .expectBody().json(expectedResponse)
    }
}
