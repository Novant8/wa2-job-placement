package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalKafkaDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalKafkaDTO
import it.polito.wa2.g07.crm.entities.lab02.AddressType
import it.polito.wa2.g07.crm.entities.lab02.Email
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.JobProposalValidationException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
import it.polito.wa2.g07.crm.services.lab03.JobOfferServiceImpl
import org.apache.camel.ProducerTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class JobProposalServiceImpl (
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
    private val professionalRepository: ProfessionalRepository,
    private val proposalRepository: JobProposalRepository,
    private val kafkaTemplate: KafkaTemplate<String,JobProposalKafkaDTO>
):JobProposalService{

    @Autowired
    private lateinit var producerTemplate: ProducerTemplate

    @Value("\${gmail.username}")
    private lateinit var from: String

     @Transactional
    override fun createJobProposal(customerId: Long, professionalId: Long, jobOfferId: Long): JobProposalDTO {
        val customer = customerRepository.findById(customerId).getOrElse { throw EntityNotFoundException("The customer does not exist") }
        val professional = professionalRepository.findById(professionalId).getOrElse { throw EntityNotFoundException("The Professional doesn't exist") }
        val jobOffer = jobOfferRepository.findById(jobOfferId).getOrElse { throw EntityNotFoundException("The given Job Offer doesn't exist") }

        val jobProposal = JobProposal(jobOffer)

         customer.addJobProposal(jobProposal);
         professional.addJobProposal(jobProposal)

         logger.info("Created Job Proposal with ID #${jobProposal.proposalID}")
         kafkaTemplate.send("JOB_PROPOSAL-CREATE",jobProposal.toJobProposalKafkaDTO())
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
            var msgProf= ""
            if (!customerConfirm){
                proposal.status = ProposalStatus.DECLINED
                logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has declined Job Proposal #${proposal.proposalID}")
                msgProf =  "Dear Professional, the Customer for [ "+ proposal.jobOffer.description +" ] has declined the proposal. Best regards"
            }else{

                logger.info("Customer ${proposal.customer.contactInfo.name} ${proposal.customer.contactInfo.surname} has accepted Job Proposal #${proposal.proposalID}")
                msgProf =  "Dear Professional, the Customer for [ "+ proposal.jobOffer.description +" ] has accepted the proposal. Best regards"
            }
            kafkaTemplate.send("JOB_PROPOSAL-UPDATE",proposal.toJobProposalKafkaDTO())
            val professionalMail = proposal.professional!!.contactInfo.addresses
                .filter { it.addressType == AddressType.EMAIL }
                .map { it as Email } // Assuming EmailAddress is the correct type
                .firstOrNull()?.email
            val headers = mapOf(
                "From" to from,
                "To" to professionalMail,
                "Subject" to "Update Job Offer [ "+ proposal.jobOffer.description +" ]"
            )
            producerTemplate.sendBodyAndHeaders("seda:sendEmail", msgProf, headers)


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
            var msg = ""
            if (!professionalConfirm){
                logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has declined Job Proposal #${proposal.proposalID}")
                proposal.status = ProposalStatus.DECLINED
                msg =  "Dear Customer, the Professional proposed for [ "+ proposal.jobOffer.description +" ] has declined the proposal. Best regards"
            }

            else{
                proposal.status= ProposalStatus.ACCEPTED
                logger.info("Professional ${proposal.professional.contactInfo.name} ${proposal.professional.contactInfo.surname} has accepted Job Proposal #${proposal.proposalID}")
                msg =  "Dear Customer, the Professional proposed for [ "+ proposal.jobOffer.description +" ] has accepted the proposal. Best regards"
            }
            val customerMail = proposal.customer!!.contactInfo.addresses
                .filter { it.addressType == AddressType.EMAIL }
                .map { it as Email } // Assuming EmailAddress is the correct type
                .firstOrNull()?.email
            val headers = mapOf(
                "From" to from,
                "To" to customerMail,
                "Subject" to "Update Job Offer [ "+ proposal.jobOffer.description +" ]"
            )
            producerTemplate.sendBodyAndHeaders("seda:sendEmail", msg, headers)
            kafkaTemplate.send("JOB_PROPOSAL-UPDATE",proposal.toJobProposalKafkaDTO())
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

        kafkaTemplate.send("JOB_PROPOSAL-UPDATE",proposal.toJobProposalKafkaDTO())
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
        kafkaTemplate.send("JOB_PROPOSAL-UPDATE",proposal.toJobProposalKafkaDTO())
        return proposalRepository.save(proposal).toJobProposalDTO()
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger(JobOfferServiceImpl::class.java)
    }
}