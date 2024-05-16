package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException

import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository




import it.polito.wa2.g07.crm.dtos.lab02.CreateProfessionalReducedDTO
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class ProfessionalServiceImpl (private val professionalRepository: ProfessionalRepository,private val contactRepository: ContactRepository) : ProfessionalService {

    @Transactional
    override fun createProfessional(professional: CreateProfessionalDTO): ProfessionalDTO {


          if (professional.contactInfo.category?.uppercase()!= "PROFESSIONAL"){
            throw InvalidParamsException(" You must register a Professional user ")
        }
        return professionalRepository.save(professional.toEntity()).toProfessionalDto()
    }
/* 
    @Transactional
    override fun bindContactToProfessional(
        contactID: Long,
        location: String,
        skills: Set<String>,
        dailyRate: Double,
        employmentState: EmploymentState,
        notes: String?
    ): ProfessionalDTO {
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
    }  */

    @Transactional()
    override fun postProfessionalNotes(professionalId: Long, notes: String?): ProfessionalDTO {
        val professionalOpt= professionalRepository.findById(professionalId)
        if (!professionalOpt.isPresent){
            throw  EntityNotFoundException("Professional with id : $professionalId is not present" )
        }
        val professional = professionalOpt.get()
        professional.notes= notes

        return professionalRepository.save(professional).toProfessionalDto()

    }

    @Transactional()
    override fun postProfessionalLocation(professionalId: Long, location: String): ProfessionalDTO {

        val professionalOpt= professionalRepository.findById(professionalId)
        if (!professionalOpt.isPresent){
            throw  EntityNotFoundException("Professional with id : $professionalId is not present" )
        }
        val professional = professionalOpt.get()
        professional.location= location


        return professionalRepository.save(professional).toProfessionalDto()

    }

    @Transactional()
    override fun postProfessionalSkills(professionalId: Long, skills: Set<String>): ProfessionalDTO {
        val professionalOpt= professionalRepository.findById(professionalId)
        if (!professionalOpt.isPresent){
            throw  EntityNotFoundException("Professional with id : $professionalId is not present" )
        }
        val professional = professionalOpt.get()
        professional.skills= skills


        return professionalRepository.save(professional).toProfessionalDto()
    }

    @Transactional
    override fun postProfessionalEmploymentState(professionalId: Long, employmentState: EmploymentState): ProfessionalDTO {
        val professionalOpt= professionalRepository.findById(professionalId)
        if (!professionalOpt.isPresent){
            throw  EntityNotFoundException("Professional with id : $professionalId is not present" )
        }
        val professional = professionalOpt.get()
        professional.employmentState= employmentState


        return professionalRepository.save(professional).toProfessionalDto()
    }

    @Transactional
    override fun postProfessionalDailyRate(professionalId: Long, dailyRate: Double): ProfessionalDTO {
        val professionalOpt= professionalRepository.findById(professionalId)
        if (!professionalOpt.isPresent){
            throw  EntityNotFoundException("Professional with id : $professionalId is not present" )
        }
        val professional = professionalOpt.get()
        professional.dailyRate= dailyRate


        return professionalRepository.save(professional).toProfessionalDto()
    }





    @Transactional(readOnly = true)
    override fun searchProfessionals(filterDTO: ProfessionalFilterDTO, pageable: Pageable): Page<ProfessionalReducedDTO> {
        return professionalRepository.findAll(filterDTO.toSpecification(), pageable).map { it.toProfessionalReducedDto() }
    }

    @Transactional(readOnly = true)
    override fun getProfessionalById(professionalId: Long): ProfessionalDTO {
        return professionalRepository
            .findById(professionalId)
            .map { it.toProfessionalDto() }
            .orElseThrow { EntityNotFoundException("Professional with ID $professionalId not found.") }
    }

    override fun create(professional: CreateProfessionalDTO): ProfessionalDTO {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun bindContactToProfessional(contactId: Long, create: CreateProfessionalReducedDTO): ProfessionalDTO {
        val contactOpt = contactRepository.findById(contactId)
        if (!contactOpt.isPresent){
            throw EntityNotFoundException("Contact with Id :$contactId is not found")
        }

        val contact = contactOpt.get()
        if (professionalRepository.findByContactInfo(contact).isPresent){
            throw ContactAssociationException("Contact with id : $contactId is already associated to another Professional ")
        }else if(contact.category.name.uppercase() != "PROFESSIONAL"){
            throw InvalidParamsException("You must register a professional user ")
        }
        val professional = Professional(contactInfo = contact,
                dailyRate = create.dailyRate,
                location = create.location,
                skills = create.skills,
                employmentState = EmploymentState.UNEMPLOYED,
                notes = create.notes,
            )

        return professionalRepository.save(professional).toProfessionalDto()
    }

}