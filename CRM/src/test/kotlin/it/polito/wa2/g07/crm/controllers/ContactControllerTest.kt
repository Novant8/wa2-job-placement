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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

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
                .get("/API/contacts")
                .andExpect {
                    status{ isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.NONE, "", PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedContactDTO.id) }
                        jsonPath("$.content[0].name") { value(mockReducedContactDTO.name) }
                        jsonPath("$.content[0].surname") { value(mockReducedContactDTO.surname) }
                    }
                }
        }

        @Test
        fun getContacts_filterParam() {
            mockMvc
                .get("/API/contacts") {
                    queryParam("filterBy", ContactFilterBy.FULL_NAME.name)
                    queryParam("q", query)
                }.andExpect{
                    status{ isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.FULL_NAME, query, PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedContactDTO.id) }
                        jsonPath("$.content[0].name") { value(mockReducedContactDTO.name) }
                        jsonPath("$.content[0].surname") { value(mockReducedContactDTO.surname) }
                    }
                }
        }

        @Test
        fun getContacts_filterParam_caseInsensitive() {
            mockMvc
                .get("/API/contacts"){
                    queryParam("filterBy", ContactFilterBy.FULL_NAME.name.lowercase())
                    queryParam("q", query)
                }
                .andExpect{
                    status{ isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.FULL_NAME, query, PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_emptyQuery() {
            mockMvc
                .get("/API/contacts"){
                    queryParam("filterBy", ContactFilterBy.FULL_NAME.name)
                }
                .andExpect{
                    status { isBadRequest() }
                }
        }

        @Test
        fun getContacts_invalidFilter() {
            mockMvc
                .get("/API/contacts") {
                    queryParam("filterBy", "i am not a valid filter")
                }
                .andExpect{
                    status { isBadRequest() }
                }
        }

        @Test
        fun getContacts_validCategory() {
            mockMvc
                .get("/API/contacts"){
                    queryParam("filterBy", ContactFilterBy.CATEGORY.name)
                    queryParam("q", ContactCategory.CUSTOMER.name)
                }
                .andExpect{
                    status { isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name, PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_validCategory_caseInsensitive() {
            mockMvc
                .get("/API/contacts"){
                    queryParam("filterBy", ContactFilterBy.CATEGORY.name)
                    queryParam("q", ContactCategory.CUSTOMER.name.lowercase())
                }
                .andExpect{
                    status { isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterBy.CATEGORY, ContactCategory.CUSTOMER.name.lowercase(), PageRequest.of(0, 20)) }
                }
        }

        @Test
        fun getContacts_invalidCategory() {
            mockMvc
                .get("/API/contacts") {
                    queryParam("filterBy", ContactFilterBy.CATEGORY.name)
                    queryParam("q", "i am not a valid category")
                }
                .andExpect {
                    status { isBadRequest() }
                .andExpect{
                    status().isBadRequest
                }
        }

    }

}