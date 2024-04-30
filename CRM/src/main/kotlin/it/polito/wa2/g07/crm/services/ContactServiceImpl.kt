package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*


import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import jakarta.transaction.Transactional
import org.slf4j.Logger
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

        val contactDTO = contactRepository.save(newContact).toContactDto()
        logger.info("Created Contact #${contactDTO.id}.")
        return contactDTO
    }

@Transactional
    override fun getContacts(
        filterDTO: ContactFilterDTO,
        pageable: Pageable
    ): Page<ReducedContactDTO> {
        return contactRepository.findAll(filterDTO.toSpecification(), pageable).map { it.toReducedContactDTO() }
    }

    @Transactional
    override fun getContactById(contactId: Long): ContactDTO {
        val contactOpt = contactRepository.findById(contactId)

        if (!contactOpt.isPresent){
            throw EntityNotFoundException("Contact not found with ID: $contactId")
        }
        val contact = contactOpt.get()
        return contact.toContactDto()
    }

    @Transactional
    override fun insertAddress(contactId: Long, addressDTO: AddressDTO): ContactDTO {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }
        return insertAddress(contact, addressDTO)
    }

    @Transactional
    fun insertAddress(contact: Contact, addressDTO: AddressDTO): ContactDTO {
        val duplicateAddress = contact.addresses.find { it == addressDTO } != null
        if (duplicateAddress) {
            throw DuplicateAddressException("The given address is already associated to contact #${contact.contactId}")
        }

        var address = when(addressDTO) {
            is EmailDTO -> addressRepository.findMailAddressByMail(addressDTO.email)
            is TelephoneDTO -> addressRepository.findTelephoneAddressByTelephoneNumber(addressDTO.phoneNumber)
            is DwellingDTO -> addressRepository.findDwellingAddressByStreet(addressDTO.street, addressDTO.city, addressDTO.district, addressDTO.country)
        }.getOrDefault(addressDTO.toEntity())

        address = addressRepository.save(address)
        contact.addAddress(address)
        logger.info("Added Address #${address.id} to Contact #${contact.contactId}.")
        return contactRepository.save(contact).toContactDto()
    }

    @Transactional
    override fun deleteAddress(contactId: Long, addressId: Long, addressType: AddressType) {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }
        val address = addressRepository.findById(addressId).orElseThrow{ EntityNotFoundException("Address with ID $addressId is not present") }
        return deleteAddress(contact, address, addressType)
    }

    @Transactional
    fun deleteAddress(contact: Contact, address: Address, addressType: AddressType) {
        if (address.addressType != addressType) {
            throw InvalidParamsException("Address with ID ${address.id} is not of type $addressType")
        }
        if (!contact.removeAddress(address)) {
            throw InvalidParamsException("Address with ID ${address.id} is not associated with Contact with ID ${contact.contactId}")
        }
        logger.info("Removed Address #${address.id} from Contact #${contact.contactId}.")
        contactRepository.save(contact)
    }

    @Transactional
    override fun updateAddress(contactId: Long, addressId: Long, addressDTO: AddressDTO): ContactDTO {
        val contact = contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact not found with ID : $contactId") }
        val address = addressRepository.findById(addressId).orElseThrow{ EntityNotFoundException("Address with ID $addressId is not present") }
        if(address == addressDTO) {
            throw DuplicateAddressException("The given address is already associated with the contact #$contactId")
        }

        deleteAddress(contact, address, address.addressType)
        return insertAddress(contact, addressDTO)
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)
    }
}
