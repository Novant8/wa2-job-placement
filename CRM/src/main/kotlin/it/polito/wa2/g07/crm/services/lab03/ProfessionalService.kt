package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalReducedDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO

interface ProfessionalService {
    fun searchProfessionals(filterDTO: ProfessionalFilterDTO, pageable: Pageable): Page<ProfessionalReducedDTO>
    fun create(professional: CreateProfessionalDTO): ProfessionalDTO
}