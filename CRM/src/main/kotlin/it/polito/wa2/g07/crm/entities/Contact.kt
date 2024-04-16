package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*


enum class Category (category : String){
    CUSTOMER("Customer"),
    PROFESSIONAL("Professional"),
    UNKNOWN("nknown")
}

@Entity

class Contact {

    @Id
    @GeneratedValue
    var contactId: Long = 0

    var name: String?=null
    var surname: String?=null

    var SSN: String? = null

    lateinit var category : Category

    @ManyToMany
    @JoinTable(
        name = "contacts_addresses",
        joinColumns = [ JoinColumn(name = "contact_id") ],
        inverseJoinColumns = [ JoinColumn(name = "course_id") ])
    val addresses: MutableSet<Address> = mutableSetOf()

    fun addDwelling(d: Dwelling) {
        d.contacts.add(this)
        this.addresses.add(d)
    }

    fun addEmail(e: Email) {
        e.contacts.add(this)
        this.addresses.add(e)
    }

    fun addTelephone(t: Telephone) {
        t.contacts.add(this)
        this.addresses.add(t)
    }
}