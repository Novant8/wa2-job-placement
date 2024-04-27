package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*


import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*
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
            throw EntityNotFoundException("Contact not found with ID: $contactId")
        }
        val contact = contactOpt.get()
        logger.info("Edited Document {} ", contact.toContactDto())
        return contact.toContactDto()
    }

    @Transactional
    override fun insertAddress(contactId: Long, addressDTO: AddressDTO): ContactDTO {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }
        return insertAddress(contact, addressDTO)
    }

    @Transactional
    fun insertAddress(contact: Contact, addressDTO: AddressDTO): ContactDTO {
        val duplicateAddress = contact.addresses.find { it is Email && it == addressDTO } != null
        if (duplicateAddress) {
            throw DuplicateAddressException("The given address is already associated to contact #${contact.contactId}")
        }

        val address = when(addressDTO) {
            is EmailDTO -> addressRepository.findMailAddressByMail(addressDTO.email)
            is TelephoneDTO -> addressRepository.findTelephoneAddressByTelephoneNumber(addressDTO.phoneNumber)
            is DwellingDTO -> addressRepository.findDwellingAddressByStreet(addressDTO.street, addressDTO.city, addressDTO.district, addressDTO.country)
        }.getOrDefault(addressDTO.toEntity())

        addressRepository.save(address)
        contact.addAddress(address)
        return contactRepository.save(contact).toContactDto()
    }

    @Transactional
    override fun deleteAddress(contactId: Long, addressId: Long, addressType: AddressType) {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }

        val address = addressRepository.findById(addressId).orElseThrow{ EntityNotFoundException("Address with ID $addressId is not present") }
        if (address.addressType != addressType) {
            throw InvalidParamsException("Address with ID $addressId is not of type $addressType")
        }

        if (!contact.removeAddress(address)) {
            throw InvalidParamsException("Address with ID $addressId is not associated with Contact with ID $contactId")
        }
    }

    @Transactional
    override fun updateAddress(contactId: Long, addressId: Long, addressDTO: AddressDTO): ContactDTO {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }

        val addressOpt = addressRepository.findById(addressId)
        if (!addressOpt.isPresent || addressOpt.get().addressType != addressDTO.addressType) {
            throw InvalidParamsException("Address with ID $addressId is not of type ${addressDTO.addressType} or it is not present")
        }

        val address = addressOpt.get()
        if (address.contacts.size > 1) {
            // Old address is associated to multiple contacts:
            // Remove old address from contact and add/associate the new one to it
            contact.removeAddress(address)
            return insertAddress(contact, addressDTO)
        } else {
            // The address is associated to only one contact:
            // Edit the existing information
            when(addressDTO) {
                is EmailDTO -> {
                    address as Email
                    address.email = addressDTO.email
                }
                is TelephoneDTO -> {
                    address as Telephone
                    address.number = addressDTO.phoneNumber
                }
                is DwellingDTO -> {
                    address as Dwelling
                    address.street = addressDTO.street
                    address.city = addressDTO.city
                    address.district = addressDTO.district
                    address.country = addressDTO.country
                }
            }
            addressRepository.save(address)
            return contactRepository.save(contact).toContactDto()
        }
    }

    companion object{
        val logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)
    }
}
