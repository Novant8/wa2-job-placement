package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern

data class SsnDTO (
    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "SSN must not be blank")
    @field:Schema(example = "123456")
    val ssn: String?
)
