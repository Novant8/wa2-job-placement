package it.polito.wa2.g07.crm.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

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

    lateinit var name: String
    lateinit var surname: String

    var SSN: String? = null

    lateinit var category : Category

    @ManyToMany
    @JoinTable(name = "contact_address",
        joinColumns = [ JoinColumn(name="contact_id") ],
        inverseJoinColumns = [ JoinColumn(name = "address_id") ]
    )
    val addresses: MutableSet<Address> = mutableSetOf()

    @ManyToMany
    @JoinTable(name = "contact_email",
            joinColumns = [ JoinColumn(name="contact_id") ],
            inverseJoinColumns = [ JoinColumn(name = "email_id") ]
    )
    val emails: MutableSet<Email> = mutableSetOf()

    @ManyToMany
    @JoinTable(name = "contact_telephone",
            joinColumns = [ JoinColumn(name="contact_id") ],
            inverseJoinColumns = [ JoinColumn(name = "telephone_id") ]
    )
    val telephones: MutableSet<Telephone> = mutableSetOf()

    @OneToMany(mappedBy = "sender")
    val messages: MutableSet<Message> = mutableSetOf()

    fun addAddress(a: Address) {
        a.contacts.add(this)
        this.addresses.add(a)
    }

    fun addEmail(e: Email) {
        e.contacts.add(this)
        this.emails.add(e)
    }
    
    fun addTelephone(t: Telephone) {
        this.telephones.add(t)
        t.contacts.add(this)
    }
}