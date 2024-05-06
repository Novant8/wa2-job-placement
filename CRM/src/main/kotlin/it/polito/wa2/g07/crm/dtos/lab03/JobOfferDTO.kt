package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional
import kotlin.time.Duration

data class JobOfferDTO (
    val id:Long,
    val customer: Customer,
    val requiredSkills: List<String>,
    val duration: Duration,
    val offerStatus: OfferStatus,
    val notes: String?,
    val professional: Professional,
    val value:Double
)


