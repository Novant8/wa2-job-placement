package it.polito.wa2.g07.crm.controllers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.ContactCategory
import it.polito.wa2.g07.crm.services.ContactService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ContactController::class)
class ContactControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var contactService: ContactService

    private val mockContactDTO = ContactDTO(
        1L,
        "Mario",
        "Rossi",
        ContactCategory.CUSTOMER,
        listOf(
            EmailDTO("mario.rossi@example.org"),
            TelephoneDTO("34242424242"),
            DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")
        ),
        "RSSMRA70A01L219K"
    )

    private val mockReducedContactDTO = ReducedContactDTO(
            mockContactDTO.id,
            mockContactDTO.name,
            mockContactDTO.surname,
            mockContactDTO.category
    )

    @Nested
    inner class GetContactTests {

        private val pageImpl = PageImpl(listOf(mockReducedContactDTO))
        private val query = "query"

        @BeforeEach
        fun initMocks() {
            every { contactService.getContacts(any(ContactFilterBy::class), any(String::class), any(Pageable::class)) } returns pageImpl
        }

        @Test
        fun getContacts_noParams() {
            mockMvc
                .perform(get("/API/contacts"))
                .andExpect{
                    status().isOk
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.NONE, "", PageRequest.of(0, 20)) }
                    jsonPath("$[0].id", mockReducedContactDTO.id)
                    jsonPath("$[1].name", mockReducedContactDTO.name)
                    jsonPath("$[1].surname", mockReducedContactDTO.surname)
                    jsonPath("$[1].name", mockReducedContactDTO.category.name)
                }
        }

        @Test
        fun getContacts_filterParam() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.FULL_NAME.name).param("q", query))
                .andExpect{
                    status().isOk
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.FULL_NAME, query, PageRequest.of(0, 20)) }
                    jsonPath("$[0].id", mockReducedContactDTO.id)
                    jsonPath("$[1].name", mockReducedContactDTO.name)
                    jsonPath("$[1].surname", mockReducedContactDTO.surname)
                    jsonPath("$[1].name", mockReducedContactDTO.category.name)
                }
        }

        @Test
        fun getContacts_filterParam_caseInsensitive() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.FULL_NAME.name.lowercase()).param("q", query))
                .andExpect{
                    status().isOk
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.FULL_NAME, query, PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_emptyQuery() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.FULL_NAME.name))
                .andExpect{
                    status().isBadRequest
                }
        }

        @Test
        fun getContacts_invalidFilter() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", "i am not a valid filter"))
                .andExpect{
                    status().isBadRequest
                }
        }

        @Test
        fun getContacts_validCategory() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.CATEGORY.name).param("q", ContactCategory.CUSTOMER.name))
                .andExpect{
                    status().isOk
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name, PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_validCategory_caseInsensitive() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.CATEGORY.name).param("q", ContactCategory.CUSTOMER.name.lowercase()))
                .andExpect{
                    status().isOk
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name.lowercase(), PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_invalidCategory() {
            mockMvc
                .perform(get("/API/contacts").param("filterBy", ContactFilterBy.CATEGORY.name).param("q", "i am not a valid category"))
                .andExpect{
                    status().isBadRequest
                }
        }

    }

}