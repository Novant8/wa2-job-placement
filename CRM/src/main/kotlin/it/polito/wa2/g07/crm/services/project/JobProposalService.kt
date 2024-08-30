package it.polito.wa2.g07.crm.services.project

import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.entities.lab03.Professional

interface JobProposalService {

    fun createJobProposal(customerId: Long, professionalId: Long, jobOfferId: Long) : JobProposalDTO
}