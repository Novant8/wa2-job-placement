package it.polito.wa2.g07.crm.entities.lab03

import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.project.JobProposal
import jakarta.persistence.*

@Entity
class Customer (
    @OneToOne(cascade = [CascadeType.ALL])
    var contactInfo : Contact,

    var notes: String? = null
) {

    @Id
    @GeneratedValue
    var customerId: Long = 0L

    @OneToMany (mappedBy = "customer", cascade = [ CascadeType.REMOVE ])
    var placementHistory: MutableSet<JobOffer> = mutableSetOf()

    fun addPlacement (j: JobOffer){
        j.customer = this
        placementHistory.add(j)
    }

    fun removePlacement(j: JobOffer): Boolean {
        return placementHistory.remove(j)
    }

    @OneToMany(mappedBy = "customer")
    var jobProposals: MutableSet<JobProposal> = mutableSetOf()

    fun addJobProposal(jobProposal: JobProposal){
        jobProposals.add(jobProposal);
        jobProposal.customer = this
    }
}
