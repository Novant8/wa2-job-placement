package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toEntity
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional

data class CreateProfessionalDTO
    (
        val contactInfo: CreateContactDTO,
        val location: String,
        val skills: Set<String>,
        var dailyRate: Double,
        var employmentState: EmploymentState,
        var notes: String?
            )

          fun CreateProfessionalDTO.toEntity(): Professional= Professional(
                  this.contactInfo.toEntity(),
                  this.location,
                  this.skills,
                  this.dailyRate,
                  this.employmentState,
                  this.notes
          )
