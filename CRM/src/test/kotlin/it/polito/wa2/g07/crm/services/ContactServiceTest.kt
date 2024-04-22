package it.polito.wa2.g07.crm.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.toContactDto
import it.polito.wa2.g07.crm.dtos.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.exceptions.ContactNotFoundException
import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
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
    init {
        mockContact.contactId = 1L
        mockContact.addresses = mutableSetOf(
            Email("mario.rossi@example.org"),
            Telephone("34242424242"),
            Dwelling("Via Roma, 18", "Torino", "TO", "IT")
        )
    }

    private val contactRepository = mockk<ContactRepository>()
    private val service = ContactServiceImpl(contactRepository)

    @Nested
    inner class GetContactTests {

        private val pageImpl = PageImpl(listOf(mockContact))
        private val pageReq = PageRequest.of(1, 10)

        @BeforeEach
        fun initMocks() {
            every { contactRepository.findAll(any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllByFullNameLike(any(String::class), any(Pageable::class)) } returns pageImpl
            every { contactRepository.findAllBySSN(any(String::class), any(Pageable::class)) } returns pageImpl
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
                ContactFilterBy.SSN to contactRepository::findAllBySSN,
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
            assertThrows<ContactNotFoundException> {
                service.getContactById(mockContact.contactId + 1)
            }
        }
    }
}