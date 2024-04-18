package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*



@Entity
@DiscriminatorValue("dwelling")
 class Dwelling : Address() {

     var street: String? = null
     var city: String? = null
     var district: String? = null
     var country: String? = null

}