package it.polito.wa2.g07.crm.entities.lab03

import jakarta.persistence.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

enum class OfferStatus {
    CREATED,
    SELECTION_PHASE,
    CANDIDATE_PROPOSAL,
    CONSOLIDATED,
    DONE,
    ABORTED
}
@Entity
class JobOffer(
    @ManyToOne
    var customer: Customer,

    @ElementCollection
    var requiredSkills: Set<String> = setOf(),

    var duration: Duration,

    var status: OfferStatus = OfferStatus.CREATED,

    var notes: String? = null,
) {

    companion object {
        const val PROFIT_MARGIN = 0.2
    }

    @Id
    @GeneratedValue
    var offerId: Long = 0L

    @ManyToOne
    var professional: Professional? = null
        set(professional) {
            professional?.jobOffers?.add(this)
            field = professional
        }

    val value: Double?
        get() = when(this.professional){
            null -> null
            //  null -> throw IllegalStateException("Value cannot be calculated if the job offer has no professional!")
            else -> this.duration.toDouble(DurationUnit.DAYS) * this.professional!!.daily_rate * PROFIT_MARGIN
        }


}