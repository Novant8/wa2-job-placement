package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.AddressType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class MessageCreateDTO (
    @field:Valid
    val sender: AddressDTO,
    @field:NotNull
    @field:NotBlank
    val channel: String,
    @field:NotNull
    @field:NotBlank
    val subject: String,
    @field:NotNull
    @field:NotBlank
    val body:String,
)
