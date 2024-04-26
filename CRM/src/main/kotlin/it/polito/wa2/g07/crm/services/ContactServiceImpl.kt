package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.ContactDTO
import it.polito.wa2.g07.crm.dtos.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.toContactDto
import it.polito.wa2.g07.crm.dtos.toEntity


import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.ContactCategory
import it.polito.wa2.g07.crm.entities.Dwelling
import it.polito.wa2.g07.crm.entities.Email
import it.polito.wa2.g07.crm.entities.Telephone
import it.polito.wa2.g07.crm.exceptions.ContactNotFoundException
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrDefault

@Service
class ContactServiceImpl(
    private val contactRepository: ContactRepository,
    private val addressRepository: AddressRepository
) : ContactService{
    @Transactional
    override fun create(contact: CreateContactDTO): ContactDTO {
        val newContact = contact.toEntity()

        // Search for existing addresses in the DB
        newContact.addresses =
            newContact.addresses.map {
                when (it) {
                    is Email -> addressRepository.findMailAddressByMail(it.email).getOrDefault(it)
                    is Telephone -> addressRepository.findTelephoneAddressByTelephoneNumber(it.number).getOrDefault(it)
                    is Dwelling -> addressRepository.findDwellingAddressByStreet(it.street, it.city, it.district, it.country).getOrDefault(it)
                    else -> error("Address is not an email, telephone or dwelling")
                }
            }.toMutableSet()

        return contactRepository.save(newContact).toContactDto()
    }

@Transactional
    override fun getContacts(
        filterBy: ContactFilterBy,
        query: String,
        pageable: Pageable
    ): Page<ReducedContactDTO> {
        val result = when (filterBy) {
            ContactFilterBy.NONE ->         contactRepository.findAll(pageable)
            ContactFilterBy.FULL_NAME ->    contactRepository.findAllByFullNameLike(query, pageable)
            ContactFilterBy.SSN ->          contactRepository.findAllBySsn(query, pageable)
            ContactFilterBy.EMAIL ->        contactRepository.findAllByEmail(query, pageable)
            ContactFilterBy.TELEPHONE ->    contactRepository.findAllByTelephone(query, pageable)
            ContactFilterBy.ADDRESS ->      contactRepository.findAllByDwellingLike(query, pageable)
            ContactFilterBy.CATEGORY ->     contactRepository.findAllByCategory(ContactCategory.valueOf(query.uppercase()), pageable)
        }
        return result.map { it.toReducedContactDTO() }
    }
    @Transactional
    override fun getContactById(contactId: Long): ContactDTO {
        val contactOpt = contactRepository.findById(contactId)

        if (!contactOpt.isPresent){
            throw ContactNotFoundException("Contact not found ")
        }
        val contact = contactOpt.get()
        logger.info("Edited Document {} ", contact.toContactDto())
        return contact.toContactDto()
    }

    @Transactional
    override fun insertEmail(id: Long, value: String) {
        val contactOpt = contactRepository.findById(id)
        if (!contactOpt.isPresent){
            throw ContactNotFoundException("Contact not found ")
        }
        val contact = contactOpt.get()

        val duplicateEmail = contact.addresses.find { it is Email && it.email == value } != null
        if (duplicateEmail) {
            throw DuplicateAddressException("The mail $value is already associated to contact #$id")
        }

        val email = addressRepository.findMailAddressByMail(value).getOrDefault(Email(value))
        contact.addAddress(email)
        contactRepository.save(contact)
    }

    companion object{
        val logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)
    }
}
