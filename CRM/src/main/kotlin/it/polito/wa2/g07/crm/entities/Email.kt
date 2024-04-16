package it.polito.wa2.g07.crm.entities


import jakarta.persistence.*

@Entity
@DiscriminatorValue("email")
class Email:Address(){
    lateinit var email: String
}
/*
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Email {
    @Id
    @GeneratedValue
    var emailId :Long = 0

    lateinit var email : String

    @ManyToMany(mappedBy = "emails")
    @JsonBackReference
    val contacts: MutableSet<Contact> = mutableSetOf()
}*/