package it.polito.wa2.g07.crm.dtos.lab02

import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import org.springframework.data.jpa.domain.Specification

data class ContactFilterDTO(
    val fullName: String? = null,
    val category: String? = null,
    val address: String? = null,
    val telephone: String? = null,
    val ssn: String? = null,
    val email: String? = null
) {
    fun toSpecification(): Specification<Contact> {
        var spec = Specification.where<Contact>(null)

        if (!this.fullName.isNullOrBlank()) {
            spec = spec.and { root, _, builder ->
                builder.like(
                    builder.lower(builder.concat(builder.concat(root.get("name"), " "), root.get("surname"))), // concat(name, " ", surname)
                    "%${this.fullName}%".lowercase()
                )
            }
        }

        if(!this.category.isNullOrBlank()) {
            if (!ContactCategory.entries.map { it.name }.contains(this.category.uppercase())) {
                throw InvalidParamsException("${this.category} is not a valid category. Valid categories: ${ContactCategory.entries}")
            }
            spec = spec.and { root, _, builder ->
                builder.equal(root.get<ContactCategory>("category"), ContactCategory.valueOf(this.category.uppercase()))
            }
        }

        if(!this.ssn.isNullOrBlank()) {
            spec = spec.and { root, _, builder ->
                builder.equal(root.get<String?>("ssn"), this.ssn.uppercase())
            }
        }

        if(!this.email.isNullOrBlank()) {
            spec = spec.and { root, query, builder ->
                val subquery = query.subquery(Email::class.java)
                val email = subquery.from(Email::class.java)
                subquery
                    .select(email)
                    .where(
                        builder.equal(email.get<String>("email"),this.email),
                        builder.isMember(root, email.get<Collection<Contact>>("contacts"))
                    )
                builder.exists(subquery)
            }
        }

        if(!this.telephone.isNullOrBlank()) {
            spec = spec.and { root, query, builder ->
                val subquery = query.subquery(Telephone::class.java)
                val telephone = subquery.from(Telephone::class.java)
                subquery
                    .select(telephone)
                    .where(
                        builder.equal(telephone.get<String>("number"), this.telephone),
                        builder.isMember(root, telephone.get<Collection<Contact>>("contacts"))
                    )
                builder.exists(subquery)
            }
        }

        if(!this.address.isNullOrBlank()) {
            spec = spec.and { root, query, builder ->
                val subquery = query.subquery(Dwelling::class.java)
                val dwelling = subquery.from(Dwelling::class.java)
                subquery
                    .select(dwelling)
                    .where(
                        builder.like(
                            /* concat(street, ", ", city, ", (", district ?: "", "), ", country ?: "") */
                            builder.lower(builder.concat(dwelling.get("street"), builder.concat(", ", builder.concat(dwelling.get("city"), builder.concat(", (", builder.concat(builder.coalesce(dwelling.get("district"), ""), builder.concat("), ", builder.coalesce(dwelling.get("country"), "")))))))),
                            "%${this.address}%".lowercase()
                        ),
                        builder.isMember(root, dwelling.get<Collection<Contact>>("contacts"))
                    )
                builder.exists(subquery)
            }
        }

        return spec
    }
}