package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.ContactDTO
import it.polito.wa2.g07.crm.dtos.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.toContactDto
import it.polito.wa2.g07.crm.dtos.toEntity
import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.springframework.stereotype.Service

@Service
class ContactServiceImpl(private val contactRepository: ContactRepository) : ContactService{
    override fun create(contact: CreateContactDTO): ContactDTO {
       return contactRepository.save(contact.toEntity()).toContactDto()
    }

}