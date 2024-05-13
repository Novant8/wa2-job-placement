package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab02.Message
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class MessageCreateDTO (
    @field:Valid
    val sender: AddressDTO,

    @field:NotNull
    @field:NotBlank
    @field:Schema(implementation = MessageChannel::class)
    val channel: String,

    @field:NotNull
    @field:NotBlank
    val subject: String,

    @field:NotNull
    @field:NotBlank
    val body:String,
)

fun MessageCreateDTO.toEntity() : Message {
     val message= Message(
         this.subject,
         this.body,
         this.sender.toEntity(),
         MessageChannel.valueOf(this.channel)
     )

    return message
}
