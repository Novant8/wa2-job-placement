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



}

