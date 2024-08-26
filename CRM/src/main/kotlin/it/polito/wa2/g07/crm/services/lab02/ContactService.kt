package it.polito.wa2.g07.crm.services.lab02

import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.AddressType


import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication


interface ContactService {
    fun create (contact: CreateContactDTO): ContactDTO

    fun getContacts(filterDTO: ContactFilterDTO, pageable: Pageable): Page<ReducedContactDTO>
    fun getContactById (contactId : Long): ContactDTO
    fun getContactByUserId(userId: String) : ContactDTO

    fun insertAddress(contactId: Long, addressDTO: AddressDTO): ContactDTO
    fun updateAddress(contactId: Long, addressId: Long, addressDTO: AddressDTO): ContactDTO
    fun updateAddress(contactId: Long, addressId: Long, addressDTO: AddressDTO, authentication: Authentication?): ContactDTO
    fun deleteAddress(contactId: Long, addressId: Long, addressType: AddressType)

    fun updateName(contactId: Long, nameDTO: NameDTO): ContactDTO
    fun updateSurname(contactId: Long, surnameDTO: SurnameDTO): ContactDTO
    fun updateSSN(contactId: Long, ssnDTO: SsnDTO): ContactDTO
}