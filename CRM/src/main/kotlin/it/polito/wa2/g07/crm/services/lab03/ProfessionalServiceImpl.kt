package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class ProfessionalServiceImpl (private val professionalRepository: ProfessionalRepository,private val contactRepository: ContactRepository) : ProfessionalService {

    @Transactional
    override fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun bindContactToProfessional(professionalID: Long, notes: String?): ProfessionalDTO {
        TODO("Not yet implemented")
    }

}