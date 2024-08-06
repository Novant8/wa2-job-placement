package it.polito.wa2.g07.keycloak

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.admin.AdminEvent

class KeycloakCustomEventListener : EventListenerProvider {
    override fun onEvent(event: Event) {
        println("Event:-" + event.userId)
        val mapper = ObjectMapper()
        val value = try {
            val eventDetails = event.details
            eventDetails["userId"] = event.userId
            mapper.writeValueAsString(eventDetails)
        } catch(e: JsonProcessingException) {
            "<JSON Processing Error>"
        }
        Producer.publishEvent("IAM-"+event.type.toString(), value)
    }

    override fun onEvent(adminEvent: AdminEvent, b: Boolean) {
        println("Admin Event:-" + adminEvent.resourceType.name)
        Producer.publishEvent("IAM-"+adminEvent.operationType.toString(), adminEvent.authDetails.userId)
    }

    override fun close() {
    }
}