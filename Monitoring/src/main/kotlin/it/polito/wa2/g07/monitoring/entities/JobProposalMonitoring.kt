package it.polito.wa2.g07.monitoring.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class JobProposalMonitoring (
    @Id
    var proposalId:Long= 0,
    var status :String? = null,
    var documentId : Long? = null,
    var professionalSignedContract : Long? = null,
    var customer : String? = null,
    var professional : String? = null,
    var jobOffer: String? = null
){
}