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
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ContactServiceImpl(private val contactRepository: ContactRepository, private val addressRepository: AddressRepository) : ContactService{
    @Transactional
    override fun create(contact: CreateContactDTO): ReducedContactDTO {
        return contactRepository.save(contact.toEntity()).toReducedContactDTO()
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
            ContactFilterBy.SSN ->          contactRepository.findAllBySSN(query, pageable)
            ContactFilterBy.EMAIL ->        contactRepository.findAllByEmail(query, pageable)
            ContactFilterBy.TELEPHONE ->    contactRepository.findAllByTelephone(query, pageable)
            ContactFilterBy.ADDRESS ->      contactRepository.findAllByDwellingLike(query, pageable)
            ContactFilterBy.CATEGORY ->     contactRepository.findAllByCategory(ContactCategory.valueOf(query), pageable)
        }
        return result.map { it.toReducedContactDTO() }
    }
    @Transactional
    override fun getContactById(contactId: Long): ContactDTO {
        val contactOpt = contactRepository.findById(contactId)

        if (!contactOpt.isPresent){
            throw EntityNotFoundException("Contact not found with ID: $contactId")
        }
        val contact = contactOpt.get()
        logger.info("Edited Document {} ", contact.toContactDto())
        return contact.toContactDto()
    }

    @Transactional
    override fun insertEmail(id: Long, value: String) {

        val contactOpt = contactRepository.findById(id)

        if (!contactOpt.isPresent){
            throw EntityNotFoundException("Contact not found with ID : $id  ")
        }
        val email = Email(value)
        val contact = contactOpt.get()
        contact.addAddress(email)
    }
@Transactional
    override fun deleteEmail(contactId: Long, emailId: Long) {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with ID: $contactId") }

        val email = addressRepository.findById(emailId)

        if (email.isPresent && email.get() is Email){
            if (contact.addresses.contains(email.get())) {

                contact.removeAddress(email.get())
                addressRepository.deleteById(emailId)

            } else {
                throw InvalidParamsException("Email with ID $emailId is not associated with Contact with ID $contactId")
            }
        } else {
            throw InvalidParamsException("Address with ID $emailId is not an email or it is not present")
        }


    }
@Transactional
    override fun updateEmail(contactId: Long, emailId: Long, emailValue: String): ContactDTO {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with ID: $contactId") }

        val emailOpt = addressRepository.findById(emailId)

        if (emailOpt.isPresent && emailOpt.get() is Email){
            if (contact.addresses.contains(emailOpt.get())) {

                var email:Email = emailOpt.get() as Email
                email.email= emailValue

                addressRepository.save(email )
                return contact.toContactDto()

            } else {
                throw InvalidParamsException("Email with ID $emailId is not associated with Contact with ID $contactId")
            }
        } else {
            throw InvalidParamsException("Address with ID $emailId is not an email or it is not present")
        }
    }
@Transactional
    override fun updateTelephone(contactId: Long, telephoneId: Long, phoneNumber: String): ContactDTO {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with ID: $contactId") }

        val telephoneOpt = addressRepository.findById(telephoneId)

        if (telephoneOpt.isPresent && telephoneOpt.get() is Telephone){
            if (contact.addresses.contains(telephoneOpt.get())) {

                var telephone:Telephone = telephoneOpt.get() as Telephone
                telephone.number = phoneNumber

                addressRepository.save(telephone )
                return contact.toContactDto()

            } else {
                throw InvalidParamsException("Telephone Number with ID $telephoneId is not associated with Contact with ID $contactId")
            }
        } else {
            throw InvalidParamsException("Address with ID $telephoneId is not an telephone or it is not present")
        }
    }
    @Transactional
    override fun updateDwelling(contactId: Long, dwellingId: Long, street: String?, city: String?, district: String?, country: String?): ContactDTO {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with ID: $contactId") }

        val dwellingOpt = addressRepository.findById(dwellingId)

        if (dwellingOpt.isPresent && dwellingOpt.get() is Dwelling){
            if (contact.addresses.contains(dwellingOpt.get())) {

                var dwelling:Dwelling = dwellingOpt.get() as Dwelling
                dwelling.street= street?:""
                dwelling.city= city?:""
                dwelling.district= district?:""
                dwelling.country= country?:""

                addressRepository.save(dwelling )
                return contact.toContactDto()

            } else {
                throw InvalidParamsException("Dwelling with ID $dwellingId is not associated with Contact with ID $contactId")
            }
        } else {
            throw InvalidParamsException("Address with ID $dwellingId is not an Dwelling or it is not present")
        }
    }

    companion object{
        val logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)
    }
}
