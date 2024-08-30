package it.polito.wa2.g07.crm.repositories.project

import it.polito.wa2.g07.crm.entities.project.JobProposal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobProposalRepository:JpaRepository<JobProposal,Long> {
}