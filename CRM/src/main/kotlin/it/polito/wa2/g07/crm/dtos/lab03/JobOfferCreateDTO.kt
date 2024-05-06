package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import jakarta.validation.constraints.*
import kotlin.time.Duration

class JobOfferCreateDTO (
    @field:NotBlank
    val description: String,
    @field:Size(min=1)
    val requiredSkills: Set<String>,
    @field: Positive
    val duration: Duration,
    val notes: String? = null
)



