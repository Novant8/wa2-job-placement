package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*



@Entity
@DiscriminatorValue("dwelling")
public class Dwelling : Address() {

    lateinit var city: String
     var district: String? = null
     var country: String? = null

}