package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.controllers.lab02.ContactController
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.*


import it.polito.wa2.g07.crm.entities.lab02.ContactCategory

import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.CustomerService
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
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
import org.springframework.test.web.servlet.put



@WebMvcTest(CustomerController::class,ContactController::class)
class CustomerControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var customerService: CustomerService

    @MockkBean
    private lateinit var contactService: ContactService

    @MockkBean
    private lateinit var jobOfferService: JobOfferService


    private val mockEmailDTO = EmailDTO("company.test@example.org")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")

    private val mockResponseEmailDTO = EmailResponseDTO(1L,"company.test@example.org")
    private val mockResponseTelephoneDTO = TelephoneResponseDTO(2L,"34242424242")
    private val mockResponseDwellingDTO = DwellingResponseDTO(3L,"Via Roma, 18", "Torino", "TO", "IT")


    private val mockContactDTO = ContactDTO(
        1L,
        "Mario",
        "Rossi",
        ContactCategory.CUSTOMER,
        listOf(mockResponseEmailDTO, mockResponseTelephoneDTO, mockResponseDwellingDTO),
        "RSSMRA70A01L219K"
    )
    private val mockReducedContactDTO = ReducedContactDTO(
        mockContactDTO.id,
        mockContactDTO.name,
        mockContactDTO.surname,
        mockContactDTO.category
    )

    private val mockReducedCustomerDTO = ReducedCustomerDTO(
        1L,
        mockReducedContactDTO,
        "New Customer"
    )

    private val mockCustomerDTO = CustomerDTO(
        1L,
        mockContactDTO,
        "New Customer"
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
    inner class PostCustomer{

        @BeforeEach
        fun initMocks(){
            every { customerService.createCustomer(any(CreateCustomerDTO::class)) } answers {
                val customerDTO = firstArg<CreateCustomerDTO>()
                if (customerDTO.contact.category != "CUSTOMER"){
                   throw InvalidParamsException("You must register a Customer user ")
                }else{
                    customerDTO.toEntity().toCustomerDto()
                }
            }
        }

        @Test
        fun saveCustomer(){
            val createCustomerDTO = CreateCustomerDTO(
                CreateContactDTO(
                    "Company",
                    "Test",
                    ContactCategory.CUSTOMER.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO,mockTelephoneDTO,mockDwellingDTO)
                ),
                "New Customer "
            )

            mockMvc
                .post("/API/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createCustomerDTO)
                }.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.contactInfo.name"){value(createCustomerDTO.contact.name)}
                        jsonPath("$.contactInfo.surname"){value(createCustomerDTO.contact.surname)}
                        jsonPath("$.contactInfo.category"){value(createCustomerDTO.contact.category)}
                        jsonPath("$.contactInfo.ssn"){value(createCustomerDTO.contact.ssn)}
                        jsonPath("$.contactInfo.addresses[*].email"){value(mockEmailDTO.email)}
                        jsonPath("$.contactInfo.addresses[*].phoneNumber"){value(mockTelephoneDTO.phoneNumber)}
                        jsonPath("$.contactInfo.addresses[*].street"){value(mockDwellingDTO.street)}
                        jsonPath("$.contactInfo.addresses[*].district"){value(mockDwellingDTO.district)}
                        jsonPath("$.contactInfo.addresses[*].city"){value(mockDwellingDTO.city)}
                        jsonPath("$.contactInfo.addresses[*].country"){value(mockDwellingDTO.country)}
                        jsonPath("$.notes"){value(createCustomerDTO.notes)}
                    }
                }
        }

        @Test
        fun saveCustomer_noNotes(){
            val createCustomerDTO = CreateCustomerDTO(
                CreateContactDTO(
                    "Company",
                    "Test",
                    ContactCategory.CUSTOMER.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO,mockTelephoneDTO,mockDwellingDTO)
                ),
                null
            )

            mockMvc
                .post("/API/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createCustomerDTO)
                }.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.contactInfo.name"){value(createCustomerDTO.contact.name)}
                        jsonPath("$.contactInfo.surname"){value(createCustomerDTO.contact.surname)}
                        jsonPath("$.contactInfo.category"){value(createCustomerDTO.contact.category)}
                        jsonPath("$.contactInfo.ssn"){value(createCustomerDTO.contact.ssn)}
                        jsonPath("$.contactInfo.addresses[*].email"){value(mockEmailDTO.email)}
                        jsonPath("$.contactInfo.addresses[*].phoneNumber"){value(mockTelephoneDTO.phoneNumber)}
                        jsonPath("$.contactInfo.addresses[*].street"){value(mockDwellingDTO.street)}
                        jsonPath("$.contactInfo.addresses[*].district"){value(mockDwellingDTO.district)}
                        jsonPath("$.contactInfo.addresses[*].city"){value(mockDwellingDTO.city)}
                        jsonPath("$.contactInfo.addresses[*].country"){value(mockDwellingDTO.country)}
                        jsonPath("$.notes"){value(null)}
                    }
                }
        }

        @Test
        fun saveProfessional(){
            val createCustomerDTO = CreateCustomerDTO(
                CreateContactDTO(
                    "Company",
                    "Test",
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO,mockTelephoneDTO,mockDwellingDTO)
                ),
                "New Customer "
            )

            mockMvc
                .post("/API/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createCustomerDTO)
                }.andExpect {
                    status { isBadRequest() }

                }
        }

    }

    @Nested
    inner class AssociateContact{

        private val usedContactIds = HashSet<Long>()
        @BeforeEach
        fun initMocks(){
           every { customerService.bindContactToCustomer(any(Long::class), any(String::class)) } answers {
               val contactId:Long = firstArg<Long>()

               if (usedContactIds.contains(contactId)){
                   throw ContactAssociationException("Contact with id : ${mockContactDTO.id} is already associated to another Customer ")
               }else if (contactId != mockContactDTO.id){
                   throw EntityNotFoundException("Contact does not exist")
               }

                CustomerDTO(
                    1L,
                    ContactDTO(
                        mockContactDTO.id,
                        mockContactDTO.name,
                        mockContactDTO.surname,
                        mockContactDTO.category,
                        listOf(mockContactDTO.addresses).flatten(),
                        mockContactDTO.ssn
                    ),
                    secondArg<String>()
                )
            }
             //every { customerService.bindContactToCustomer(any(Long::class),any(String::class)) } throws EntityNotFoundException("Contact does not exist")

        }

        @Test
        fun associateValidContact(){
            val notes = mapOf("notes" to "New User")
            val contactId = mockContactDTO.id
            mockMvc
                .post("/API/contacts/$contactId/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(notes)
                }.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.contactInfo.name"){value(mockContactDTO.name)}
                        jsonPath("$.contactInfo.surname"){value(mockContactDTO.surname)}
                        jsonPath("$.contactInfo.category"){value(mockContactDTO.category.name)}
                        jsonPath("$.contactInfo.ssn"){value(mockContactDTO.ssn)}
                        jsonPath("$.contactInfo.addresses[*].email"){value(mockEmailDTO.email)}
                        jsonPath("$.contactInfo.addresses[*].phoneNumber"){value(mockTelephoneDTO.phoneNumber)}
                        jsonPath("$.contactInfo.addresses[*].street"){value(mockDwellingDTO.street)}
                        jsonPath("$.contactInfo.addresses[*].district"){value(mockDwellingDTO.district)}
                        jsonPath("$.contactInfo.addresses[*].city"){value(mockDwellingDTO.city)}
                        jsonPath("$.contactInfo.addresses[*].country"){value(mockDwellingDTO.country)}
                        jsonPath("$.notes"){value(notes["notes"])}
                    }
                }
        }

        @Test
        fun associateUnknownContact(){
            val notes = mapOf("notes" to "New User")

            mockMvc
                .post("/API/contacts/20/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(notes)
                }.andExpect {
                    status { isNotFound() }

                }
        }

        @Test
        fun associateAlreadyConnectedContact(){
            val notes = mapOf("notes" to "New User")
            val notes2 = mapOf("notes" to "New User2")
            val contactId = mockContactDTO.id
            mockMvc
                .post("/API/contacts/$contactId/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(notes)
                }.andExpect {
                    status { isCreated() }

                }

            usedContactIds.add(contactId)

            mockMvc
                .post("/API/contacts/$contactId/customers"){
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(notes2)
                }.andExpect {
                    status { isConflict() }

                }
        }

    }

    @Nested
    inner class GetCustomerTest {
        @BeforeEach
        fun initMocks() {
            every { customerService.getCustomers(any(Pageable::class)) } answers {
                PageImpl(
                    listOf(
                        mockReducedCustomerDTO
                    )
                )
            }
            every { customerService.getCustomerById(any(Long::class)) } answers {
                val customerId = firstArg<Long>()
                if (customerId != mockCustomerDTO.id) {
                    throw EntityNotFoundException("Customer with id : $customerId is not found")
                }
                mockCustomerDTO
            }
            every { contactService.getContacts(any(ContactFilterDTO::class), any(Pageable::class)) } returns PageImpl(listOf(mockReducedContactDTO))
            every { customerService.getCustomersByContactIds(match { it.contains(mockContactDTO.id) }, any(Pageable::class)) } returns PageImpl(listOf(mockReducedCustomerDTO))
        }

        @Test
        fun getCustomers_noFilters(){
            mockMvc.get("/API/customers")
                .andExpect {
                    status { isOk() }
                    verify (exactly = 1){customerService.getCustomers(PageRequest.of(0,20))  }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedCustomerDTO.id) }
                        jsonPath("$.content[0].contactInfo.id") { value(mockReducedCustomerDTO.contactInfo.id) }
                        jsonPath("$.content[0].contactInfo.name") { value(mockReducedCustomerDTO.contactInfo.name) }
                        jsonPath("$.content[0].contactInfo.surname") { value(mockReducedCustomerDTO.contactInfo.surname) }
                        jsonPath("$.content[0].contactInfo.category") { value(mockReducedCustomerDTO.contactInfo.category.name) }
                        jsonPath("$.content[0].notes") { value(mockReducedCustomerDTO.notes) }
                    }
                }

        }

        @Test
        fun getCustomers_withFilter() {
            mockMvc.get("/API/customers") {
                queryParam("full_name", mockContactDTO.name)
            }.andExpect {
                status { isOk() }
                verify (exactly = 1){customerService.getCustomers(PageRequest.of(0,20))  }
                content {
                    jsonPath("$.content[0].id") { value(mockReducedCustomerDTO.id) }
                    jsonPath("$.content[0].contactInfo.id") { value(mockReducedCustomerDTO.contactInfo.id) }
                    jsonPath("$.content[0].contactInfo.name") { value(mockReducedCustomerDTO.contactInfo.name) }
                    jsonPath("$.content[0].contactInfo.surname") { value(mockReducedCustomerDTO.contactInfo.surname) }
                    jsonPath("$.content[0].contactInfo.category") { value(mockReducedCustomerDTO.contactInfo.category.name) }
                    jsonPath("$.content[0].notes") { value(mockReducedCustomerDTO.notes) }
                }
            }
        }

        @Test
        fun getCustomersById(){
            mockMvc.get("/API/customers/${mockCustomerDTO.id}")
                .andExpect {
                    status { isOk() }
                    verify (exactly = 1){customerService.getCustomerById(mockCustomerDTO.id)  }
                    content {
                        jsonPath("$.id") { value(mockCustomerDTO.id) }
                        jsonPath("$.contactInfo.id") { value(mockCustomerDTO.contactInfo.id) }
                        jsonPath("$.contactInfo.name") { value(mockCustomerDTO.contactInfo.name) }
                        jsonPath("$.contactInfo.surname") { value(mockCustomerDTO.contactInfo.surname) }
                        jsonPath("$.contactInfo.category") { value(mockCustomerDTO.contactInfo.category.name) }
                        jsonPath("$.contactInfo.addresses[*].email") { value(mockEmailDTO.email) }
                        jsonPath("$.contactInfo.addresses[*].phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.contactInfo.addresses[*].street") { value(mockDwellingDTO.street) }
                        jsonPath("$.contactInfo.addresses[*].city") { value(mockDwellingDTO.city) }
                        jsonPath("$.contactInfo.addresses[*].district") { value(mockDwellingDTO.district) }
                        jsonPath("$.contactInfo.addresses[*].country") { value(mockDwellingDTO.country) }
                        jsonPath("$.notes") { value(mockCustomerDTO.notes) }
                    }
                }

        }
        @Test
        fun getNonExistentCustomer(){
            mockMvc.get("/API/customers/202")
                .andExpect {
                    status { isNotFound() }
                }

        }
    }
    @Nested
    inner class CreateJobOffer {
        private var customerID_1 = 0L
        private var mockJobOffer = JobOfferDTO(0L,
            description = "descrizione",
            customer = ReducedCustomerDTO(0L,ReducedContactDTO(0L,"nome","cognome",ContactCategory.CUSTOMER),null),
            requiredSkills = mutableSetOf("test"),
            duration=90,
            offerStatus = OfferStatus.CREATED,null,null,null)

        @BeforeEach
        fun initMocks(){
            every { jobOfferService.createJobOffer(any(),any()) } answers { mockJobOffer }
        }


        @Test
        fun createJobOffer() {


            val dto = JobOfferCreateDTO("descrizione",mutableSetOf ("saltare","correre"), 90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 1){ jobOfferService.createJobOffer(any(),any())}
            }


        }

        @Test
        fun createJobOfferWithNotes() {
            val dto = JobOfferCreateDTO("descrizione",mutableSetOf ("saltare","correre"), 90, notes = "ciao")
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 1){ jobOfferService.createJobOffer(any(),any())}
            }
        }


        @Test
        fun createJobOffer_EmptyFields() {
            //empty requiredSkill
            var dto = JobOfferCreateDTO("descrizione",mutableSetOf (), 90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 0){ jobOfferService.createJobOffer(any(),any())}
            }
            //blanck description
             dto = JobOfferCreateDTO("",mutableSetOf ("saltare","correre"), 90)
                mockMvc.post("/API/customers/0/job-offers") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(dto)
                }.andExpect {
                    verify(exactly =0){ jobOfferService.createJobOffer(any(),any())}
                }
            //duration negative
            dto = JobOfferCreateDTO("descrizione",mutableSetOf ("saltare","correre"), -90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 0){ jobOfferService.createJobOffer(any(),any())}
            }


        }


    }

    @Nested
    inner class PutCustomer(){
        private val contactId = mockContactDTO.id

        @BeforeEach
        fun initMocks(){
            every { customerService.postCustomerNotes(any(Long::class), any(String::class)) } throws EntityNotFoundException("Customer not found")
            every { customerService.postCustomerNotes(mockCustomerDTO.id, any(String::class)) } answers {
                val notes = secondArg<String>()
                CustomerDTO(
                    mockCustomerDTO.id,
                    mockCustomerDTO.contactInfo,
                    notes
                )
            }
            every {customerService.getCustomerById(any(Long::class))} throws EntityNotFoundException("Customer not found")
            every { customerService.getCustomerById(mockCustomerDTO.id) } answers {mockCustomerDTO}
            every { contactService.updateAddress(any(Long::class), any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Contact not found")
            every { contactService.updateAddress(mockContactDTO.id, any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Address not found")
            for (addressResponseDTO in listOf(mockResponseEmailDTO, mockResponseTelephoneDTO, mockResponseDwellingDTO)) {
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
        fun updateNotes(){
            val updateNotes = mapOf("notes" to "UpdatedNotes")

            mockMvc.put("/API/customers/$contactId/notes"){
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateNotes)
            }.andExpect {
                status { isOk() }
                content {
                    jsonPath("$.notes") { value(updateNotes["notes"]) }
                }
            }
        }

        @Test
        fun updateMail_success() {
            val customerId = mockCustomerDTO.id
            val addressId = mockResponseEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/customers/$customerId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.contactInfo.addresses[*].email") { value(updatedEmailDTO.email) }
                        jsonPath("$.contactInfo.addresses[?(@.email == \"${mockEmailDTO.email}\")]") { doesNotExist() }
                    }
                }
        }


        @Test
        fun updateUnknownCustomer(){
            val updateNotes = mapOf("notes" to "UpdatedNotes")

            mockMvc.put("/API/customers/200/notes"){
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateNotes)
            }.andExpect {
                status { isNotFound() }

            }
        }

        @Test
        fun updateMail_invalidMail() {
            for (emailDTO in invalidEmailDTOs) {
                val customerId = mockCustomerDTO.id
                val addressId = mockResponseEmailDTO.id
                mockMvc
                    .put("/API/customers/$customerId/email/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(emailDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun updateMail_contactNotFound() {
            val customerId = mockCustomerDTO.id + 1
            val addressId = mockResponseEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/customers/$customerId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateMail_addressNotFound() {
            val customerId = mockCustomerDTO.id
            val addressId = 42L
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/customers/$customerId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateMail_invalidAddressType() {
            val customerId = mockCustomerDTO.id
            val addressId = mockResponseTelephoneDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/customers/$customerId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateMail_duplicateMail() {
            val customerId = mockCustomerDTO.id
            val addressId = mockResponseEmailDTO.id
            mockMvc
                .put("/API/customers/$customerId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(mockEmailDTO)
                }.andExpect {
                    status { isConflict() }
                }
        }

        @Test
        fun updateTelephone_success() {
            val customerId = mockCustomerDTO.id
            val addressId = mockResponseTelephoneDTO.id
            val updatedTelephoneDTO = TelephoneDTO("34298989898")
            mockMvc
                .put("/API/customers/$customerId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedTelephoneDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.contactInfo.addresses[*].phoneNumber") { value(updatedTelephoneDTO.phoneNumber) }
                        jsonPath("$.contactInfo.addresses[?(@.phoneNumber == \"${mockTelephoneDTO.phoneNumber}\")]") { doesNotExist() }
                    }
                }
        }

        @Test
        fun updateTelephone_invalidTelephone() {
            for (telephoneDTO in invalidTelephoneDTOs) {
                val customerId = mockCustomerDTO.id
                val addressId = mockResponseTelephoneDTO.id
                mockMvc
                    .put("/API/customers/$customerId/telephone/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(telephoneDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }


        @Test
        fun updateDwelling_success() {
            val customerId = mockCustomerDTO.id
            val addressId = mockResponseDwellingDTO.id
            val validDwellingDTOs = listOf(
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT"),
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", null),
                DwellingDTO("Via Garibaldi, 42", "Torino", null, null)
            )
            for (dwellingDTO in validDwellingDTOs) {
                mockMvc
                    .put("/API/customers/$customerId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isOk() }
                        content {
                            jsonPath("$.contactInfo.addresses[*].street") { value(dwellingDTO.street) }
                            jsonPath("$.contactInfo.addresses[?(@.street == \"${mockDwellingDTO.street}\")]") { doesNotExist() }
                        }
                    }
            }
        }

        @Test
        fun updateDwelling_invalidDwelling() {
            for (dwellingDTO in invalidDwellingDTOs) {
                val customerId = mockCustomerDTO.id
                val addressId = mockResponseDwellingDTO.id
                mockMvc
                    .put("/API/customers/$customerId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }


    }
}