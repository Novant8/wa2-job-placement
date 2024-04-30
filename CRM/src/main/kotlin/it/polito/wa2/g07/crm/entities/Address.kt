package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*




@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
abstract class Address {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    open var id: Long=0

    @ManyToMany(mappedBy = "addresses", cascade = [CascadeType.ALL])
    open var contacts: MutableSet<Contact> = mutableSetOf()

    @OneToMany(mappedBy = "sender", cascade = [CascadeType.ALL])
    open var messages: MutableSet<Message> = mutableSetOf()

    abstract val addressType: AddressType

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other === this) return true
        if(other is Address) return this.id == other.id
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}