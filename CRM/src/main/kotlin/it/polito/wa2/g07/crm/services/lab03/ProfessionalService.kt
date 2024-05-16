package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalReducedDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState

interface ProfessionalService {
    fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO
    fun bindContactToProfessional(contactID: Long,location:String,skills: Set<String>, dailyRate:Double,employmentState: EmploymentState, notes: String?): ProfessionalDTO
    fun searchProfessionals(filterDTO: ProfessionalFilterDTO, pageable: Pageable): Page<ProfessionalReducedDTO>
    fun getProfessionalById(professionalId: Long): ProfessionalDTO
    //fun create(professional: CreateProfessionalDTO): ProfessionalDTO
    fun postProfessionalNotes(professionalId:Long, notes: String?): ProfessionalDTO
    fun postProfessionalLocation(professionalId:Long, location :String): ProfessionalDTO
    fun postProfessionalDailyRate(professionalId:Long, dailyRate :Double): ProfessionalDTO
    fun postProfessionalSkills(professionalId:Long, skills: Set<String> ): ProfessionalDTO
    fun postProfessionalEmploymentState(professionalId:Long, employmentState: EmploymentState ): ProfessionalDTO

}