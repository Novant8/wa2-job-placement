package it.polito.wa2.g07.crm.entities

import it.polito.wa2.g07.crm.dtos.EmailDTO
import jakarta.persistence.*

@Entity
@DiscriminatorValue("email")
class Email(
    @Column(unique = true)
    var email: String
) : Address()