package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.springframework.stereotype.Service

@Service
class ContactServiceImpl(private val contactRepository: ContactRepository) : ContactService{
}