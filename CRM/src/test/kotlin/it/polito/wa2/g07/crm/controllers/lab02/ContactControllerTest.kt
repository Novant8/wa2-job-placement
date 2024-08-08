package it.polito.wa2.g07.crm.controllers.lab02

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.AddressType
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.CustomerService
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.*

@WebMvcTest(ContactController::class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var contactService: ContactService

    @MockkBean
    private lateinit var customerService: CustomerService
    @MockkBean
    private lateinit var professionalService: ProfessionalService

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

    @Nested
    inner class GetContactTests {

        @BeforeEach
        fun initMocks() {
            every { contactService.getContacts(any(ContactFilterDTO::class), any(Pageable::class)) } answers {
                val filterDTO = firstArg<ContactFilterDTO>()
                if(!filterDTO.category.isNullOrBlank() && !ContactCategory.entries.map { it.name }.contains(filterDTO.category?.uppercase())) {
                    throw InvalidParamsException("Invalid category")
                }
                PageImpl(listOf(mockReducedContactDTO))
            }
        }

        @Test
        fun getContacts_success() {
            mockMvc
                .get("/API/contacts")
                .andExpect {
                    status{ isOk() }
                    verify(exactly = 1) { contactService.getContacts(ContactFilterDTO(), PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedContactDTO.id) }
                        jsonPath("$.content[0].name") { value(mockReducedContactDTO.name) }
                        jsonPath("$.content[0].surname") { value(mockReducedContactDTO.surname) }
                    }
                }
        }

        @Test
        fun getContacts_invalidCategory() {
            mockMvc
                .get("/API/contacts") {
                    queryParam("category", "i am not valid")
                }
                .andExpect {
                    status { isBadRequest() }
                }
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PostContactTests {

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
                    content = jsonMapper().writeValueAsString(createContactDTO)
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
                    content = jsonMapper().writeValueAsString(createContactDTO)
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
                    content = jsonMapper().writeValueAsString(createContactDTO)
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
                        content = jsonMapper().writeValueAsString(createContactDTO)
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
                        content = jsonMapper().writeValueAsString(createContactDTO)
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
                    content = jsonMapper().writeValueAsString(createContactDTO)
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
                        content = jsonMapper().writeValueAsString(createContactDTO)
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
                    content = jsonMapper().writeValueAsString(emailDTO)
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
                        content = jsonMapper().writeValueAsString(emailDTO)
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
                    content = jsonMapper().writeValueAsString(emailDTO)
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
                    content = jsonMapper().writeValueAsString(mockEmailDTO)
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
                    content = jsonMapper().writeValueAsString(telephoneDTO)
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
                    content = jsonMapper().writeValueAsString(telephoneDTO)
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
                        content = jsonMapper().writeValueAsString(telephoneDTO)
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
                    content = jsonMapper().writeValueAsString(telephoneDTO)
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
                    content = jsonMapper().writeValueAsString(mockTelephoneDTO)
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
                    content = jsonMapper().writeValueAsString(emailDTO)
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
                        content = jsonMapper().writeValueAsString(dwellingDTO)
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
                        content = jsonMapper().writeValueAsString(dwellingDTO)
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
                    content = jsonMapper().writeValueAsString(dwellingDTO)
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
                    content = jsonMapper().writeValueAsString(mockDwellingDTO)
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
                    content = jsonMapper().writeValueAsString(emailDTO)
                }
                .andExpect {
                    status { isBadRequest() }
                }
        }

    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PutContactTests {

        @BeforeEach
        fun initMocks() {
            every { contactService.updateAddress(any(Long::class), any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Contact not found")
            every { contactService.updateAddress(mockContactDTO.id, any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Address not found")
            for (addressResponseDTO in listOf(mockEmailDTO, mockTelephoneDTO, mockDwellingDTO)) {
                every { contactService.updateAddress(mockContactDTO.id, addressResponseDTO.id, any(AddressDTO::class)) } answers {
                    val addressDTO = thirdArg<AddressDTO>()
                    if (addressDTO.addressType != addressResponseDTO.toAddressDTO().addressType)
                        throw InvalidParamsException("Mismatching address types")
                    ContactDTO(
                        mockContactDTO.id,
                        mockContactDTO.name,
                        mockContactDTO.surname,
                        mockContactDTO.category,
                        mockContactDTO.addresses.map { if (it.id == addressResponseDTO.id) addressDTO.toEntity().toAddressResponseDTO() else it },
                        mockContactDTO.ssn
                    )
                }
                every { contactService.updateAddress(mockContactDTO.id, addressResponseDTO.id, addressResponseDTO.toAddressDTO()) } throws DuplicateAddressException("Address already associated")
            }
        }

        @Test
        @Disabled
        fun updateMail_success() {
            val contactId = mockContactDTO.id
            val addressId = mockEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/contacts/$contactId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.addresses[*].email") { value(updatedEmailDTO.email) }
                        jsonPath("$.addresses[?(@.email == \"${mockEmailDTO.email}\")]") { doesNotExist() }
                    }
                }
        }

        @Test
        @Disabled
        fun updateMail_invalidMail() {
            for (emailDTO in invalidEmailDTOs) {
                val contactId = mockContactDTO.id
                val addressId = mockEmailDTO.id
                mockMvc
                    .put("/API/contacts/$contactId/email/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(emailDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        @Disabled
        fun updateMail_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/contacts/$contactId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @Disabled
        fun updateMail_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/contacts/$contactId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @Disabled
        fun updateMail_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/contacts/$contactId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @Disabled
        fun updateMail_duplicateMail() {
            val contactId = mockContactDTO.id
            val addressId = mockEmailDTO.id
            mockMvc
                .put("/API/contacts/$contactId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(mockEmailDTO)
                }.andExpect {
                    status { isConflict() }
                }
        }

        @Test
        fun updateTelephone_success() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            val updatedTelephoneDTO = TelephoneDTO("34298989898")
            mockMvc
                .put("/API/contacts/$contactId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedTelephoneDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.addresses[*].phoneNumber") { value(updatedTelephoneDTO.phoneNumber) }
                        jsonPath("$.addresses[?(@.phoneNumber == \"${mockTelephoneDTO.phoneNumber}\")]") { doesNotExist() }
                    }
                }
        }

        @Test
        fun updateTelephone_invalidTelephone() {
            for (telephoneDTO in invalidTelephoneDTOs) {
                val contactId = mockContactDTO.id
                val addressId = mockTelephoneDTO.id
                mockMvc
                    .put("/API/contacts/$contactId/telephone/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(telephoneDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun updateTelephone_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockTelephoneDTO.id
            val updatedTelephoneDTO = TelephoneDTO("34298989898")
            mockMvc
                .put("/API/contacts/$contactId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedTelephoneDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateTelephone_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            val updatedTelephoneDTO = TelephoneDTO("34298989898")
            mockMvc
                .put("/API/contacts/$contactId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedTelephoneDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateTelephone_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            val updatedEmailDTO = EmailDTO("updated.email@example.org")
            mockMvc
                .put("/API/contacts/$contactId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateTelephone_duplicateTelephone() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            mockMvc
                .put("/API/contacts/$contactId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(mockTelephoneDTO)
                }.andExpect {
                    status { isConflict() }
                }
        }

        @Test
        fun updateDwelling_success() {
            val contactId = mockContactDTO.id
            val addressId = mockDwellingDTO.id
            val validDwellingDTOs = listOf(
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT"),
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", null),
                DwellingDTO("Via Garibaldi, 42", "Torino", null, null)
            )
            for (dwellingDTO in validDwellingDTOs) {
                mockMvc
                    .put("/API/contacts/$contactId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isOk() }
                        content {
                            jsonPath("$.addresses[*].street") { value(dwellingDTO.street) }
                            jsonPath("$.addresses[?(@.street == \"${mockDwellingDTO.street}\")]") { doesNotExist() }
                        }
                    }
            }
        }

        @Test
        fun updateDwelling_invalidDwelling() {
            for (dwellingDTO in invalidDwellingDTOs) {
                val contactId = mockContactDTO.id
                val addressId = mockDwellingDTO.id
                mockMvc
                    .put("/API/contacts/$contactId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun updateDwelling_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockDwellingDTO.id
            val updatedDwellingDTO = DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT")
            mockMvc
                .put("/API/contacts/$contactId/address/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedDwellingDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateDwelling_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            val updatedDwellingDTO = DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT")
            mockMvc
                .put("/API/contacts/$contactId/address/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedDwellingDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateDwelling_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockDwellingDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/contacts/$contactId/address/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateDwelling_duplicateDwelling() {
            val contactId = mockContactDTO.id
            val addressId = mockDwellingDTO.id
            mockMvc
                .put("/API/contacts/$contactId/address/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(mockDwellingDTO)
                }.andExpect {
                    status { isConflict() }
                }
        }

    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class DeleteContactTests {

        @BeforeEach
        fun initMocks() {
            every { contactService.deleteAddress(any(Long::class), any(Long::class), any(AddressType::class)) } throws EntityNotFoundException("Contact not found")
            every { contactService.deleteAddress(mockContactDTO.id, any(Long::class), any(AddressType::class)) } throws EntityNotFoundException("Address not found")
            for (addressResponseDTO in mockContactDTO.addresses)
                every { contactService.deleteAddress(mockContactDTO.id, addressResponseDTO.id, any(AddressType::class)) } answers {
                    if (thirdArg<AddressType>() != addressResponseDTO.toAddressDTO().addressType) {
                        throw InvalidParamsException("Invalid address type")
                    }
                }
        }

        @Test
        fun deleteMail_success() {
            val contactId = mockContactDTO.id
            val addressId = mockEmailDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/email/$addressId")
                .andExpect {
                    status { isNoContent() }
                }
        }

        @Test
        fun deleteMail_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockEmailDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/email/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteMail_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            mockMvc
                .delete("/API/contacts/$contactId/email/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteMail_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/email/$addressId")
                .andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun deleteTelephone_success() {
            val contactId = mockContactDTO.id
            val addressId = mockTelephoneDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/telephone/$addressId")
                .andExpect {
                    status { isNoContent() }
                }
        }

        @Test
        fun deleteTelephone_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockTelephoneDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/telephone/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteTelephone_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            mockMvc
                .delete("/API/contacts/$contactId/telephone/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteTelephone_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockEmailDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/telephone/$addressId")
                .andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun deleteDwelling_success() {
            val contactId = mockContactDTO.id
            val addressId = mockDwellingDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/address/$addressId")
                .andExpect {
                    status { isNoContent() }
                }
        }

        @Test
        fun deleteDwelling_contactNotFound() {
            val contactId = mockContactDTO.id + 1
            val addressId = mockDwellingDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/address/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteDwelling_addressNotFound() {
            val contactId = mockContactDTO.id
            val addressId = 42L
            mockMvc
                .delete("/API/contacts/$contactId/address/$addressId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteDwelling_invalidAddressType() {
            val contactId = mockContactDTO.id
            val addressId = mockEmailDTO.id
            mockMvc
                .delete("/API/contacts/$contactId/address/$addressId")
                .andExpect {
                    status { isBadRequest() }
                }
        }

    }

}