package it.polito.wa2.g07.crm.entities.lab03

import it.polito.wa2.g07.crm.entities.lab02.Contact

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Id

import jakarta.persistence.*


enum class EmploymentState{
    UNEMPLOYED,
    EMPLOYED,
    NOT_AVAILABLE
}

@Entity
class Professional(
    @OneToOne
    var contactInfo: Contact,

    var location: String,

    @ElementCollection
    var skills : Set<String>,

    var daily_rate: Double,

    var employmentState: EmploymentState = EmploymentState.UNEMPLOYED,

    var notes: String? = null,
) {

    @Id
    @GeneratedValue
    var professionalId: Long = 0L

    @OneToMany(mappedBy = "professional")
    val jobOffers : MutableSet<JobOffer> = mutableSetOf()

}