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
    val employmentState: EmploymentState,
    val dailyRate: Double,
    val notes: String?
)

fun Professional.toProfessionalDto() =
    ProfessionalDTO(
        this.professionalId,
        this.contactInfo.toContactDto(),
        this.location,
        this.skills.toSet(),
        this.employmentState,
        this.dailyRate,
        this.notes
    )