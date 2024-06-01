package it.polito.wa2.g07.crm.dtos.lab03

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toEntity
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional


data class CreateProfessionalDTO (
    val contactInfo: CreateContactDTO,

    @field:Schema(example = "New York")
    val location: String,

    @field:ArraySchema(arraySchema = Schema(example = "[ \"Proficient in Kotlin\", \"Can work autonomously\" ]"))
    val skills: Set<String>,

    @field:Schema(example = "150")
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

