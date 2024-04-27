package it.polito.wa2.g07.crm.services

import io.mockk.*
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
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

    init {
        mockContact.contactId = 1L
        mockMail.id = 1L
        mockTelephone.id = 2L
        mockDwelling.id = 3L
        mockContact.addresses = mutableSetOf(
            mockMail,
            mockTelephone,
            mockDwelling
        )
    }

    private val contactRepository = mockk<ContactRepository>()
    private val addressRepository = mockk<AddressRepository>()
    private val service = ContactServiceImpl(contactRepository, addressRepository)

    @Nested
    inner class GetContactTests {

        private val pageImpl = PageImpl(listOf(mockContact))
        private val pageReq = PageRequest.of(1, 10)

        @BeforeEach
        fun initMocks() {
            every { contactRepository.findAll(any(Pageable::class)) } returns pageImpl
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
        fun getContacts_noneFilter() {
            val result = service.getContacts(ContactFilterBy.NONE, "", pageReq)

            val expectedResult = PageImpl(listOf(mockContact.toReducedContactDTO()))
            verify(exactly = 1) { contactRepository.findAll(pageReq) }
            assertEquals(result, expectedResult)
        }

        @Test
        fun getContacts_stringFilters() {
            val filterFunctionsMap: Map<ContactFilterBy, (String, Pageable) -> Page<Contact>> = mapOf(
                ContactFilterBy.FULL_NAME to contactRepository::findAllByFullNameLike,
                ContactFilterBy.SSN to contactRepository::findAllBySsn,
                ContactFilterBy.EMAIL to contactRepository::findAllByEmail,
                ContactFilterBy.TELEPHONE to contactRepository::findAllByTelephone,
                ContactFilterBy.ADDRESS to contactRepository::findAllByDwellingLike
            )

            filterFunctionsMap.forEach { (filter, func) ->
                val query = "query"
                val result = service.getContacts(filter, query, pageReq)

                val expectedResult = PageImpl(listOf(mockContact.toReducedContactDTO()))
                verify(exactly = 1) { func(query, pageReq) }
                assertEquals(result, expectedResult)
            }
        }

        @Test
        fun getContacts_categoryFilter() {
            val result = service.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name, pageReq)

            val expectedResult = PageImpl(listOf(mockContact.toReducedContactDTO()))
            verify(exactly = 1) { contactRepository.findAllByCategory(ContactCategory.CUSTOMER, pageReq) }
            assertEquals(result, expectedResult)
        }

        @Test
        fun getContacts_categoryFilter_caseInsensitive() {
            val result = service.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name.lowercase(), pageReq)

            val expectedResult = PageImpl(listOf(mockContact.toReducedContactDTO()))
            verify(exactly = 1) { contactRepository.findAllByCategory(ContactCategory.CUSTOMER, pageReq) }
            assertEquals(result, expectedResult)
        }

        @Test
        fun getContactById_success() {
            val result = service.getContactById(mockContact.contactId)
            assertEquals(result, mockContact.toContactDto())
        }

        @Test
        fun getContactById_notFound() {
            assertThrows<EntityNotFoundException> {
                service.getContactById(mockContact.contactId + 1)
            }
        }
    }

    @Nested
    inner class PostContactTests {

        val contactSlot = slot<Contact>()

        @BeforeEach
        fun initMocks() {
            every { contactRepository.save(capture(contactSlot)) } answers { firstArg<Contact>() }
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every { contactRepository.findById(mockContact.contactId) } returns Optional.of(mockContact)
            every { addressRepository.findMailAddressByMail(any(String::class)) } returns Optional.empty()
            every { addressRepository.findMailAddressByMail(mockMail.email) } returns Optional.of(mockMail)
            every { addressRepository.findTelephoneAddressByTelephoneNumber(any(String::class)) } returns Optional.empty()
            every { addressRepository.findTelephoneAddressByTelephoneNumber(mockTelephone.number) } returns Optional.of(mockTelephone)
            every { addressRepository.findDwellingAddressByStreet(any(String::class), any(String::class), any(String::class), any(String::class)) } returns Optional.empty()
            every { addressRepository.findDwellingAddressByStreet(mockDwelling.street, mockDwelling.city, mockDwelling.district, mockDwelling.country) } returns Optional.of(mockDwelling)
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
                EmailResponseDTO(0L,"luigi.verdi@example.org"),
                TelephoneResponseDTO(0L,"34798989898"),
                DwellingResponseDTO(0L,"Via Roma, 19", "Torino", "TO", "IT")
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
                EmailResponseDTO(1L,mockMail.email),
                TelephoneResponseDTO(2L,mockTelephone.number),
                DwellingResponseDTO(3L,mockDwelling.street, mockDwelling.city, mockDwelling.district?:"", mockDwelling.country?:"")
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
                EmailResponseDTO(0L,"luigi.verdi@example.org"),
                TelephoneResponseDTO(0L,"34798989898"),
                DwellingResponseDTO(0L,"Via Roma, 19", "Torino", "TO", "IT")
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
        }

        @Disabled("TODO: REPLACE insertEmail WITH insertAddress")
        @Test
        fun insertEmail_success() {
            val id = mockContact.contactId
            val newMail = "mario.rossi@gmail.com"
            //service.insertEmail(id, newMail)

            verify { contactRepository.save(any(Contact::class)) }
            assertNotNull(contactSlot.captured.addresses.find { it is Email && it.email == newMail })
        }

        @Disabled("TODO: REPLACE insertEmail WITH insertAddress")
        @Test
        fun insertEmail_existingMail() {
            val existingMail = "existing.mail@example.org"
            val existingMailEntity = Email(existingMail)
            existingMailEntity.id = 2L
            every { addressRepository.findMailAddressByMail(existingMail) } returns Optional.of(existingMailEntity)

            val id = mockContact.contactId
            // service.insertEmail(id, existingMail)

            verify { contactRepository.save(any(Contact::class)) }
            assertEquals(contactSlot.captured.addresses.find { it is Email && it.email == existingMail }?.id, existingMailEntity.id)
        }

        @Disabled("TODO: REPLACE insertEmail WITH insertAddress")
        @Test
        fun insertEmail_mailAlreadyAssociatedToContact() {
            val id = mockContact.contactId
            val newMail = mockMail.email
            assertThrows<DuplicateAddressException> {
                // service.insertEmail(id, newMail)
            }
            verify { contactRepository.save(any(Contact::class)) wasNot called }
        }

        @Disabled("TODO: REPLACE insertEmail WITH insertAddress")
        @Test
        fun insertEmail_invalidUser() {
            val id = mockContact.contactId + 1
            val newMail = "mario.rossi@gmail.com"
            assertThrows<EntityNotFoundException> {
                // service.insertEmail(id, newMail)
            }
            verify { contactRepository.save(any(Contact::class)) wasNot called }
        }

    }
}