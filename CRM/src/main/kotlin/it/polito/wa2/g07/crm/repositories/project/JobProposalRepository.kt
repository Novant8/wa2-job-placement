package it.polito.wa2.g07.crm.repositories.project

import it.polito.wa2.g07.crm.entities.lab02.Dwelling
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.entities.project.JobProposal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JobProposalRepository:JpaRepository<JobProposal,Long> {

    fun findByJobOffer_OfferIdAndProfessional_ProfessionalId(idJobOffer : Long, idProfessional: Long): Optional<JobProposal>

    /*@Query("SELECT p FROM JobProposal p " +
            "WHERE p.jobOffer.offerId = :idJobOffer " +
            "AND p.professional.professionalId = :idProfessional ")
    fun findJobProposalByJobOfferAndProfessional(
        @Param("idJobOffer") idJobOffer: Long,
        @Param("idProfessional") idProfessional: Long,
    ): Optional<JobProposal>*/
}