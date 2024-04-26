package it.polito.wa2.g07.crm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.ContactCategory
import it.polito.wa2.g07.crm.exceptions.ContactNotFoundException
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.services.ContactService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(ContactController::class)
class ContactControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var contactService: ContactService

    private val mockEmailDTO = EmailDTO("mario.rossi@example.org")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")
    private val mockContactDTO = ContactDTO(
        1L,
        "Mario",
        "Rossi",
        ContactCategory.CUSTOMER,
        listOf(mockEmailDTO, mockTelephoneDTO, mockDwellingDTO),
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
                }
        }

    }

    @Nested
    inner class PostContactTests {

        private val jsonMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        @BeforeEach
        fun initMocks() {
            every { contactService.create(any(CreateContactDTO::class)) } answers { firstArg<CreateContactDTO>().toEntity().toContactDto() }
            every { contactService.insertEmail(any(Long::class), any(String::class)) } throws ContactNotFoundException("Contact does not exist")
            every { contactService.insertEmail(mockContactDTO.id, any(String::class)) } returns Unit
            every { contactService.insertEmail(mockContactDTO.id, mockEmailDTO.email) } throws DuplicateAddressException("Mail already associated to contact")
        }

        @Test
        fun saveContact_noAddresses() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                "VRDLGU70A01L219G",
                addresses = listOf()
            )

            mockMvc
                .post("/API/contacts") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(createContactDTO)
                }
                .andExpect{
                    status { isCreated() }
                    content {
                        jsonPath("$.name") { value(createContactDTO.name) }
                        jsonPath("$.surname") { value(createContactDTO.surname) }
                        jsonPath("$.category") { value(createContactDTO.category) }
                        jsonPath("$.ssn") { value(createContactDTO.ssn) }
                        jsonPath("$.addresses") { isEmpty() }
                    }
                }
        }

        @Test
        fun saveContact_noSSN() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                null,
                addresses = listOf()
            )

            mockMvc
                .post("/API/contacts") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(createContactDTO)
                }
                .andExpect{
                    status { isCreated() }
                    content {
                        jsonPath("$.name") { value(createContactDTO.name) }
                        jsonPath("$.surname") { value(createContactDTO.surname) }
                        jsonPath("$.category") { value(createContactDTO.category) }
                        jsonPath("$.ssn") { doesNotExist() }
                        jsonPath("$.addresses") { isEmpty() }
                    }
                }
        }

        @Test
        fun saveContact_withAddresses() {
            val mailDTO = EmailDTO("luigi.verdi@example.org")
            val telephoneDTO = TelephoneDTO("34798989898")
            val dwellingDTO = DwellingDTO("Via Roma, 19", "Torino", "TO", "IT")
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL.name,
                "VRDLGU70A01L219G",
                addresses = listOf(mailDTO, telephoneDTO, dwellingDTO)
            )

            mockMvc
                .post("/API/contacts") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(createContactDTO)
                }
                .andExpect{
                    status { isCreated() }
                    content {
                        jsonPath("$.name") { value(createContactDTO.name) }
                        jsonPath("$.surname") { value(createContactDTO.surname) }
                        jsonPath("$.category") { value(createContactDTO.category) }
                        jsonPath("$.ssn") { value(createContactDTO.ssn) }
                        jsonPath("$.addresses") { isArray() }
                        jsonPath("$.addresses[0].email") { value(mailDTO.email) }
                        jsonPath("$.addresses[1].phoneNumber") { value(telephoneDTO.phoneNumber)}
                        jsonPath("$.addresses[2].street") { value(dwellingDTO.street) }
                        jsonPath("$.addresses[2].city") { value(dwellingDTO.city) }
                        jsonPath("$.addresses[2].district") { value(dwellingDTO.district) }
                        jsonPath("$.addresses[2].country") { value(dwellingDTO.country) }
                    }
                }
        }

        @Test
        fun saveContact_invalidName() {
            for (name in listOf(null, "")) {
                val createContactDTO = CreateContactDTO(
                    name,
                    "Verdi",
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    addresses = listOf()
                )

                mockMvc
                    .post("/API/contacts") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(createContactDTO)
                    }
                    .andExpect{
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun saveContact_invalidSurname() {
            for (surname in listOf(null, "")) {
                val createContactDTO = CreateContactDTO(
                    "Luigi",
                    surname,
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    addresses = listOf()
                )

                mockMvc
                    .post("/API/contacts") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(createContactDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun saveContact_invalidCategory() {
            val createContactDTO = CreateContactDTO(
                "Luigi",
                "Verdi",
                "I am not valid",
                "VRDLGU70A01L219G",
                addresses = listOf()
            )

            mockMvc
                .post("/API/contacts") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(createContactDTO)
                }
                .andExpect{
                    status { isCreated() }
                    content {
                        jsonPath("$.category") { value(ContactCategory.UNKNOWN.name) }
                    }
                }
        }

        @Test
        fun insertMail_success() {
            val id = mockContactDTO.id
            val mailJson = "{ \"email\": \"mario.rossi2@gmail.com\" }"
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mailJson
                }
                .andExpect{
                    status { isCreated() }
                }
        }

        @Test
        fun insertMail_contactNotFound() {
            val id = mockContactDTO.id + 1
            val mailJson = "{ \"email\": \"mario.rossi2@gmail.com\" }"
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mailJson
                }
                .andExpect{
                    status { isNotFound() }
                }
        }

        @Test
        fun insertMail_duplicateMail() {
            val id = mockContactDTO.id
            val mailJson = "{ \"email\": \"${mockEmailDTO.email}\" }"
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mailJson
                }
                .andExpect{
                    status { isNotModified() }
                }
        }

    }

}