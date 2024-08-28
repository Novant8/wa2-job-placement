package it.polito.wa2.g07.monitoring.entities

import jakarta.persistence.*


@Entity
data class JobOfferMonitoring (
    @Id
    var id:Long = 0,
    var description : String? = null,
    var customer: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    var requiredSkills: MutableSet<String> = mutableSetOf(),
    var duration: Long?= null,
    var offerStatus: String? = null,
    var notes: String? = null,
    var professional: String? = null,
    var value:Double? = null
){

}

