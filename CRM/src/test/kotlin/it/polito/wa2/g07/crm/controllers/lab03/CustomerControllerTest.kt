package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.controllers.lab02.ContactController
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
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
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.shaded.org.apache.commons.lang3.time.DurationUtils.toDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit
import kotlin.time.toDuration


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
    inner class CreateJobOffer {
        private var customerID_1 = 0L
        private var mockJobOffer = JobOfferDTO(0L,
            ReducedContactDTO(0L,"nome","cognome",ContactCategory.CUSTOMER),
            requiredSkills = setOf("test"),
            duration=90,
            offerStatus = OfferStatus.CREATED,null,null,null)

        @BeforeEach
        fun initMocks(){
            every { jobOfferService.createJobOffer(any(),any()) } answers { mockJobOffer }
        }


        @Test
        fun createJobOffer() {


            val dto = JobOfferCreateDTO("descrizione",setOf ("saltare","correre"), 90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 1){ jobOfferService.createJobOffer(any(),any())}
            }


        }

        @Test
        fun createJobOfferWithNotes() {
            val dto = JobOfferCreateDTO("descrizione",setOf ("saltare","correre"), 90, notes = "ciao")
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
            var dto = JobOfferCreateDTO("descrizione",setOf (), 90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 0){ jobOfferService.createJobOffer(any(),any())}
            }
            //blanck description
             dto = JobOfferCreateDTO("",setOf ("saltare","correre"), 90)
                mockMvc.post("/API/customers/0/job-offers") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(dto)
                }.andExpect {
                    verify(exactly =0){ jobOfferService.createJobOffer(any(),any())}
                }
            //duration negative
            dto = JobOfferCreateDTO("descrizione",setOf ("saltare","correre"), -90)
            mockMvc.post("/API/customers/0/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(dto)
            }.andExpect {
                verify(exactly = 0){ jobOfferService.createJobOffer(any(),any())}
            }


        }


    }
}