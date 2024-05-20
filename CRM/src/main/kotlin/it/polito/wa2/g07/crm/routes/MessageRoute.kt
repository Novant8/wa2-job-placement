package it.polito.wa2.g07.crm.routes

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class MessageRoute : RouteBuilder() {
    override fun configure() {
        from("direct:messages")
            .log("Funziono parte 2")
    }
}