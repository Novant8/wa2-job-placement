package it.polito.wa2.g07.crm.entities.lab02

import it.polito.wa2.g07.crm.dtos.lab02.EmailDTO
import jakarta.persistence.*

@Entity
@DiscriminatorValue("email")
class Email(
    @Column(unique = true)
    var email: String
) : Address() {
    override val addressType: AddressType
        get() = AddressType.EMAIL

    override fun equals(other: Any?): Boolean {
        if(other is Email) return this.email == other.email
        if(other is EmailDTO) return this.email == other.email
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.email.hashCode()
    }
}