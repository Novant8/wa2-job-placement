package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ContactDTO

import it.polito.wa2.g07.crm.entities.lab03.EmploymentState

data class ProfessionalDTO(
    val id:Long,
    val contactInfo: ContactDTO,
    val location :String,
    val skills: Set<String>,
    var daily_rate: Double,
    var employmentState: EmploymentState,
    var notes: String?
)
{

}