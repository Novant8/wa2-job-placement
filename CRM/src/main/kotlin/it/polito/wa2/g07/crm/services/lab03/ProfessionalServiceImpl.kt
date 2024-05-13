package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class ProfessionalServiceImpl (private val professionalRepository: ProfessionalRepository,private val contactRepository: ContactRepository) : ProfessionalService {

    @Transactional
    override fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO {
        if (professional.contactInfo.category?.uppercase()!== "PROFESSIONAL"){
            throw InvalidParamsException(" You must register a Professional user")
        }
        return professionalRepository.save(professional.toEntity()).toProfessionalDto()
    }

    @Transactional
    override fun bindContactToProfessional(contactID: Long, location:String, skills: Set<String>, dailyRate:Double, employmentState: EmploymentState, notes: String?): ProfessionalDTO {
        val contact= contactRepository.findById(contactID)
        if(!contact.isPresent) {
        throw EntityNotFoundException("Contact with Id: $contactID is not found")
        }

        val contactFound = contact.get()
        if (professionalRepository.findByContactInfo(contactFound).isPresent)
        {
            throw ContactAssociationException("Contact with id: $contactID is already associated to another professional")
        }
        else if (contactFound.category.name.uppercase() != "PROFESSIONAL"){
            throw InvalidParamsException("You must register a Professional user")
        }
        val professional= Professional(contactInfo = contactFound,location,skills,dailyRate,employmentState,notes )
        return professionalRepository.save(professional).toProfessionalDto()
    }

}