package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.JobProposalValidationException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
import it.polito.wa2.g07.crm.services.lab03.JobOfferServiceImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

         logger.info("Created Job Proposal with ID #${jobProposal.proposalID}")
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

        else if (proposal.documentId == null && customerConfirm ){
            throw JobProposalValidationException("The customer must upload a contract before accept the proposal ")
        }
        else
        {
            proposal.customerConfirm = customerConfirm
            if (!customerConfirm){
                proposal.status = ProposalStatus.DECLINED
                logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has declined Job Proposal #${proposal.proposalID}")
            }else{
                logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has accepted Job Proposal #${proposal.proposalID}")
            }

            return proposalRepository.save(proposal).toJobProposalDTO()
        }


    }

    @Transactional
    override fun professionalConfirmDecline(proposalId: Long,professionalId: Long,professionalConfirm: Boolean): JobProposalDTO {

        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }
        if(proposal.professional.professionalId != professionalId)
            throw EntityNotFoundException("The professional does not belong to this proposal")
        else if (!proposal.customerConfirm){
            throw JobProposalValidationException("The professional cannot accept the proposal before the customer has done so.")
        }
        else if (proposal.professionalSignedContract == null && professionalConfirm ){
            throw JobProposalValidationException("The professional must upload a signed contract before accept the proposal ")
        }
        else
        {

            if (!professionalConfirm){
                logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has declined Job Proposal #${proposal.proposalID}")
                proposal.status = ProposalStatus.DECLINED
            }

            else{
                proposal.status= ProposalStatus.ACCEPTED
                logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has accepted Job Proposal #${proposal.proposalID}")
            }


            return proposalRepository.save(proposal).toJobProposalDTO()
        }


    }

    @Transactional
    override fun loadDocument(proposalId: Long, documentId: Long?): JobProposalDTO {
        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }

        proposal.documentId = documentId;
        if (documentId != null )
          logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has upload a new contract for Job Proposal #${proposal.proposalID}")
        else
            logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has removed the contract from Job Proposal #${proposal.proposalID}")


        return proposalRepository.save(proposal).toJobProposalDTO()
    }

    @Transactional
    override fun loadSignedDocument(proposalId: Long, documentId: Long?): JobProposalDTO {
        val proposal = proposalRepository.findById(proposalId).orElseThrow { EntityNotFoundException("The proposal with ID $proposalId is not found") }
        if (proposal.documentId == null ){
            throw JobProposalValidationException("The professional cannot upload the contract before the customer has done so")
        }

        if (documentId != null )
            logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has upload a new contract for Job Proposal #${proposal.proposalID}")
        else
            logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has removed the contract from Job Proposal #${proposal.proposalID}")

        proposal.professionalSignedContract = documentId;
        return proposalRepository.save(proposal).toJobProposalDTO()
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger(JobOfferServiceImpl::class.java)
    }
}