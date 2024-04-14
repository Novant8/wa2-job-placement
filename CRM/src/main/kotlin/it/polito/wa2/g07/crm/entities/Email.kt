package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*

@Entity

class Email{
    @Id
    @GeneratedValue
    var emailId: Long = 0

    lateinit var email: String
}