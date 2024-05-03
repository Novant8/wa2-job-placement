package it.polito.wa2.g07.crm.entities

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Id

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
    var skill : Set<String>,

    var daily_rate: Double,

    var employmentState: EmploymentState = EmploymentState.UNEMPLOYED,

    var notes: String? = null,
) {

    @Id
    var professionalId: Long = 0L

    @OneToMany(mappedBy = "professional")
    val jobOffers : MutableSet<JobOffer> = mutableSetOf()

}