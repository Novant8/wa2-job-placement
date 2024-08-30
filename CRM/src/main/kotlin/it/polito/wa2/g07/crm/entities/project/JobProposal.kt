package it.polito.wa2.g07.crm.entities.project

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

enum class ProposalStatus {
   CREATED,
   ACCEPTED,
   DECLINED
};
@Entity
class JobProposal(
   @OneToOne
    var customer: Customer,

   @OneToOne
   var professional: Professional,

   @OneToOne
   var jobOffer: JobOffer,



) {
   @Id
   @GeneratedValue
   var proposalID : Long = 0L

   var customerConfirm : Boolean = false
   var documentId : Long? = null
   var status : ProposalStatus = ProposalStatus.CREATED



}