package it.polito.wa2.g07.crm.dtos.lab03

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.jpa.domain.Specification

@ParameterObject
data class JobOfferFilterDTO(
    @field:Parameter(description = "Filter by professional ID")
    val professionalId : Long ? = null,

    @field:Parameter(description = "Filter by customer ID")
    val customerId: Long ? = null,

    @field:Parameter(
        description = "Filter by status. Results will contain all job offers that are currently in any of the given statuses.",
        array = ArraySchema(schema = Schema(implementation = OfferStatus::class))
    )
    val status: List<String>? = null

) {
    fun toSpecification(): Specification<JobOffer> {

        var spec = Specification.where<JobOffer>(null)


        if (this.status!=null) {


            val offerList = mutableListOf<OfferStatus>()


            this.status.map { it-> OfferStatus.valueOf(it) }.toCollection(offerList)

            //val offerStatus = OfferStatus.entries.map{ it.toString() }
            //if (!offerStatus.contains(this.status.uppercase())) {
            //    throw InvalidParamsException("${this.status} is not a valid offer state. Valid offer states: $offerStatus")
            //}
            spec = spec.and { root, query, builder ->

                   builder.isTrue(
                       // root.get<OfferStatus>("status").`in`(OfferStatus.CREATED,OfferStatus.CANDIDATE_PROPOSAL),
                            root.get<OfferStatus>("status").`in`(offerList),

                       )

            }


        }
        if (this.professionalId!= null) {
            spec = spec.and { root, _, builder ->
                builder.equal(
                    root.get<Professional>("professional").get<String>("professionalId"),
                    this.professionalId.toString()
                )
            }
        }
        if (this.customerId!= null) {
            spec = spec.and { root, query, builder ->

                builder.equal(
                    root.get<Customer>("customer").get<String>("customerId"),
                    this.customerId.toString()
                )
            }
        }

        return spec
    }
}