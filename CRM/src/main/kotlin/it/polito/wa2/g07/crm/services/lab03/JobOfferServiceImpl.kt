package it.polito.wa2.g07.crm.services.lab03



import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.repositories.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import kotlin.jvm.optionals.getOrElse

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
): JobOfferService {

    @Transactional
    override fun createJobOffer(customerId: Long, job: JobOfferCreateDTO): JobOfferDTO {


        val customer = customerRepository.findById(customerId).getOrElse { throw EntityNotFoundException("The customer does not exist") }

        val jobOffer = JobOffer(
            requiredSkills = job.requiredSkills,
            duration= job.duration,
            notes = job.notes,
            status = OfferStatus.CREATED,
            description = job.description
            )
        customer.addPlacement(jobOffer)
        return jobOfferRepository.save(jobOffer).toJobOfferDTO()
    }
    @Transactional
    override fun searchJobOffer(filterDTO: JobOfferFilterDTO, pageable: Pageable): Page<JobOfferReducedDTO> {

        return jobOfferRepository.findAll(filterDTO.toSpecification(),pageable).map { it.toJobOfferReducedDTO() }
    }

    @Transactional
    override fun searchJobOfferById(idOffer: Long): JobOfferDTO {
        val job= jobOfferRepository.findById(idOffer).getOrElse { throw EntityNotFoundException("The offer does not exist") }
        return job.toJobOfferDTO()
    }
    @Transactional
    override  fun getJobOfferValue(idOffer:Long):Double?{
        val job= jobOfferRepository.findById(idOffer).getOrElse { throw EntityNotFoundException("The offer does not exist") }
        return job.value
    }

}