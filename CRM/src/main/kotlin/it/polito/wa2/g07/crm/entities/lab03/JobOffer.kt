package it.polito.wa2.g07.crm.entities.lab03


import it.polito.wa2.g07.crm.entities.lab03.Customer
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

import jakarta.persistence.*

import com.fasterxml.jackson.annotation.JsonIgnore

import kotlin.time.Duration
import kotlin.time.DurationUnit

enum class OfferStatus {
    CREATED {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf(SELECTION_PHASE, ABORTED)
    },
    SELECTION_PHASE {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf(CANDIDATE_PROPOSAL, ABORTED)
    },
    CANDIDATE_PROPOSAL {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf(CONSOLIDATED, SELECTION_PHASE, ABORTED)
    },
    CONSOLIDATED {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf(DONE, SELECTION_PHASE, ABORTED)
    },
    DONE {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf(SELECTION_PHASE)
    },
    ABORTED {
        override val compatibleStatuses: Collection<OfferStatus>
            get() = setOf()
    };

    protected abstract val compatibleStatuses: Collection<OfferStatus>
    fun canUpdateTo(status: OfferStatus) = this.compatibleStatuses.contains(status)
}

@Entity
class JobOffer(


    @ElementCollection(fetch = FetchType.EAGER)
    var requiredSkills: MutableSet<String> = mutableSetOf(),

    var duration: Long,

    var description: String,

    var status: OfferStatus = OfferStatus.CREATED,

    var notes: String? = null,
) {
    @ManyToOne
    lateinit var customer: Customer

    companion object {
        const val PROFIT_MARGIN = 0.2
    }

    @Id
    @GeneratedValue
    var offerId: Long = 0L

    @ManyToOne(cascade = [CascadeType.ALL])
    var professional: Professional? = null
        set(professional) {
            professional?.jobOffers?.add(this)
            field = professional
        }

    val value: Double?
        get() = when(this.professional){
            null -> null
            //  null -> throw IllegalStateException("Value cannot be calculated if the job offer has no professional!")
            else -> this.duration * this.professional!!.dailyRate * PROFIT_MARGIN
        }


}