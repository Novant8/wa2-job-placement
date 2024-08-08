package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SurnameDTO (
    @field:NotNull(message = "Surname must not be null ")
    @field:NotBlank(message = "Surname must not be blank ")
    @field:Schema(example = "Doe")
    val surname: String
)
