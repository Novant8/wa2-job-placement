package it.polito.wa2.g07.crm.dtos.lab03

import com.fasterxml.jackson.annotation.JsonProperty
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import jakarta.validation.constraints.*
import org.springframework.boot.context.properties.bind.Name
import kotlin.time.Duration

class JobOfferCreateDTO (
    @field:NotBlank
    val description: String,
    @field:Size(min=1)
    val requiredSkills: MutableSet<String>,
    @field: Positive
    var duration: Long,
    val notes: String? = null
)



