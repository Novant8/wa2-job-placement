package it.polito.wa2.g07.crm.entities.lab02

import it.polito.wa2.g07.crm.dtos.lab02.TelephoneDTO
import jakarta.persistence.*

@Entity
@DiscriminatorValue("telephone")
class Telephone(
    @Column(unique = true)
    var number : String
): Address() {
    override val addressType: AddressType
        get() = AddressType.TELEPHONE

    override fun equals(other: Any?): Boolean {
        if(other is Telephone) return this.number == other.number
        if(other is TelephoneDTO) return this.number == other.phoneNumber
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.number.hashCode()
    }
}