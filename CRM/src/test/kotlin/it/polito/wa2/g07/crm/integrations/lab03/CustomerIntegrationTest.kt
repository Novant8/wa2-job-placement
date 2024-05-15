package it.polito.wa2.g07.crm.integrations.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab02.Email
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.repositories.lab02.AddressRepository
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest


import org.springframework.http.MediaType

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*



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

    @Autowired
    lateinit var addressRepository: AddressRepository


    @Nested
    inner class PostCustomerTest{

        var contact = Contact(
            "Existing",
            "Contact",
            ContactCategory.CUSTOMER
        )

        init {
            contact.addresses = mutableSetOf(
                Email("existing.mail@example.org")
            )
        }

        @BeforeEach
        fun init(){
            customerRepository.deleteAll()
            contactRepository.deleteAll()
            contact = contactRepository.save(contact)
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

            mockMvc.perform(post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Customer"))
                .andExpect(jsonPath("contactInfo.surname").value("Company"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("customer@company.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("6655855"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("New customer acquired"))
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

            mockMvc.perform(post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Customer"))
                .andExpect(jsonPath("contactInfo.surname").value("Company"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("customer@company.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("6655855"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))

        }

        @Test
        fun postCustomerWithExistingContact(){
            val customer = """
                  {
                        "contact": {
                                "name": "Existing",
                                "surname": "Contact",
                                "category": "CUSTOMER",
                                "addresses": [
                                    { "email": "existing.mail@example.org" }
                                ]
                        }
                  }
            """.trimIndent()

            mockMvc.perform(post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.addresses[*].id").value(contact.addresses.first().id.toInt()))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("existing.mail@example.org"))
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

            mockMvc.perform(post("/API/customers").contentType(MediaType.APPLICATION_JSON).content(customer))
                .andExpect(status().isBadRequest)

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

            mockMvc.perform(post("/API/contacts/$contactId1/customer").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("New Customer associated"))
        }

        @Test
        fun associateContactWithInvalidId(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()

            mockMvc.perform(post("/API/contacts/20/customer").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound)
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

            mockMvc.perform(post("/API/contacts/$contactId1/customer").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated)

            mockMvc.perform(post("/API/contacts/$contactId1/customer").contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isConflict)
        }
        @Test
        fun associateProfessionalContact(){
            val body = """
                 {
                    "notes": "New Customer associated" 
                 }
            """.trimIndent()


            mockMvc.perform(post("/API/contacts/$contactId2/customer").contentType(MediaType.APPLICATION_JSON).content(body))
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
        fun getCustomers_noFilters(){
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
        fun getCustomers_withFilter_match(){
            mockMvc
                .perform(get("/API/customers/").queryParam("fullName", "test user"))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(customerId1))
                .andExpect(jsonPath("$.content[0].contactInfo.name").value("Test"))
                .andExpect(jsonPath("$.content[0].contactInfo.surname").value("User"))
                .andExpect(jsonPath("$.content[0].contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].notes").value(null))
                .andExpect(jsonPath("$.content[1]").doesNotExist())
        }

        @Test
        fun getCustomers_withFilter_noMatch(){
            mockMvc
                .perform(get("/API/customers/").queryParam("fullName", "does not exist"))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isEmpty())
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
                content { jsonPath("$.customer.contactInfo.name") { value("Company") } }
                content { jsonPath("$.customer.contactInfo.surname") { value("Test") } }
                content { jsonPath("$.customer.contactInfo.category") { value("CUSTOMER") } }
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

    @Nested
    inner class PutCustomer(){

        private var customerId = 0L

        private var emailId = 0L
        private var telephoneId = 0L
        private var dwellingId = 0L

        @BeforeEach
        fun init(){
            customerRepository.deleteAll()
            val customerDto = CreateCustomerDTO(
                CreateContactDTO(
                    "Company",
                    "Test",
                    "customer",
                    "123456",
                    listOf(
                        TelephoneDTO("12345667889"),
                        EmailDTO("company.test@email.com"),
                        DwellingDTO("123 Main St", "City", "District","Country")
                    )
                ),
                "New Customer"
            )

            customerId = customerRepository.save(customerDto.toEntity()).customerId
            emailId = addressRepository.findMailAddressByMail("company.test@email.com").get().id
            telephoneId= addressRepository.findTelephoneAddressByTelephoneNumber("12345667889").get().id
            dwellingId = addressRepository.findDwellingAddressByStreet("123 Main St", "City", "District","Country").get().id

        }

        @Test
        fun putCustomerNotes(){
            val body = """
                 {
                    "notes": "Updated Customer Notes" 
                 }
            """.trimIndent()

            mockMvc.perform(put("/API/customers/$customerId/notes").content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("Updated Customer Notes"))
        }

        @Test
        fun putCustomerEmail (){
            val email = EmailDTO("updated.mail@example.org")

            mockMvc.perform(put("/API/customers/$customerId/email/$emailId").content(jsonMapper().writeValueAsString(email)).contentType(MediaType.APPLICATION_JSON))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value(email.email))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("New Customer"))


        }

        @Test
        fun putCustomerTelephone (){
            val telephone = TelephoneDTO("34298989898")

            mockMvc.perform(put("/API/customers/$customerId/telephone/$telephoneId").content(jsonMapper().writeValueAsString(telephone)).contentType(MediaType.APPLICATION_JSON))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value(telephone.phoneNumber))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("New Customer"))


        }

        @Test
        fun putCustomerDwelling (){
            val dwelling = DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT")

            mockMvc.perform(put("/API/customers/$customerId/address/$dwellingId").content(jsonMapper().writeValueAsString(dwelling)).contentType(MediaType.APPLICATION_JSON))
                .andExpect( status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Company"))
                .andExpect(jsonPath("contactInfo.surname").value("Test"))
                .andExpect(jsonPath("contactInfo.category").value("CUSTOMER"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value(dwelling.street))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value(dwelling.city))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value(dwelling.district))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value(dwelling.country))
                .andExpect(jsonPath("notes").value("New Customer"))


        }

        @Test
        fun putAddressNotAssociatedWithTheUser(){
            val email = "{\"email\":\"new.email@email.com\"}"

            mockMvc.perform(put("/API/contacts/$customerId/email/20").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isNotFound)
        }

        @Test
        fun putAddressFromDifferentTypes(){

            val phoneNumber = "{\"phoneNumber\":\"123456\"}"

            //we call the Put email endpoint, but we are passing a Telephone number
            mockMvc.perform(put("/API/contacts/$customerId/email/$telephoneId").contentType(MediaType.APPLICATION_JSON).content(phoneNumber))
                .andExpect(status().isBadRequest)
        }


        @Test
        fun putEmptyEmail(){

            val email = "{\"email\":\"\"}"
            mockMvc.perform(put("/API/contacts/$customerId/email/$emailId").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun putEmptyTelephone(){

            val telephone = "{\"phoneNumber\":\"\"}"
            mockMvc.perform(put("/API/contacts/$customerId/telephone/$telephoneId").contentType(MediaType.APPLICATION_JSON).content(telephone))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun putEmptyDwelling(){

            val dwelling = """
                {
                        "street":"", 
                        "city":"", 
                        "district":"", 
                        "country":""
                    }
            """.trimIndent()
            mockMvc.perform(put("/API/contacts/$customerId/address/$dwellingId").contentType(MediaType.APPLICATION_JSON).content(dwelling))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun putUnknownCustomer(){
            val body = """
                 {
                    "notes": "Updated Customer Notes" 
                 }
            """.trimIndent()

            mockMvc.perform(put("/API/customers/202/notes").content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect( status().isNotFound)

        }
    }

}