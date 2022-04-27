package de.sweller.musicservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class MusicServiceApplication {
    @Bean
    fun webClient(builder: WebClient.Builder): WebClient =
        builder
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build()
            ).build()
}

fun main(args: Array<String>) {
    runApplication<MusicServiceApplication>(*args)
}
