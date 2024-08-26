package it.polito.wa2.g07.crm

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig {

    @Value("\${keycloak.server-url}")
    private lateinit var serverUrl: String

    @Value("\${keycloak.realm}")
    private lateinit var realm: String

    @Value("\${keycloak.client-id}")
    private lateinit var clientId: String

    @Value("\${keycloak.client-secret}")
    private lateinit var clientSecret: String

    @Bean
    fun getKeycloakInstance(): Keycloak =
        KeycloakBuilder
            .builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
}