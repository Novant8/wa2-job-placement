package it.polito.wa2.g07.apigateway

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    class CustomResponseErrorHandler : DefaultResponseErrorHandler() {
        override fun hasError(statusCode: HttpStatusCode): Boolean {
            return statusCode.is5xxServerError
        }
    }

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate =
        restTemplateBuilder
            .errorHandler(CustomResponseErrorHandler())
            .build()
}