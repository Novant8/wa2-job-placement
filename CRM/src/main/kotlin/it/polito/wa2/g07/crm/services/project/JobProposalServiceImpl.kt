package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
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

        val jobProposal = JobProposal(jobOffer)

         customer.addJobProposal(jobProposal);
         professional.addJobProposal(jobProposal)


        return proposalRepository.save(jobProposal).toJobProposalDTO()
    }

    @Transactional
    override fun searchJobProposalById(idProposal: Long): JobProposalDTO {
        val proposal = proposalRepository.findById(idProposal).getOrElse { throw EntityNotFoundException("The proposal does not exist") }
        return proposal.toJobProposalDTO()
    }

    @Transactional
    override fun searchJobProposalByJobOfferAndProfessional(idJobOffer: Long, idProfessional: Long): JobProposalDTO {

        val proposal = proposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(idJobOffer, idProfessional,ProposalStatus.DECLINED).getOrElse { throw EntityNotFoundException("The proposal does not exist") }
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
            if (!customerConfirm)
                proposal.status = ProposalStatus.DECLINED
            return proposalRepository.save(proposal).toJobProposalDTO()
        }


    }

    @Transactional
    override fun professionalConfirmDecline(proposalId: Long,professionalId: Long,professionalConfirm: Boolean): JobProposalDTO {

        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }
        if(proposal.professional.professionalId != professionalId)
            throw EntityNotFoundException("The professional does not belong to this proposal")
        else
        {

            if (!professionalConfirm)
                proposal.status = ProposalStatus.DECLINED
            else
                proposal.status= ProposalStatus.ACCEPTED

            return proposalRepository.save(proposal).toJobProposalDTO()
        }


    }

    @Transactional
    override fun loadDocument(proposalId: Long, documentId: Long?): JobProposalDTO {
        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }

        proposal.documentId = documentId;
        return proposalRepository.save(proposal).toJobProposalDTO()
    }
}