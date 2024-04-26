package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@DiscriminatorValue("dwelling")
@Table(uniqueConstraints = [
    UniqueConstraint(columnNames = [ "street", "city", "district", "country" ] )
])
 class Dwelling(
        var street: String,
        var city: String,
        var district: String?,
        var country: String?
 ) : Address()