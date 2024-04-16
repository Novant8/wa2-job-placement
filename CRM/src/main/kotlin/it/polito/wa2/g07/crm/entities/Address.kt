package it.polito.wa2.g07.crm.entities


import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
 open class  Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    open var id: Long=0
/*
    @ManyToMany (mappedBy = "addresses")
    @JsonBackReference
    val contacts : MutableSet<Contact> = mutableSetOf()

    fun addContact (c:Contact){
        contacts.add(c)
        c.addresses.add(this)
    }
    fun removeContact (c:Contact){
        contacts.remove(c)
        c.addresses.remove(this)
    }
    */
}


/*
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
}*/