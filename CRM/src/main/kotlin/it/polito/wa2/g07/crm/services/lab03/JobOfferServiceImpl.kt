package it.polito.wa2.g07.crm.services.lab03



import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab02.AddressType
import it.polito.wa2.g07.crm.entities.lab02.Email
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.repositories.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import org.apache.camel.ProducerTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import kotlin.jvm.optionals.getOrElse

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
    private val professionalRepository: ProfessionalRepository,
    private val kafkaTemplate: KafkaTemplate<String, JobOfferKafkaDTO>
): JobOfferService {
    @Autowired
    private lateinit var producerTemplate: ProducerTemplate

    @Value("\${gmail.username}")
    private lateinit var from: String

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
        kafkaTemplate.send("JOB_OFFER-CREATE",jobOffer.toJobOfferKafkaDTO())

        val customerMail = jobOffer.customer.contactInfo.addresses
            .filter { it.addressType == AddressType.EMAIL }
            .map { it as Email } // Assuming EmailAddress is the correct type
            .firstOrNull()?.email
        val headers = mapOf(
            "From" to from,
            "To" to customerMail,
            "Subject" to "Created Job Offer [ "+ jobOffer.description +" ]"
        )
        producerTemplate.sendBodyAndHeaders("seda:sendEmail", "Dear Customer, the Job Offer [ "+ jobOffer.description +" ] has been CREATED correctly. Best regards", headers)



        return jobOffer.toJobOfferDTO()
    }
    @Transactional
    override fun addCandidate(jobOfferId: Long, professionalId: Long): JobOfferDTO {
        val professional = professionalRepository.findById(professionalId).getOrElse { throw EntityNotFoundException("The Professional doesn't exist") }

        val jobOffer = jobOfferRepository.findById(jobOfferId).getOrElse { throw EntityNotFoundException("The given Job Offer doesn't exist") }

        jobOffer.addCandidate(professional);

        return jobOffer.toJobOfferDTO()
    }
    @Transactional
    override fun removeCandidate(jobOfferId: Long, professionalId: Long): Long {
        val professional = professionalRepository.findById(professionalId).getOrElse { throw EntityNotFoundException("The Professional doesn't exist") }

        val jobOffer = jobOfferRepository.findById(jobOfferId).getOrElse { throw EntityNotFoundException("The given Job Offer doesn't exist") }

        jobOffer.removeCandidate(professional)



        return professional.professionalId
    }

    @Transactional
    override fun addRefusedCandidate(jobOfferId: Long, professionalId: Long): JobOfferDTO {
        val professional = professionalRepository.findById(professionalId).getOrElse { throw EntityNotFoundException("The Professional doesn't exist") }

        val jobOffer = jobOfferRepository.findById(jobOfferId).getOrElse { throw EntityNotFoundException("The given Job Offer doesn't exist") }

        jobOffer.addRefused(professional);

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
    override fun updateJobOfferStatus(jobOfferId: Long, jobOfferUpdateStatusDTO: JobOfferUpdateStatusDTO): JobOfferDTO {



        val jobOffer = jobOfferRepository.findById(jobOfferId).orElseThrow{ EntityNotFoundException("Job offer with ID $jobOfferId was not found.") }
        logger.info("Status job offer : "+ jobOfferUpdateStatusDTO.status)

        if(!jobOffer.status.canUpdateTo(jobOfferUpdateStatusDTO.status)) {
            throw InvalidParamsException("Job offer #$jobOfferId cannot be updated to status ${jobOfferUpdateStatusDTO.status}")
        }

        jobOffer.status = jobOfferUpdateStatusDTO.status
        var msgProf =""
        /* Update professional if needed */
        when(jobOfferUpdateStatusDTO.status) {
            OfferStatus.CANDIDATE_PROPOSAL -> {
                if(jobOfferUpdateStatusDTO.professionalId == null) {
                    throw InvalidParamsException("A professional is required to update to status ${jobOfferUpdateStatusDTO.status}")
                }
                jobOffer.professional = professionalRepository.findById(jobOfferUpdateStatusDTO.professionalId).orElseThrow { EntityNotFoundException("Professional with ID ${jobOfferUpdateStatusDTO.professionalId} was not found.") }
                msgProf =  "Dear professional.  We want to inform you have been selected for the job offer [ "+ jobOffer.description +" ], please enter in the platform for see the progress of the job proposal. Best regards"

            }
            OfferStatus.CONSOLIDATED -> {
                if(jobOffer.professional?.employmentState != EmploymentState.UNEMPLOYED) {
                    throw InvalidParamsException("The given professional is not available for work.")
                }
                jobOffer.professional?.employmentState = EmploymentState.EMPLOYED

                msgProf =  "Dear Professional, your enrolment process is completed for Job Offer [ "+ jobOffer.description +" ], you can now start work. Best regards"
            }
            OfferStatus.DONE -> {
                jobOffer.professional?.employmentState = EmploymentState.UNEMPLOYED
                msgProf =  "Dear Professional, the Job Offer [ "+ jobOffer.description +" ] is terminated, you are now available for other work. Best regards"

            }
            OfferStatus.ABORTED->{
                jobOffer.professional?.employmentState = EmploymentState.UNEMPLOYED
                msgProf =  "Dear Professional, the Job Offer [ "+ jobOffer.description +" ] is not more available. Best regards"

            }
            OfferStatus.SELECTION_PHASE -> {
                jobOffer.professional?.employmentState = EmploymentState.UNEMPLOYED
                jobOffer.professional = null

            }
            else -> { /* No need to update professional  */ }
        }

        val updatedJobOffer = jobOfferRepository.save(jobOffer)
        logger.info("Updated status for Job Offer #${jobOffer.offerId} from ${jobOffer.status} to ${updatedJobOffer.status}.")
        kafkaTemplate.send("JOB_OFFER-UPDATE",updatedJobOffer.toJobOfferKafkaDTO())

        val customerMail = jobOffer.customer.contactInfo.addresses
            .filter { it.addressType == AddressType.EMAIL }
            .map { it as Email } // Assuming EmailAddress is the correct type
            .firstOrNull()?.email
        if (jobOffer.professional != null && msgProf!= ""){

            val professionalMail = jobOffer.professional!!.contactInfo.addresses
                .filter { it.addressType == AddressType.EMAIL }
                .map { it as Email } // Assuming EmailAddress is the correct type
                .firstOrNull()?.email
            val headers = mapOf(
                "From" to from,
                "To" to professionalMail,
                "Subject" to "Update Job Offer [ "+ jobOffer.description +" ]"
            )
            producerTemplate.sendBodyAndHeaders("seda:sendEmail", msgProf, headers)

        }


        val headers = mapOf(
            "From" to from,
            "To" to customerMail,
            "Subject" to "Update Job Offer [ "+ jobOffer.description +" ]"
        )
        producerTemplate.sendBodyAndHeaders("seda:sendEmail", "Dear Customer, the Job Offer [ "+ jobOffer.description +" ] is now in "+jobOffer.status+" phase. Best regards", headers)



        return jobOffer.toJobOfferDTO()
    }

    override fun updateJobOffer(jobOfferId: Long, jobOfferUpdateDTO: JobOfferUpdateDTO): JobOfferDTO {
        val jobOffer = jobOfferRepository.findById(jobOfferId).orElseThrow{ EntityNotFoundException("Job offer with ID $jobOfferId was not found.") }
        jobOffer.duration = jobOfferUpdateDTO.duration
        jobOffer.requiredSkills= jobOfferUpdateDTO.requiredSkills
        jobOffer.notes= jobOfferUpdateDTO.notes
        jobOffer.description = jobOfferUpdateDTO.description
        val updatedJobOffer = jobOfferRepository.save(jobOffer)
        kafkaTemplate.send("JOB_OFFER-UPDATE",updatedJobOffer.toJobOfferKafkaDTO())
        return updatedJobOffer.toJobOfferDTO()
    }



    companion object{
        val logger: Logger = LoggerFactory.getLogger(JobOfferServiceImpl::class.java)
    }
}