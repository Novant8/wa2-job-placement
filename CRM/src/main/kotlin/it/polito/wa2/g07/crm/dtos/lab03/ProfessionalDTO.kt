package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toContactDto

import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional

data class ProfessionalDTO(
    val id: Long,
    val contactInfo: ContactDTO,
    val location: String,
    val skills: Set<String>,
    var dailyRate: Double,
    var employmentState: EmploymentState,
    var notes: String?
)
/* 
fun Professional.toProfessionalDto(): ProfessionalDTO=
    val employmentState: EmploymentState,
    val dailyRate: Double,
    val notes: String?
)
*/

fun Professional.toProfessionalDto() =
    ProfessionalDTO(
        this.professionalId,
        this.contactInfo.toContactDto(),
        this.location,
        this.skills,
        this.daily_rate,
        this.employmentState,
        this.skills.toSet(),
        this.notes
    )