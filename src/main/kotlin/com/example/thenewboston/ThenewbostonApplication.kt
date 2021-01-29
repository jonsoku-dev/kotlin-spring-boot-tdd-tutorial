package com.example.thenewboston

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ThenewbostonApplication {

    @Bean // restTemplate 를 쓰려면 이것을 꼭 작성해야한다 !
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()
}

fun main(args: Array<String>) {
    runApplication<ThenewbostonApplication>(*args)
}
