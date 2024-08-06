package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import org.keycloak.admin.client.Keycloak
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KeycloakUserServiceImpl(private val keycloak: Keycloak) : KeycloakUserService {

    @Value("\${keycloak.realm}")
    private lateinit var realm: String

    @Value("\${keycloak.client-id}")
    private lateinit var clientId: String

    fun setUserAsRole(userId: String, roleName: String) {
        // Retrieve internal UUID of the client
        val clientUUID =
            keycloak
                .realm(realm)
                .clients().findByClientId(clientId)
                .first().id

        // Retrieve customer role
        val customer =
            keycloak
                .realm(realm)
                .clients().get(clientUUID)
                .roles().get("customer")
                .toRepresentation()

        // Retrieve professional role
        val professional =
            keycloak
                .realm(realm)
                .clients().get(clientUUID)
                .roles().get("professional")
                .toRepresentation()

        // Retrieve user roles
        val userRoles =
            keycloak
                .realm(realm)
                .users().get(userId)
                .roles().clientLevel(clientUUID)
                .listAll()

        // Verify that the user is not already a customer or a professional
        if(userRoles.contains(customer) || userRoles.contains(professional)) {
            throw InvalidParamsException("User $userId is already a customer or professional.")
        }

        // Assign role to user
        keycloak
            .realm(realm)
            .users().get(userId)
            .roles().clientLevel(clientUUID)
            .add(listOf(customer, professional).filter{ it.name == roleName })
    }

    override fun setUserAsCustomer(userId: String) {
        setUserAsRole(userId, "customer")
    }

    override fun setUserAsProfessional(userId: String) {
        setUserAsRole(userId, "professional")
    }

    override fun changeUserName(userId: String, name: String) {
        // Retrieve user
        val user =
            keycloak
                .realm(realm)
                .users().get(userId)
                .toRepresentation()

        // Update attribute
        user.firstName = name

        // Update user with new attributes
        keycloak
            .realm(realm)
            .users().get(userId)
            .update(user)
    }

    override fun changeUserSurname(userId: String, surname: String) {
        // Retrieve user
        val user =
            keycloak
                .realm(realm)
                .users().get(userId)
                .toRepresentation()

        // Update attribute
        user.lastName = surname

        // Update user with new attributes
        keycloak
            .realm(realm)
            .users().get(userId)
            .update(user)
    }

    override fun changeUserEmail(userId: String, email: String) {
        // Retrieve user
        val user =
            keycloak
                .realm(realm)
                .users().get(userId)
                .toRepresentation()

        // Update attribute
        user.email = email

        // Update user with new attributes
        keycloak
            .realm(realm)
            .users().get(userId)
            .update(user)
    }

}