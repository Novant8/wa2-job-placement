package it.polito.wa2.g07.crm.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

enum class ContactCategory{
    CUSTOMER,
    PROFESSIONAL,
    UNKNOWN
}

@Entity

class Contact(
        var name: String,
        var surname: String,
        var category : ContactCategory,
        var ssn: String? = null
) {

    @Id
    @GeneratedValue
    var contactId: Long = 0

    @ManyToMany (cascade = [CascadeType.ALL])
    @JsonManagedReference
    var addresses: MutableSet<Address> = mutableSetOf()

    fun addAddress (a:Address){
        addresses.add(a)

    }
    fun removeAddress (a:Address){
        addresses.remove(a)

    }

}