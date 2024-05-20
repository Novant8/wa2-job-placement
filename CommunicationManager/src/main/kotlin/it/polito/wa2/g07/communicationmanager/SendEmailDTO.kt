package it.polito.wa2.g07.communicationmanager

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendEmailDTO(
    @field:Email(message = "Destination must be a valid e-mail")
    val to: String,

    @field:NotBlank(message = "Subject must not be blank")
    val subject: String,

    val body: String = ""
)
