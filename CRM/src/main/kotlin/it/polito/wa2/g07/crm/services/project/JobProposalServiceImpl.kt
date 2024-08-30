package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class JobProposalServiceImpl (
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
    private val professionalRepository: ProfessionalRepository,
    private val proposalRepository: JobProposalRepository
):JobProposalService{
     @Transactional
    override fun createJobProposal(customerId: Long, professionalId: Long, jobOfferId: Long): JobProposalDTO {
        val customer = customerRepository.findById(customerId).getOrElse { throw EntityNotFoundException("The customer does not exist") }
        val professional = professionalRepository.findById(professionalId).getOrElse { throw EntityNotFoundException("The Professional doesn't exist") }
        val jobOffer = jobOfferRepository.findById(jobOfferId).getOrElse { throw EntityNotFoundException("The given Job Offer doesn't exist") }

        val jobProposal = JobProposal(customer,professional,jobOffer)

         proposalRepository.save(jobProposal)
        return jobProposal.toJobProposalDTO()
    }
}