package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import org.apache.juli.logging.Log

interface JobProposalService {

    fun createJobProposal(customerId: Long, professionalId: Long, jobOfferId: Long) : JobProposalDTO
    fun searchJobProposalById(idProposal:Long): JobProposalDTO
    fun searchJobProposalByJobOfferAndProfessional(idJobOffer : Long, idProfessional: Long): JobProposalDTO
}