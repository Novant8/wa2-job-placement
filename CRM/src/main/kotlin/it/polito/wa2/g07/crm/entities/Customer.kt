package it.polito.wa2.g07.crm.entities

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

    fun removePlacement(j:JobOffer): Boolean {
        return placementHistory.remove(j)
    }
}
