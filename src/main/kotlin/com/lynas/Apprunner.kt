package com.lynas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@SpringBootApplication
class AppRunner {

    @Bean
    fun webClient() : WebClient {
        return WebClient.builder()
            .filters {
                it.add(logRequest())
                it.add(logResponse())
            }
            .build()
    }
}

fun logRequest() : ExchangeFilterFunction {
    return ExchangeFilterFunction.ofRequestProcessor {
        println(it.url())
        Mono.just(it)
    }
}

fun logResponse() : ExchangeFilterFunction {
    return ExchangeFilterFunction.ofResponseProcessor {
        it.body { inputMessage, context ->
            println(inputMessage)
        }
        Mono.just(it)
    }
}

fun main(args: Array<String>) {
    runApplication<AppRunner>(*args)
}

@RestController
class DemoController(val webClient: WebClient) {

    @GetMapping("/demo")
    fun demo(): String {
        webClient.get()
            .uri("https://634beeaad90b984a1e425527.mockapi.io/test")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntityFlux(Sample::class.java)

            .block()
            ?.body?.collectList()?.block()?.map {
                println(it.name)
            }
        return "demo"
    }
}

data class Sample(
    val createdAt: String,
    val name: String,
    val avatar: String,
    val id: String,
)

// sample json
// from https://mockapi.io/projects/634beeaad90b984a1e425528
//{
//    "createdAt": "2022-10-15T18:26:00.995Z",
//    "name": "Winifred Kassulke",
//    "avatar": "https://cloudflare-ipfs.com/ipfs/Qmd3W5DuhgHirLHGVixi6V76LhCkZUz6pnFt5AJBiyvHye/avatar/85.jpg",
//    "id": "1"
//  }