package it.polito.wa2.g07.crm.services.lab03



import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.repositories.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import kotlin.jvm.optionals.getOrElse

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
    private val professionalRepository: ProfessionalRepository,
): JobOfferService {

    @Transactional
    override fun createJobOffer(customerId: Long, job: JobOfferCreateDTO): JobOfferDTO {
        val customer = customerRepository.findById(customerId).getOrElse { throw EntityNotFoundException("The customer does not exist") }

        var jobOffer = JobOffer(
            requiredSkills = job.requiredSkills,
            duration= job.duration,
            notes = job.notes,
            status = OfferStatus.CREATED,
            description = job.description
            )
        customer.addPlacement(jobOffer)

        jobOffer = jobOfferRepository.save(jobOffer)
        logger.info("Created Job Offer with ID #${jobOffer.offerId}")
        return jobOffer.toJobOfferDTO()
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

    @Transactional
    override fun updateJobOfferStatus(jobOfferId: Long, jobOfferUpdateDTO: JobOfferUpdateDTO): JobOfferDTO {
        val jobOffer = jobOfferRepository.findById(jobOfferId).orElseThrow{ EntityNotFoundException("Job offer with ID $jobOfferId was not found.") }

        if(!jobOffer.status.canUpdateTo(jobOfferUpdateDTO.status)) {
            throw InvalidParamsException("Job offer #$jobOfferId cannot be updated to status ${jobOfferUpdateDTO.status}")
        }

        jobOffer.status = jobOfferUpdateDTO.status

        /* Update professional if needed */
        when(jobOfferUpdateDTO.status) {
            OfferStatus.CANDIDATE_PROPOSAL -> {
                if(jobOfferUpdateDTO.professionalId == null) {
                    throw InvalidParamsException("A professional is required to update to status ${jobOfferUpdateDTO.status}")
                }
                jobOffer.professional = professionalRepository.findById(jobOfferUpdateDTO.professionalId).orElseThrow { EntityNotFoundException("Professional with ID ${jobOfferUpdateDTO.professionalId} was not found.") }
            }
            OfferStatus.CONSOLIDATED -> {
                if(jobOffer.professional?.employmentState != EmploymentState.UNEMPLOYED) {
                    throw InvalidParamsException("The given professional is not available for work.")
                }
                jobOffer.professional?.employmentState = EmploymentState.EMPLOYED
            }
            OfferStatus.DONE, OfferStatus.ABORTED -> {
                jobOffer.professional?.employmentState = EmploymentState.UNEMPLOYED
            }
            OfferStatus.SELECTION_PHASE -> {
                jobOffer.professional?.employmentState = EmploymentState.UNEMPLOYED
                jobOffer.professional = null
            }
            else -> { /* No need to update professional  */ }
        }

        val updatedJobOffer = jobOfferRepository.save(jobOffer)
        logger.info("Updated status for Job Offer #${jobOffer.offerId} from ${jobOffer.status} to ${updatedJobOffer.status}.")
        return jobOffer.toJobOfferDTO()
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger(JobOfferServiceImpl::class.java)
    }
}