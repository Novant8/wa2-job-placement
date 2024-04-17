package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@DiscriminatorValue("telephone")
class Telephone:Address() {

    lateinit var number : String


}