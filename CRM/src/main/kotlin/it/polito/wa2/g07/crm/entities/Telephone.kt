package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@DiscriminatorValue("telephone")
class Telephone {
    @Id
    @GeneratedValue
    var telephoneId : Long = 0

    lateinit var number : String


}