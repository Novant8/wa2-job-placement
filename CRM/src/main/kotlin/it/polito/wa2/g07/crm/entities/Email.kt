package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity
@DiscriminatorValue("email")
class Email:Address(){
    lateinit var email: String
}