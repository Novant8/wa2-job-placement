package it.polito.wa2.g07.crm.entities.lab02

import it.polito.wa2.g07.crm.dtos.lab02.DwellingDTO
import jakarta.persistence.*

@Entity
@DiscriminatorValue("dwelling")
@Table(uniqueConstraints = [
    UniqueConstraint(columnNames = [ "street", "city", "district", "country" ] )
])
 class Dwelling(
        var street: String,
        var city: String,
        var district: String?,
        var country: String?
 ) : Address() {
    override val addressType: AddressType
        get() = AddressType.DWELLING

    override fun equals(other: Any?): Boolean {
        if(other is Dwelling)
            return this.street == other.street
                    && this.city == other.city
                    && this.district == other.district
                    && this.country == other.country
        if(other is DwellingDTO)
            return this.street == other.street
                    && this.city == other.city
                    && this.district == other.district
                    && this.country == other.country
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return arrayOf(this.street, this.city, this.district, this.country).contentHashCode()
    }
 }