package it.polito.wa2.g07.crm.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Address {
    @Id
    @GeneratedValue
    var addressId: Long = 0

    lateinit var street: String
    lateinit var city: String
    lateinit var district: String
    lateinit var country: String

    @ManyToMany(mappedBy = "addresses")
    @JsonBackReference
    val contacts: MutableSet<Contact> = mutableSetOf()
}