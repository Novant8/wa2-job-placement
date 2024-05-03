package it.polito.wa2.g07.crm.dtos

import jakarta.validation.constraints.NotBlank
import kotlin.time.Duration

class JobOfferCreateDTO (
    @field:NotBlank
    val description: String,

    val requiredSkills: Set<String>,

    val duration: Duration,

    val notes: String? = null
)