package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.entities.lab03.OfferStatus

data class JobOfferUpdateDTO (
    val status: OfferStatus,
    val professionalId: Long? = null
)