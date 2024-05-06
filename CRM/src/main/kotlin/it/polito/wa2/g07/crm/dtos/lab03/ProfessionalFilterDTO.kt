package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification

data class ProfessionalFilterDTO(
    val skills: Set<String> = setOf(),
    val location: String? = null,
    val employmentState: String? = null
) {
    fun toSpecification(): Specification<Professional> {
        var spec = Specification.where<Professional>(null)

        if(this.skills.isNotEmpty()) {
            spec = spec.and { root, query, builder ->
                val subquery = query.subquery(Long::class.java)
                val professional = subquery.from(Professional::class.java)
                val skills = professional.join<Professional, String>("skills", JoinType.INNER)
                subquery
                    .select(professional.get("professionalId"))
                    .where(
                        builder.or(*this.skills.map{ builder.like(builder.lower(skills), "%${it.lowercase()}%") }.toTypedArray())
                    )
                root.get<Long>("professionalId").`in`(subquery)
            }
        }

        if (!this.location.isNullOrBlank()) {
            spec = spec.and { root, _, builder ->
                builder.like(
                    builder.lower(root.get("location")),
                    "%${this.location.lowercase()}%"
                )
            }
        }

        if (!this.employmentState.isNullOrBlank()) {
            val employmentStates = EmploymentState.entries.map{ it.toString() }
            if (!employmentStates.contains(this.employmentState.uppercase())) {
                throw InvalidParamsException("${this.employmentState} is not a valid employment state. Valid employment states: $employmentStates")
            }
            spec = spec.and { root, _, builder ->
                builder.equal(
                    root.get<EmploymentState>("employmentState"),
                    EmploymentState.valueOf(this.employmentState.uppercase())
                )
            }
        }

        return spec
    }
}