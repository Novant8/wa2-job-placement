package it.polito.wa2.g07.crm.kafka

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterEventValue(
    @JsonProperty("auth_method")
    val authMethod: String,

    @JsonProperty("auth_type")
    val authType: String,

    @JsonProperty("register_method")
    val registerMethod: String,

    val userId: String,
    val username: String,
    val email: String,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String,

    @JsonProperty("code_id")
    val codeId: String,

    @JsonProperty("redirect_uri")
    val redirectUri: String
)
