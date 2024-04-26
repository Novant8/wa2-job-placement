package it.polito.wa2.g07.crm.repositories

import it.polito.wa2.g07.crm.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AddressRepository: JpaRepository<Address, Long>{
    @Query("SELECT e FROM Email e WHERE e.email = :email")
    fun findMailAddressByMail(@Param("email") a:String) : Optional<Email>

    @Query("SELECT t FROM Telephone t WHERE t.number = :telephone")
    fun findTelephoneAddressByTelephoneNumber(@Param("telephone") a:String) : Optional<Telephone>

    @Query("SELECT d FROM Dwelling d " +
            "WHERE d.street = :street " +
            "AND d.city = :city " +
            "AND (d.district is null  or d.district = :district )" +
            "AND (d.country is null or  d.country = :country)")
    fun findDwellingAddressByStreet(
        @Param("street") street: String,
        @Param("city") city: String,
        @Param("district") district: String?,
        @Param("country") country: String?,
    ): Optional<Dwelling>

}

