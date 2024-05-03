package it.polito.wa2.g07.crm.entities.lab02

import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import jakarta.persistence.*

@Entity
class Customer (
    @OneToOne
    var contactInfo : Contact,

    var notes: String? = null
) {

    @Id
    var customerId: Long = 0L

    @OneToMany (mappedBy = "customer")
    var placementHistory: MutableSet<JobOffer> = mutableSetOf()

    fun addPlacement (j: JobOffer){
        j.customer = this
        placementHistory.add(j)
    }

    fun removePlacement(j: JobOffer): Boolean {
        return placementHistory.remove(j)
    }
}
