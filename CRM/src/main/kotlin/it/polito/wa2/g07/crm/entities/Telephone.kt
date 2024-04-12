package it.polito.wa2.g07.crm.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Telephone {
    @Id
    @GeneratedValue
    var telephoneId : Long = 0

    lateinit var number : String

    @ManyToMany(mappedBy = "telephones")
    val contacts: MutableSet<Contact> = mutableSetOf()
}