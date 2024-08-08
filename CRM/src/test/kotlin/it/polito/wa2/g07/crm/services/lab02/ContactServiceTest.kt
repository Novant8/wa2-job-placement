package it.polito.wa2.g07.crm.services.lab02

import io.mockk.*
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.AddressRepository
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.services.project.KeycloakUserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class ContactServiceTest {

    private val mockContact: Contact = Contact(
        "Mario",
        "Rossi",
        ContactCategory.CUSTOMER,
        "RSSMRA70A01L219K"
    )
    private val mockMail = Email("mario.rossi@example.org")
    private val mockTelephone = Telephone("34242424242")
    private val mockDwelling = Dwelling("Via Roma, 18", "Torino", "TO", "IT")

    fun initMockContact() {
        mockMail.id = 1L
        mockMail.email = "mario.rossi@example.org"
        mockMail.contacts.add(mockContact)

        mockTelephone.id = 2L
        mockTelephone.number = "34242424242"
        mockTelephone.contacts.add(mockContact)

        mockDwelling.id = 3L
        mockDwelling.street = "Via Roma, 18"
        mockDwelling.city = "Torino"
        mockDwelling.district = "TO"
        mockDwelling.country = "IT"
        mockDwelling.contacts.add(mockContact)

        mockContact.addresses = mutableSetOf(
            mockMail,
            mockTelephone,
            mockDwelling
        )

        mockContact.contactId = 1L
    }

    init {
        initMockContact()
    }

    private val contactRepository = mockk<ContactRepository>()
    private val addressRepository = mockk<AddressRepository>()
    private val keycloakUserService = mockk<KeycloakUserService>()
    private val service = ContactServiceImpl(contactRepository, addressRepository, keycloakUserService)

    @Nested
    inner class GetContactTests {

        private val pageImpl = PageImpl(listOf(mockContact))
        private val pageReq = PageRequest.of(1, 10)

        @BeforeEach
        fun initMocks() {
            every { contactRepository.findAll(any(), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByFullNameLike(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllBySsn(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByEmail(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByTelephone(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByDwellingLike(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByCategory(any(ContactCategory::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every { contactRepository.findById(mockContact.contactId) } returns Optional.of(mockContact)
        }

        @Test
        fun getContacts_success() {
            val contactFilterDTO = ContactFilterDTO()
            val result = service.getContacts(contactFilterDTO, pageReq)

            val expectedResult = pageImpl.map { it.toReducedContactDTO() }
            verify(exactly = 1) { contactRepository.findAll(any(), pageReq) }
            assertEquals(result, expectedResult)
        }
    }

    @Nested
    inner class CreateContactTests {

        private val contactSlot = slot<Contact>()
        private val addressSlot = slot<Address>()

        @BeforeEach
        fun initMocks() {
            every { contactRepository.save(capture(contactSlot)) } answers { firstArg<Contact>() }
            every { contactRepository.delete(any(Contact::class)) } returns Unit
            every { addressRepository.save(capture(addressSlot)) } answers { firstArg<Address>() }
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every { contactRepository.findById(mockContact.contactId) } returns Optional.of(mockContact)
            every { addressRepository.findMailAddressByMail(any(String::class)) } returns Optional.empty()
            every { addressRepository.findMailAddressByMail(mockMail.email) } returns Optional.of(mockMail)
            every { addressRepository.findTelephoneAddressByTelephoneNumber(any(String::class)) } returns Optional.empty()
            every { addressRepository.findTelephoneAddressByTelephoneNumber(mockTelephone.number) } returns Optional.of(mockTelephone)
            every {
                addressRepository.findDwellingAddressByStreet(
                    any(String::class),
                    any(String::class),
                    any(String::class),
                    any(String::class)
                )
            } returns Optional.empty()
            every {
                addressRepository.findDwellingAddressByStreet(
                    mockDwelling.street,
                    mockDwelling.city,
                    mockDwelling.district,
                    mockDwelling.country
                )
            } returns Optional.of(mockDwelling)
        }

        @Test
        fun create_noSSN_noAddresses() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                null,
                listOf()
            )

            val result = service.create(createContactDTO)

            val expectedDTO = ContactDTO(
                0L,
                createContactDTO.name!!,
                createContactDTO.surname!!,
                ContactCategory.PROFESSIONAL,
                listOf(),
                createContactDTO.ssn
            )
            assertEquals(result, expectedDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun create_SSN_noAddresses() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                "VRDLGU70A01L219G",
                addresses = listOf()
            )

            val result = service.create(createContactDTO)

            val expectedDTO = ContactDTO(
                0L,
                createContactDTO.name!!,
                createContactDTO.surname!!,
                ContactCategory.PROFESSIONAL,
                listOf(),
                createContactDTO.ssn
            )
            assertEquals(result, expectedDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun create_withAddresses() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                "VRDLGU70A01L219G",
                listOf(
                    EmailDTO("luigi.verdi@example.org"),
                    TelephoneDTO("34798989898"),
                    DwellingDTO("Via Roma, 19", "Torino", "TO", "IT")
                )
            )

            val result = service.create(createContactDTO)
            val expectedAddresses = listOf(
                EmailResponseDTO(0L, "luigi.verdi@example.org"),
                TelephoneResponseDTO(0L, "34798989898"),
                DwellingResponseDTO(0L, "Via Roma, 19", "Torino", "TO", "IT")
            )

            val expectedDTO = ContactDTO(
                0L,
                createContactDTO.name!!,
                createContactDTO.surname!!,
                ContactCategory.PROFESSIONAL,
                expectedAddresses,
                createContactDTO.ssn
            )
            assertEquals(result, expectedDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun create_existingAddresses() {
            val createContactDTO = CreateContactDTO(
                "Maria",
                "Rossi",
                ContactCategory.PROFESSIONAL.name,
                "VRDLGU70A01L219G",
                listOf(
                    EmailDTO(mockMail.email),
                    TelephoneDTO(mockTelephone.number),
                    DwellingDTO(mockDwelling.street, mockDwelling.city, mockDwelling.district, mockDwelling.country)
                )
            )

            val result = service.create(createContactDTO)
            val expectedAddresses = listOf(
                EmailResponseDTO(1L, mockMail.email),
                TelephoneResponseDTO(2L, mockTelephone.number),
                DwellingResponseDTO(
                    3L,
                    mockDwelling.street,
                    mockDwelling.city,
                    mockDwelling.district ?: "",
                    mockDwelling.country ?: ""
                )
            )

            val expectedDTO = ContactDTO(
                0L,
                createContactDTO.name!!,
                createContactDTO.surname!!,
                ContactCategory.PROFESSIONAL,
                expectedAddresses,
                createContactDTO.ssn
            )
            assertEquals(result, expectedDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun create_invalidCategory() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                "I am not valid",
                "VRDLGU70A01L219G",
                listOf(
                    EmailDTO("luigi.verdi@example.org"),
                    TelephoneDTO("34798989898"),
                    DwellingDTO("Via Roma, 19", "Torino", "TO", "IT")
                )
            )

            val result = service.create(createContactDTO)
            val expectedAddresses = listOf(
                EmailResponseDTO(0L, "luigi.verdi@example.org"),
                TelephoneResponseDTO(0L, "34798989898"),
                DwellingResponseDTO(0L, "Via Roma, 19", "Torino", "TO", "IT")
            )

            val expectedDTO = ContactDTO(
                0L,
                createContactDTO.name!!,
                createContactDTO.surname!!,
                ContactCategory.UNKNOWN,
                expectedAddresses,
                createContactDTO.ssn
            )
            assertEquals(result, expectedDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun create_deleteAutoGeneratedContact() {
            val existingMail = Email("existing.mail@example.org")
            existingMail.contacts = mutableSetOf(Contact("Auto-generated", "Auto-generated", ContactCategory.UNKNOWN))

            every { addressRepository.findMailAddressByMail(existingMail.email) } returns Optional.of(existingMail)

            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                null,
                listOf(existingMail.toAddressDTO())
            )

            service.create(createContactDTO)
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 1) { contactRepository.delete(any(Contact::class)) }
        }
    }

    @Nested
    inner class AddressManagementTests {

        @BeforeEach
        fun initMocks() {
            every { keycloakUserService.changeUserName(any(String::class), any(String::class)) } returns Unit
            every { keycloakUserService.changeUserSurname(any(String::class), any(String::class)) } returns Unit
            every { keycloakUserService.changeUserSurname(any(String::class), any(String::class)) } returns Unit
            every { contactRepository.save(any(Contact::class)) } answers { firstArg<Contact>() }
            every { addressRepository.save(any(Address::class)) } answers { firstArg<Address>() }
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every { contactRepository.findById(mockContact.contactId) } returns Optional.of(mockContact)
            every { contactRepository.delete(any(Contact::class)) } returns Unit
            every { addressRepository.findMailAddressByMail(any(String::class)) } returns Optional.empty()
            every { addressRepository.findMailAddressByMail(mockMail.email) } returns Optional.of(mockMail)
            every { addressRepository.findTelephoneAddressByTelephoneNumber(any(String::class)) } returns Optional.empty()
            every { addressRepository.findTelephoneAddressByTelephoneNumber(mockTelephone.number) } returns Optional.of(mockTelephone)
            every {
                addressRepository.findDwellingAddressByStreet(
                    any(String::class),
                    any(String::class),
                    any(String::class),
                    any(String::class)
                )
            } returns Optional.empty()
            every {
                addressRepository.findDwellingAddressByStreet(
                    mockDwelling.street,
                    mockDwelling.city,
                    mockDwelling.district,
                    mockDwelling.country
                )
            } returns Optional.of(mockDwelling)
            every { addressRepository.delete(any(Address::class)) } returns Unit

            every { addressRepository.findById(any(Long::class)) } returns Optional.empty()
            for (address in mockContact.addresses) {
                every { addressRepository.findById(address.id) } returns Optional.of(address)
            }
        }

        @BeforeEach
        fun callInitMockContact() {
            initMockContact()
        }

        @Test
        fun insertAddress_new() {
            val addressDTOs = listOf(
                EmailDTO("mario.rossi@gmail.com"),
                TelephoneDTO("3424242424"),
                DwellingDTO("Via Garibaldi, 20", "Torino", "TO", "IT")
            )

            for (addressDTO in addressDTOs) {
                val result = service.insertAddress(mockContact.contactId, addressDTO)

                assertTrue(mockContact.addresses.contains(addressDTO.toEntity()))
                assertEquals(result, mockContact.toContactDto())
                verify { contactRepository.save(any(Contact::class)) }
                verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }

                mockContact.addresses.remove(addressDTO.toEntity())
            }
        }

        @Test
        fun insertAddress_existing() {
            val existingMail = "existing.mail@example.org"
            val existingPhone = "34298989898"
            val existingDwellingStreet = "Via Esistente, 42"
            val existingDwellingCity = "Torino"
            val existingDwellingDistrict = "TO"
            val existingDwellingCountry = "IT"
            val existingEntities = listOf(
                Email(existingMail),
                Telephone(existingPhone),
                Dwelling(existingDwellingStreet, existingDwellingCity, existingDwellingDistrict, existingDwellingCountry)
            )
            existingEntities[0].id = 4L
            existingEntities[1].id = 5L
            existingEntities[2].id = 6L

            every { addressRepository.findMailAddressByMail(existingMail) } returns Optional.of(existingEntities[0] as Email)
            every { addressRepository.findTelephoneAddressByTelephoneNumber(existingPhone) } returns Optional.of(existingEntities[1] as Telephone)
            every { addressRepository.findDwellingAddressByStreet(existingDwellingStreet, existingDwellingCity, existingDwellingDistrict, existingDwellingCountry) } returns Optional.of(existingEntities[2] as Dwelling)

            for (address in existingEntities) {
                val result = service.insertAddress(mockContact.contactId, address.toAddressDTO())

                assertTrue(mockContact.addresses.contains(address))
                assertEquals(result, mockContact.toContactDto())
                verify { contactRepository.save(any(Contact::class)) }
                verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
            }
        }

        @Test
        fun insertAddress_alreadyAssociated() {
            for (address in mockContact.addresses) {
                assertThrows<DuplicateAddressException> {
                    service.insertAddress(mockContact.contactId, address.toAddressDTO())
                }
                verify(exactly = 0) { contactRepository.save(any(Contact::class)) }
                verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
            }
        }

        @Test
        fun insertAddress_invalidContact() {
            assertThrows<EntityNotFoundException> {
                service.insertAddress(mockContact.contactId + 1, EmailDTO("mario.rossi@gmail.com"))
            }
            verify(exactly = 0) { contactRepository.save(any(Contact::class)) }
            verify(exactly = 0) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun insertAddress_deleteAutoGeneratedContact() {
            val existingMail = Email("existing.mail@example.org")
            existingMail.contacts = mutableSetOf(Contact("Auto-generated", "Auto-generated", ContactCategory.UNKNOWN))

            every { addressRepository.findMailAddressByMail(existingMail.email) } returns Optional.of(existingMail)

            service.insertAddress(mockContact.contactId, existingMail.toAddressDTO())
            verify { contactRepository.save(any(Contact::class)) }
            verify(exactly = 1) { contactRepository.delete(any(Contact::class)) }
        }

        @Test
        fun deleteAddress_singleAssociation() {
            service.deleteAddress(mockContact.contactId, mockMail.id, mockMail.addressType)

            assertFalse(mockContact.addresses.contains(mockMail))
            verify { contactRepository.save(mockContact) }
            verify { addressRepository.delete(mockMail) }
        }

        @Test
        fun deleteAddress_multipleAssociations() {
            val otherContact = Contact(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL
            )
            otherContact.addAddress(mockMail)

            service.deleteAddress(mockContact.contactId, mockMail.id, mockMail.addressType)

            assertFalse(mockContact.addresses.contains(mockMail))
            verify { contactRepository.save(mockContact) }
            verify(exactly = 0) { addressRepository.delete(mockMail) }
        }

        @Test
        fun deleteAddress_invalidContact() {
            assertThrows<EntityNotFoundException> {
                service.deleteAddress(mockContact.contactId + 1, mockMail.id, mockMail.addressType)
            }
        }

        @Test
        fun deleteAddress_invalidAddress() {
            assertThrows<EntityNotFoundException> {
                service.deleteAddress(mockContact.contactId, 42L, AddressType.EMAIL)
            }
        }

        @Test
        fun deleteAddress_inconsistentType() {
            assertThrows<InvalidParamsException> {
                service.deleteAddress(mockContact.contactId, mockMail.id, AddressType.TELEPHONE)
            }
        }

        @Test
        fun deleteAddress_notAssociated() {
            val existingMail = Email("existing.mail@example.org")
            existingMail.id = 98L
            every { addressRepository.findById(existingMail.id) } returns Optional.of(existingMail)

            assertThrows<InvalidParamsException> {
                service.deleteAddress(mockContact.contactId, existingMail.id, existingMail.addressType)
            }
        }

        @Test
        fun updateAddress_singleAssociation() {
            val oldMail = Email(mockMail.email)
            oldMail.id = mockMail.id
            val newMailDTO = EmailDTO("new.mail@example.org")

            val result = service.updateAddress(mockContact.contactId, mockMail.id, newMailDTO)

            assertTrue(mockContact.addresses.contains(newMailDTO.toEntity()))
            assertFalse(mockContact.addresses.contains(oldMail))
            assertEquals(result, mockContact.toContactDto())
            verify { contactRepository.save(mockContact) }
            verify { addressRepository.delete(mockMail) }
        }

        @Test
        fun updateAddress_multipleAssociations() {
            val otherContact = Contact(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL
            )
            otherContact.addAddress(mockMail)

            val oldMail = Email(mockMail.email)
            oldMail.id = mockMail.id
            val newMailDTO = EmailDTO("new.mail@example.org")

            val result = service.updateAddress(mockContact.contactId, mockMail.id, newMailDTO)

            assertTrue(mockContact.addresses.contains(newMailDTO.toEntity()))
            assertFalse(mockContact.addresses.contains(oldMail))
            assertTrue(otherContact.addresses.contains(oldMail))
            assertEquals(result, mockContact.toContactDto())
            verify { contactRepository.save(mockContact) }
            verify(exactly = 0) { addressRepository.delete(mockMail) }
        }

        @Test
        fun updateAddress_newAddressAlreadyExists() {
            val existingMail = Email("existing.mail@example.org")
            existingMail.id = 98L
            every { addressRepository.findById(existingMail.id) } returns Optional.of(existingMail)

            val oldMail = Email(mockMail.email)
            oldMail.id = mockMail.id

            val result = service.updateAddress(mockContact.contactId, mockMail.id, existingMail.toAddressDTO())

            assertTrue(mockContact.addresses.contains(existingMail))
            assertFalse(mockContact.addresses.contains(oldMail))
            assertEquals(result, mockContact.toContactDto())
            verify { contactRepository.save(mockContact) }
        }

        @Test
        fun updateAddress_alreadyAssociated() {
            val oldMail = Email(mockMail.email)
            oldMail.id = mockMail.id
            val existingMail = Email("existing.mail@example.org")
            mockContact.addAddress(existingMail)

            assertThrows<DuplicateAddressException> {
                service.updateAddress(mockContact.contactId, mockMail.id, existingMail.toAddressDTO())
            }
        }

        @Test
        fun updateAddress_noModifications() {
            assertThrows<DuplicateAddressException> {
                service.updateAddress(mockContact.contactId, mockMail.id, mockMail.toAddressDTO())
            }
        }

        @Test
        fun updateAddress_invalidContact() {
            val newMailDTO = EmailDTO("new.mail@example.org")
            assertThrows<EntityNotFoundException> {
                service.updateAddress(mockContact.contactId + 1, mockMail.id, newMailDTO)
            }
        }

        @Test
        fun updateAddress_invalidAddress() {
            val newMailDTO = EmailDTO("new.mail@example.org")
            assertThrows<EntityNotFoundException> {
                service.updateAddress(mockContact.contactId + 1, 42L, newMailDTO)
            }
        }
    }
}