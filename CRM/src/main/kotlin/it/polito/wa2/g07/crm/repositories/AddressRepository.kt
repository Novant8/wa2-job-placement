package it.polito.wa2.g07.crm.repositories

import it.polito.wa2.g07.crm.entities.Address
import it.polito.wa2.g07.crm.entities.Contact
import it.polito.wa2.g07.crm.entities.Dwelling
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository: JpaRepository<Address, Long>
interface DwellingRepository: JpaRepository<Dwelling, Long>


