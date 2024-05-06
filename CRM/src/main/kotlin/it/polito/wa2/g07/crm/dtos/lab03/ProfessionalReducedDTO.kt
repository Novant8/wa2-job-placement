package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional

data class ProfessionalReducedDTO(
    val id: Long,
    val contactInfo: ReducedContactDTO,
    val location: String,
    val skills: Set<String>,
    val employmentState: EmploymentState
)

fun Professional.toProfessionalReducedDto() =
    ProfessionalReducedDTO(
        this.professionalId,
        this.contactInfo.toReducedContactDTO(),
        this.location,
        this.skills.toSet(),
        this.employmentState
    )