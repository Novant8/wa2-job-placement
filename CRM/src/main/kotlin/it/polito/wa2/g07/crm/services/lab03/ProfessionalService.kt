package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO

interface ProfessionalService {
    fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO
    fun bindContactToProfessional(professionalID: Long,notes: String?): ProfessionalDTO
}