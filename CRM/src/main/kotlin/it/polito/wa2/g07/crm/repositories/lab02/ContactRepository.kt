package it.polito.wa2.g07.crm.repositories.lab02

import it.polito.wa2.g07.crm.entities.lab02.ContactCategory

import it.polito.wa2.g07.crm.entities.lab02.Contact
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Repository
interface ContactRepository:JpaRepository<Contact,Long>, JpaSpecificationExecutor<Contact> {

    @Transactional(readOnly=true)
    fun findByUserId(userId: String): Optional<Contact>

    @Query("SELECT c FROM Contact c WHERE concat(c.name, ' ', c.surname) LIKE %:query%")
    fun findAllByFullNameLike(@Param("query") query: String, pageable: Pageable): Page<Contact>

    fun findAllBySsn(ssn: String, pageable: Pageable): Page<Contact>

    fun findAllByCategory(category: ContactCategory, pageable: Pageable): Page<Contact>

    @Query("SELECT e.contacts FROM Email e WHERE e.email = :email")
    fun findAllByEmail(@Param("email") e: String, pageable: Pageable): Page<Contact>

    @Query("SELECT t.contacts FROM Telephone t WHERE t.number = :telephone")
    fun findAllByTelephone(@Param("telephone") t: String, pageable: Pageable): Page<Contact>

    @Query("SELECT d.contacts FROM Dwelling d WHERE concat(d.street, ', ', d.city, '(', d.district, '), ', d.country) LIKE %:address%")
    fun findAllByDwellingLike(@Param("address") a: String, pageable: Pageable): Page<Contact>



}