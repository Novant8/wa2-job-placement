package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@DiscriminatorValue("dwelling")
 class Dwelling(
        var street: String,
        var city: String,
        var district: String?,
        var country: String?
 ) : Address()