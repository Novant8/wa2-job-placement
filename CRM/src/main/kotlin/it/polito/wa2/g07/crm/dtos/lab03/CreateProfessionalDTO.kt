package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO

data class CreateProfessionalDTO
    (
            val contactInfo: CreateContactDTO,
            val location: String,
            val skills: Set<String>,
            var dailyRate: Double,
            var notes: String?
            )
{


}