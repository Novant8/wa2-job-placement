package it.polito.wa2.g07.keycloak

import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class KeycloakCustomEventListenerProviderFactory : EventListenerProviderFactory {
    override fun create(keycloakSession: KeycloakSession): EventListenerProvider {
        return KeycloakCustomEventListener()
    }

    override fun init(scope: Config.Scope) {
    }

    override fun postInit(keycloakSessionFactory: KeycloakSessionFactory) {
    }

    override fun close() {
    }

    override fun getId(): String {
        return "kafka-event-listener"
    }
}