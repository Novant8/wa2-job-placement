package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
import org.springframework.data.jpa.domain.AbstractPersistable_.id
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

    @Transactional
    override fun searchJobProposalById(idProposal: Long): JobProposalDTO {
        val proposal = proposalRepository.findById(idProposal).getOrElse { throw EntityNotFoundException("The proposal does not exist") }
        return proposal.toJobProposalDTO()
    }

    @Transactional
    override fun searchJobProposalByJobOfferAndProfessional(idJobOffer: Long, idProfessional: Long): JobProposalDTO {

        val proposal = proposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalId(idJobOffer, idProfessional).getOrElse { throw EntityNotFoundException("The proposal does not exist") }
        return proposal.toJobProposalDTO()
    }

    @Transactional
    override fun customerConfirmDecline(proposalId: Long,customerId: Long,customerConfirm: Boolean): JobProposalDTO {
        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }
        if(proposal.customer.customerId != customerId)
            throw EntityNotFoundException("The customer does not belong to this proposal")
        else
        {
            proposal.customerConfirm = customerConfirm
            return proposalRepository.save(proposal).toJobProposalDTO()
        }


    }
}