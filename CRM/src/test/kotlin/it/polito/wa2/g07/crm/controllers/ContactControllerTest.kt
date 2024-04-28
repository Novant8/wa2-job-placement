package it.polito.wa2.g07.crm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
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

    private val mockEmailDTO = EmailResponseDTO(1L,"mario.rossi@example.org")
    private val mockTelephoneDTO = TelephoneResponseDTO(2L,"34242424242")
    private val mockDwellingDTO = DwellingResponseDTO(3L,"Via Roma, 18", "Torino", "TO", "IT")
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

        private val invalidEmailDTOs = listOf(
            EmailDTO(""),
            EmailDTO(" "),
            EmailDTO("invalid")
        )

        private val invalidTelephoneDTOs = listOf(
            TelephoneDTO(""),
            TelephoneDTO(" "),
            TelephoneDTO("invalid")
        )

        private val invalidDwellingDTOs = listOf(
            DwellingDTO("", "Torino", "TO", "IT"),
            DwellingDTO(" ", "Torino", "TO", "IT"),
            DwellingDTO("Via Garibaldi, 42", "", "TO", "IT"),
            DwellingDTO("Via Garibaldi, 42", " ", "TO", "IT"),
            DwellingDTO("Via Garibaldi, 42", "Torino", "", "IT"),
            DwellingDTO("Via Garibaldi, 42", "Torino", " ", "IT"),
            DwellingDTO("Via Garibaldi, 42", "Torino", "TO", ""),
            DwellingDTO("Via Garibaldi, 42", "Torino", "TO", " ")
        )

        @BeforeEach
        fun initMocks() {
            every { contactService.create(any(CreateContactDTO::class)) } answers { firstArg<CreateContactDTO>().toEntity().toContactDto() }
            every { contactService.insertAddress(any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Contact does not exist")
            every { contactService.insertAddress(mockContactDTO.id, any(AddressDTO::class)) } answers {
                val addressResponseDTO = when(val addressDTO = secondArg<AddressDTO>()) {
                    is EmailDTO -> EmailResponseDTO(4L, addressDTO.email)
                    is TelephoneDTO -> TelephoneResponseDTO(4L, addressDTO.phoneNumber)
                    is DwellingDTO -> DwellingResponseDTO(4L, addressDTO.street, addressDTO.city, addressDTO.district, addressDTO.country)
                }
                ContactDTO(
                    mockContactDTO.id,
                    mockContactDTO.name,
                    mockContactDTO.surname,
                    mockContactDTO.category,
                    listOf(mockContactDTO.addresses, listOf(addressResponseDTO)).flatten(),
                    mockContactDTO.ssn
                )
            }
            every { contactService.insertAddress(mockContactDTO.id, mockEmailDTO.toAddressDTO()) } throws DuplicateAddressException("Mail already associated to contact")
            every { contactService.insertAddress(mockContactDTO.id, mockTelephoneDTO.toAddressDTO()) } throws DuplicateAddressException("Phone already associated to contact")
            every { contactService.insertAddress(mockContactDTO.id, mockDwellingDTO.toAddressDTO()) } throws DuplicateAddressException("Dwelling already associated to contact")
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
        fun saveContact_invalidAddresses() {
            val invalidAddresses = invalidEmailDTOs + invalidTelephoneDTOs + invalidDwellingDTOs

            for (address in invalidAddresses) {
                val createContactDTO = CreateContactDTO(
                    "Luigi",
                    "Verdi",
                    "I am not valid",
                    "VRDLGU70A01L219G",
                    addresses = listOf(address)
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
        fun insertMail_success() {
            val id = mockContactDTO.id
            val emailDTO = EmailDTO("mario.rossi2@gmail.com")
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(emailDTO)
                }
                .andExpect{
                    status { isCreated() }
                }
        }

        @Test
        fun insertMail_invalidMail() {
            for (emailDTO in invalidEmailDTOs) {
                val id = mockContactDTO.id
                mockMvc
                    .post("/API/contacts/$id/email") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(emailDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun insertMail_contactNotFound() {
            val id = mockContactDTO.id + 1
            val emailDTO = EmailDTO("mario.rossi2@gmail.com")
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(emailDTO)
                }
                .andExpect{
                    status { isNotFound() }
                }
        }

        @Test
        fun insertMail_duplicateMail() {
            val id = mockContactDTO.id
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(mockEmailDTO)
                }
                .andExpect{
                    status { isConflict() }
                }
        }

        @Test
        fun insertMail_invalidAddressType() {
            val id = mockContactDTO.id
            val telephoneDTO = TelephoneDTO("34742424242")
            mockMvc
                .post("/API/contacts/$id/email") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(telephoneDTO)
                }
                .andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun insertTelephone_success() {
            val id = mockContactDTO.id
            val telephoneDTO = TelephoneDTO("34742424242")
            mockMvc
                .post("/API/contacts/$id/telephone") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(telephoneDTO)
                }
                .andExpect{
                    status { isCreated() }
                }
        }

        @Test
        fun insertTelephone_invalidTelephone() {
            for (telephoneDTO in invalidTelephoneDTOs) {
                val id = mockContactDTO.id
                mockMvc
                    .post("/API/contacts/$id/telephone") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(telephoneDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun insertTelephone_contactNotFound() {
            val id = mockContactDTO.id + 1
            val telephoneDTO = TelephoneDTO("34742424242")
            mockMvc
                .post("/API/contacts/$id/telephone") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(telephoneDTO)
                }
                .andExpect{
                    status { isNotFound() }
                }
        }

        @Test
        fun insertTelephone_duplicateTelephone() {
            val id = mockContactDTO.id
            mockMvc
                .post("/API/contacts/$id/telephone") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(mockTelephoneDTO)
                }
                .andExpect{
                    status { isConflict() }
                }
        }

        @Test
        fun insertTelephone_invalidAddressType() {
            val id = mockContactDTO.id
            val emailDTO = EmailDTO("mario.rossi2@gmail.com")
            mockMvc
                .post("/API/contacts/$id/telephone") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(emailDTO)
                }
                .andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun insertDwelling_success() {
            val validDwellingDTOs = listOf(
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT"),
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", null),
                DwellingDTO("Via Garibaldi, 42", "Torino", null, null)
            )

            for (dwellingDTO in validDwellingDTOs) {
                val id = mockContactDTO.id
                mockMvc
                    .post("/API/contacts/$id/address") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(dwellingDTO)
                    }
                    .andExpect{
                        status { isCreated() }
                    }
            }
        }

        @Test
        fun insertDwelling_invalidDwellings() {
            for (dwellingDTO in invalidDwellingDTOs) {
                val id = mockContactDTO.id
                mockMvc
                    .post("/API/contacts/$id/address") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper.writeValueAsString(dwellingDTO)
                    }
                    .andExpect{
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun insertDwelling_contactNotFound() {
            val id = mockContactDTO.id + 1
            val dwellingDTO = DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT")
            mockMvc
                .post("/API/contacts/$id/address") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(dwellingDTO)
                }
                .andExpect{
                    status { isNotFound() }
                }
        }

        @Test
        fun insertDwelling_duplicateDwelling() {
            val id = mockContactDTO.id
            mockMvc
                .post("/API/contacts/$id/address") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(mockDwellingDTO)
                }
                .andExpect{
                    status { isConflict() }
                }
        }

        @Test
        fun insertDwelling_invalidAddressType() {
            val id = mockContactDTO.id
            val emailDTO = EmailDTO("mario.rossi2@gmail.com")
            mockMvc
                .post("/API/contacts/$id/address") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper.writeValueAsString(emailDTO)
                }
                .andExpect {
                    status { isBadRequest() }
                }
        }

    }

}