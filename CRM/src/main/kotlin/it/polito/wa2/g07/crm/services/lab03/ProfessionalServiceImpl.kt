package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfessionalServiceImpl(
    private val professionalRepository: ProfessionalRepository
) : ProfessionalService {

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
}