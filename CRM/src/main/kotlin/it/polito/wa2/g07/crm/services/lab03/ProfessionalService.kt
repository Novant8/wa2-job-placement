package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState

interface ProfessionalService {
    fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO
    fun bindContactToProfessional(contactID: Long,location:String,skills: Set<String>, dailyRate:Double,employmentState: EmploymentState, notes: String?): ProfessionalDTO
}