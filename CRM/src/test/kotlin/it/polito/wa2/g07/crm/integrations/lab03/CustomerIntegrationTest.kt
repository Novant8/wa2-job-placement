package it.polito.wa2.g07.crm.integrations.lab03

import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest

import org.springframework.http.MediaType

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import java.time.LocalDateTime
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc
class CustomerIntegrationTest: CrmApplicationTests() {

    @Autowired
    lateinit var mockMvc:MockMvc

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var jobOfferRepository: JobOfferRepository

    @Nested
    inner class PostCustomerTest{
        @BeforeEach
        fun init(){
            customerRepository.deleteAll()
        }
        @Test
        fun postCustomer (){
            val customer = """
                  {
                        "contact":{
                                 "name": "Customer",
                                "surname": "Company",
                                "category": "CUSTOMER",
                                "ssn":"123456",
                                "addresses":[{
                                    
                                    "phoneNumber":"6655855"
                                },
                                {
                                    "email": "customer@company.com"
                                },
                                 {
                                    "street":"123 Main St", 
                                    "city":"City", 
                                    "district":"District", 
                                    "country":"Country"
                                }
                                ]
                        },
                        "notes": "New customer acquired"
                  }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.name").value("Customer"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.surname").value("Company"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].email").value("customer@company.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].phoneNumber").value("6655855"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(MockMvcResultMatchers.jsonPath("notes").value("New customer acquired"))
        }

        @Test
        fun postCustomerWithoutNotes (){
            val customer = """
                  {
                        "contact":{
                                 "name": "Customer",
                                "surname": "Company",
                                "category": "CUSTOMER",
                                "ssn":"123456",
                                "addresses":[{
                                    
                                    "phoneNumber":"6655855"
                                },
                                {
                                    "email": "customer@company.com"
                                },
                                 {
                                    "street":"123 Main St", 
                                    "city":"City", 
                                    "district":"District", 
                                    "country":"Country"
                                }
                                ]
                        }
                  }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.name").value("Customer"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.surname").value("Company"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].email").value("customer@company.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].phoneNumber").value("6655855"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].country").value("Country"))

        }
        @Test
        fun postProfessional (){
            val customer = """
                  {
                        "contact":{
                                 "name": "Customer",
                                "surname": "Company",
                                "category": "Professional",
                                "ssn":"123456",
                                "addresses":[{
                                    
                                    "phoneNumber":"6655855"
                                },
                                {
                                    "email": "customer@company.com"
                                },
                                 {
                                    "street":"123 Main St", 
                                    "city":"City", 
                                    "district":"District", 
                                    "country":"Country"
                                }
                                ]
                        },
                        "notes": "New customer acquired"
                  }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)

        }

        //All the other test are the same of Contact Test
    }

    @Nested
    inner class Post_AssociateContactToCustomer(){
        private var contactId1 = 0L
        private var contactId2 = 0L

        @BeforeEach
        fun init(){

            customerRepository.deleteAll()
            contactRepository.deleteAll()
            val contactDto1 = CreateContactDTO(
                "Company",
                "Test",
                "customer",
                "123456",
                listOf(
                    TelephoneDTO("12345667889"),
                    EmailDTO("company.test@email.com"),
                    DwellingDTO("123 Main St", "City", "District","Country")
                )
            )

            val contactDto2 = CreateContactDTO(
                "Company2",
                "Test2",
                "professional",
                "9988ABC",
                listOf(
                    TelephoneDTO("00223345"),
                    EmailDTO("different.company@email.com"),
                    DwellingDTO("Street2", "City2", "District2","Country2")
                )
            )
            contactId1 = contactRepository.save(contactDto1.toEntity()).contactId
            contactId2= contactRepository.save(contactDto2.toEntity()).contactId
        }

        @Test
        fun associateContact(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/contacts/$contactId1/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.name").value("Company"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.surname").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(MockMvcResultMatchers.jsonPath("notes").value("New Customer associated"))
        }

        @Test
        fun associateContactWithInvalidId(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/contacts/20/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
        }
        @Test
        fun associateAlreadyConnectedContact(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()

            val body2 = """
                 {
                    "notes": "New Customer2 associated" 
                 }
            """.trimIndent()

            mockMvc.perform(MockMvcRequestBuilders.post("/API/contacts/$contactId1/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(MockMvcResultMatchers.status().isCreated)

            mockMvc.perform(MockMvcRequestBuilders.post("/API/contacts/$contactId1/customers").contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(MockMvcResultMatchers.status().isConflict)
        }
        @Test
        fun associateProfessionalContact(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()


            mockMvc.perform(post("/API/contacts/$contactId2/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest)


        }

    }

    @Nested
    inner class GetCustomerTest {

        private var customerId1 = 0L
        private var customerId2 = 0L

        @BeforeEach
        fun init(){
            customerRepository.deleteAll()
            contactRepository.deleteAll()
            val customerDto1 = CreateCustomerDTO(
                CreateContactDTO(
                    "Test",
                    "User",
                    "customer",
                    "123456",
                    listOf(
                        TelephoneDTO("12345667889"),
                        EmailDTO("test.user@email.com"),
                        DwellingDTO("123 Main St", "City", "District","Country")
                    )
                ),
                null
            )

            val customerDto2 = CreateCustomerDTO(
                CreateContactDTO(
                    "Company",
                    "Test",
                    "customer",
                    "123456",
                    listOf(
                        TelephoneDTO("0015589647"),
                        EmailDTO("company.test@email.com"),
                        DwellingDTO("Street2", "City2", "District2","Country2")
                    )
                ),
                "New Customer associated"
            )

            customerId1 = customerRepository.save(customerDto1.toEntity()).customerId
            customerId2 = customerRepository.save(customerDto2.toEntity()).customerId
        }

        @Test
        fun getCustomers(){
            mockMvc.perform(get("/API/customers/"))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(customerId1))
                .andExpect(jsonPath("$.content[0].contactInfo.name").value("Test"))
                .andExpect(jsonPath("$.content[0].contactInfo.surname").value("User"))
                .andExpect(jsonPath("$.content[0].contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].notes").value(null))
                .andExpect(jsonPath("$.content[1].id").value(customerId2))
                .andExpect(jsonPath("$.content[1].contactInfo.name").value("Company"))
                .andExpect(jsonPath("$.content[1].contactInfo.surname").value("Test"))
                .andExpect(jsonPath("$.content[1].contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[1].notes").value("New Customer associated"))

        }

        @Test
        fun getCustomersByID(){
            mockMvc.perform(get("/API/customers/$customerId2"))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("0015589647"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("Street2"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City2"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District2"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country2"))
                .andExpect(jsonPath("notes").value("New Customer associated"))
        }

        @Test
        fun getNonExistentCustomer() {
            mockMvc.perform(get("/API/contacts/202")).andExpect(status().isNotFound)

        }
    }

    @Nested
    inner class Post_JobOffer {
        private var customerID_1 = 0L


        @BeforeEach()
        fun init() {
            jobOfferRepository.deleteAll()
            customerRepository.deleteAll()
            contactRepository.deleteAll()


            val contact = Contact("Company", "Test", ContactCategory.CUSTOMER)
            val customer = Customer(contact, "Affidabile")
            customerID_1 = customerRepository.save(customer).customerId


        }

        @Test
        fun createJobOffer() {
            val req = """
                {"description":"descrizione","requiredSkills":["saltare","correre"],"duration":90}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                content { jsonPath("$.customer.id").isNotEmpty }
                content { jsonPath("$.customer.name") { value("Company") } }
                content { jsonPath("$.customer.surname") { value("Test") } }
                content { jsonPath("$.customer.category") { value("CUSTOMER") } }
                content { jsonPath("$.requiredSkills[0]") { value("correre") } }
                content { jsonPath("$.requiredSkills[1]") { value("saltare") } }
                content { jsonPath("$.offerStatus") { value("CREATED") } }
                content { jsonPath("$.duration") { value(90) } }
                content { jsonPath("$.professional").isEmpty }
                content { jsonPath("$.value").isEmpty }
                content { jsonPath("$.notes").isEmpty }
            }
        }

        @Test
        fun createJobOfferWithNotes() {
            val req = """
                {"description":"descrizione","requiredSkills":["saltare","correre"],"duration":90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                content { jsonPath("$.notes") { value("Questa è una nota") } }
            }
        }

        @Test
        fun createJobOffer_CustomerInvalid() {
            val req = """
                {"description":"descrizione","requiredSkills":["saltare","correre"],"duration":90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/1010201/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun createJobOffer_EmptyFields() {
            //empty requiredSkill
            var req = """
                {"description":"descrizione","requiredSkills":[],"duration":90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isUnprocessableEntity() }
            }
           //blanck description
            req = """
                {"description":"","requiredSkills":["test"],"duration":90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isUnprocessableEntity() }
            }
            //duration negative
            req = """
                {"description":"fd","requiredSkills":["test"],"duration":-90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isUnprocessableEntity() }
            }


        }

        @Test
        fun createMultipleJobOffer() {

            val req = """
                {"description":"descrizione","requiredSkills":["skill"],"duration":90,"notes":"Questa è una nota"}
            """.trimIndent()
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isOk() }
            }
            mockMvc.post("/API/customers/$customerID_1/job-offers") {
                contentType = MediaType.APPLICATION_JSON
                content = req
            }.andExpect {
                status { isOk() }
            }
        }

    }

}