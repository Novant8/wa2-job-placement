package it.polito.wa2.g07.crm.controllers



import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@AutoConfigureMockMvc
class ContactIntegrationTest:CrmApplicationTests() {
    @Autowired
     lateinit var mockMvc: MockMvc

     @Autowired
     lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var addressRepository: AddressRepository
//    @MockkBean
//    private lateinit var contactService:ContactService

    @Nested
    inner class GetContactsTest {
        private var id = 1L

        @BeforeEach
        fun init(){
            contactRepository.deleteAll()
            val contactDto = CreateContactDTO(
                "Test",
                "User",
                "customer",
                "123456",
                listOf(
                    TelephoneDTO("12345667889"),
                    EmailDTO("test.user@email.com"),
                    DwellingDTO("123 Main St", "City", "District","Country")
                )
            )
            id = contactRepository.save(contactDto.toEntity()).contactId
        }

        @Test
        fun getContacts_noFilters() {
            mockMvc.perform(
                get("/API/contacts")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))
        }

        @Test
        fun getContacts_fullNameFilter() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("fullName", "test user")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("fullName", "invalid")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContacts_categoryFilter() {
            // Successful filter
            for (filter in listOf("customer", "CUSTOMER")) {
                mockMvc.perform(
                    get("/API/contacts")
                        .queryParam("category", filter)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.content[0].id").value(id))
                    .andExpect(jsonPath("$.content[0].name").value("Test"))
                    .andExpect(jsonPath("$.content[0].surname").value("User"))
                    .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))
            }

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("category", "professional")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())

            // Invalid category
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("category", "invalid")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun getContacts_ssnFilter() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("ssn", "123456")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("ssn", "invalid")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContacts_emailFilter() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("email", "test.user@email.com")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("email", "invalid")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContacts_telephoneFilter() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("telephone", "12345667889")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("telephone", "invalid")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContacts_addressFilter() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("address", "123 main st")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("address", "invalid")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContacts_multipleFilters() {
            // Successful filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("fullName", "test user")
                    .queryParam("category", "customer")
                    .queryParam("ssn", "123456")
                    .queryParam("email", "test.user@email.com")
                    .queryParam("telephone", "12345667889")
                    .queryParam("address", "123 main st")

            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].name").value("Test"))
                .andExpect(jsonPath("$.content[0].surname").value("User"))
                .andExpect(jsonPath("$.content[0].category").value("CUSTOMER"))

            // Failed filter
            mockMvc.perform(
                get("/API/contacts")
                    .queryParam("fullName", "INVALID")
                    .queryParam("category", "customer")
                    .queryParam("ssn", "123456")
                    .queryParam("email", "test.user@email.com")
                    .queryParam("telephone", "12345667889")
                    .queryParam("address", "123 main st")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isEmpty())
        }

        @Test
        fun getContactFromId() {
            mockMvc.perform(get("/API/contacts/$id"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value("123456"))
                .andExpect(jsonPath("addresses[*].email").value("test.user@email.com"))
                .andExpect(jsonPath("addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("addresses[*].city").value("City"))
                .andExpect(jsonPath("addresses[*].district").value("District"))
                .andExpect(jsonPath("addresses[*].country").value("Country"))

        }

        @Test
        fun getNonExistentUser() {
            mockMvc.perform(get("/API/contacts/202")).andExpect(status().isNotFound)

        }
    }

    @Nested
    inner class PostContactTest{

        @Test
        fun postNewContact (){
            val contact1 =
                """
                {
                    "name": "Test",
                    "surname": "User",
                    "category": "customer",
                    "addresses":[{
                        "phoneNumber":"12345678"
                    },
                    {
                        "email":"test.user@email.com"
                    },
                    {
                        "street":"123 Main St", 
                        "city":"City", 
                        "district":"District", 
                        "country":"Country"
                    }
                    ]
                
                }
                """.trimIndent()

                mockMvc.perform(post("/API/contacts/").content(contact1).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated)
                    .andExpect(jsonPath("name").value("Test"))
                    .andExpect(jsonPath("surname").value("User"))
                    .andExpect(jsonPath("category").value("CUSTOMER"))


        }

        @Test
        fun postContact_missingFields(){
            val contact1 =
                """
                {
                    "surname": "User",
                    "category": "customer",
                    "addresses":[{
                        "phoneNumber":"12345678"
                    }]
                
                }
                """.trimIndent()

            mockMvc.perform(post("/API/contacts/").content(contact1).contentType(MediaType.APPLICATION_JSON)).
            andExpect(status().isUnprocessableEntity)
        }
    }

    @Nested
    inner class PostContactEmail{

        private var contactId: Long = 0

        @BeforeEach
        fun init(){
            contactRepository.deleteAll()
            val contact = Contact(
                    "User",
                    "Test",
                    ContactCategory.CUSTOMER,
                    null,
            )
            contact.addresses= mutableSetOf(Telephone("12345667889"), Email("test.user@email.com"))
            contactId = contactRepository.save(contact).contactId
        }

        @Test
        fun postEmail (){
            val email = "{\"email\":\"test.user1@email.com\"}"
            mockMvc.perform(post("/API/contacts/$contactId/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isCreated)
        }

        @Test
        fun postBlankEmail(){
            val email= "{\"email\":\"\"}"
            mockMvc.perform(post("/API/contacts/$contactId/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isUnprocessableEntity)
        }

        @Test
        fun postEmailToNonExistingUser(){
            val invalidContact = contactId + 1
            val email = "{\"email\":\"test.user1@email.com\"}"
            mockMvc.perform(post("/API/contacts/$invalidContact/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isNotFound)
        }

        @Test
        fun postEmailAlreadyAssociated(){
            val email = "{\"email\":\"test.user@email.com\"}"
            mockMvc.perform(post("/API/contacts/$contactId/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isConflict)
        }

    }

    @Nested
    inner class DeleteContactEmail {

        private var contact1Id = 0L

        private var telephoneId = 0L
        private var email1Id = 0L
        private var email2Id = 0L

        @BeforeEach
        fun init(){
            contactRepository.deleteAll()
            addressRepository.deleteAll()
            val contact1Dto = CreateContactDTO("Test", "User", "customer",null, listOf(TelephoneDTO("435433635532424556"),EmailDTO("test.user@email.com")))
            val contact2Dto = CreateContactDTO("Test2", "User2", "",null, listOf(EmailDTO("test2.user2@email.com")))
            contact1Id= contactRepository.save(contact1Dto.toEntity()).contactId
            contactRepository.save(contact2Dto.toEntity())
            email1Id= addressRepository.findMailAddressByMail("test.user@email.com").get().id
            email2Id= addressRepository.findMailAddressByMail("test2.user2@email.com").get().id
            telephoneId= addressRepository.findTelephoneAddressByTelephoneNumber("435433635532424556").get().id
        }
        
        @Test
        fun deleteCorrectEmail(){
            mockMvc.perform(delete("/API/contacts/$contact1Id/email/$email1Id")).andExpect(status().isNoContent)
        }

        @Test
        fun deleteEmailForNonExistingUser (){
            mockMvc.perform(delete("/API/contacts/50/email/$email2Id")).andExpect(status().isNotFound)
        }

        @Test
        fun deleteNonExistentEmail() {
            mockMvc.perform(delete("/API/contacts/$contact1Id/email/4242")).andExpect(status().isNotFound)
        }

        @Test
        fun deleteAddressDifferentFromEmail (){
            mockMvc.perform(delete("/API/contacts/$contact1Id/email/$telephoneId")).andExpect(status().isBadRequest)
        }

        @Test
        fun deleteEmailNotBelongingToThatUser (){
            mockMvc.perform(delete("/API/contacts/$contact1Id/email/$email2Id")).andExpect(status().isBadRequest)
        }


    }

    @Nested
    inner class PutContact {

        private var contactId = 0L


        private var emailId = 0L
        private var telephoneId = 0L
        private var dwellingId = 0L

        @BeforeEach
        fun init(){
            contactRepository.deleteAll()
            addressRepository.deleteAll()
            val contactDto = CreateContactDTO("Test", "User", "customer",null, listOf(TelephoneDTO("3015544789"),EmailDTO("test.user@email.com"),DwellingDTO("123 Main St", "City", "District","Country")))
            contactId= contactRepository.save(contactDto.toEntity()).contactId
            emailId=addressRepository.findMailAddressByMail("test.user@email.com").get().id
            telephoneId= addressRepository.findTelephoneAddressByTelephoneNumber("3015544789").get().id
            dwellingId = addressRepository.findDwellingAddressByStreet("123 Main St", "City", "District","Country").get().id
        }


        @Test
        fun putEmail (){

            val email = "{\"email\":\"new.email@email.com\"}"

            mockMvc.perform(put("/API/contacts/$contactId/email/$emailId").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value(null))
                .andExpect(jsonPath("addresses[*].email").value("new.email@email.com"))
                .andExpect(jsonPath("addresses[*].phoneNumber").value("3015544789"))
                .andExpect(jsonPath("addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("addresses[*].city").value("City"))
                .andExpect(jsonPath("addresses[*].district").value("District"))
                .andExpect(jsonPath("addresses[*].country").value("Country"))
        }

        @Test
        fun putTelephone (){

            val telephone = "{\"phoneNumber\":\"11111111\"}"

            mockMvc.perform(put("/API/contacts/$contactId/telephone/$telephoneId").contentType(MediaType.APPLICATION_JSON).content(telephone))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value(null))
                .andExpect(jsonPath("addresses[*].email").value("test.user@email.com"))
                .andExpect(jsonPath("addresses[*].phoneNumber").value("11111111"))
                .andExpect(jsonPath("addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("addresses[*].city").value("City"))
                .andExpect(jsonPath("addresses[*].district").value("District"))
                .andExpect(jsonPath("addresses[*].country").value("Country"))
        }

        @Test
        fun putDwelling (){

            val dwelling = """
                {
                        "street":"New Street", 
                        "city":"New City", 
                        "district":"New District", 
                        "country":"New Country"
                    }
            """.trimIndent()

            mockMvc.perform(put("/API/contacts/$contactId/address/$dwellingId").contentType(MediaType.APPLICATION_JSON).content(dwelling))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value(null))
                .andExpect(jsonPath("addresses[*].email").value("test.user@email.com"))
                .andExpect(jsonPath("addresses[*].phoneNumber").value("3015544789"))
                .andExpect(jsonPath("addresses[*].street").value("New Street"))
                .andExpect(jsonPath("addresses[*].city").value("New City"))
                .andExpect(jsonPath("addresses[*].district").value("New District"))
                .andExpect(jsonPath("addresses[*].country").value("New Country"))
        }

        @Test
        fun putAddressForNonExistentUser(){
            val email = "{\"email\":\"new.email@email.com\"}"

            mockMvc.perform(put("/API/contacts/20/email/$emailId").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isNotFound)
        }
        @Test
        fun putAddressNotAssociatedWithTheUser(){
            val email = "{\"email\":\"new.email@email.com\"}"

            mockMvc.perform(put("/API/contacts/$contactId/email/20").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isNotFound)
        }

        @Test
        fun putAddressFromDifferentTypes(){

            val phoneNumber = "{\"phoneNumber\":\"123456\"}"

            //we call the Put email endpoint, but we are passing a Telephone number
            mockMvc.perform(put("/API/contacts/$contactId/email/$telephoneId").contentType(MediaType.APPLICATION_JSON).content(phoneNumber))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun putEmptyEmail(){

            val email = "{\"email\":\"\"}"
            mockMvc.perform(put("/API/contacts/$contactId/email/$emailId").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun putEmptyTelephone(){

            val telephone = "{\"phoneNumber\":\"\"}"
            mockMvc.perform(put("/API/contacts/$contactId/telephone/$telephoneId").contentType(MediaType.APPLICATION_JSON).content(telephone))
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
            mockMvc.perform(put("/API/contacts/$contactId/address/$dwellingId").contentType(MediaType.APPLICATION_JSON).content(dwelling))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        @Disabled("Empty street should result in a unprocessable entity?")
        fun putDwellingWithEmptyField(){

            val dwelling = """
                {
                        "street":"", 
                        "city":"New City", 
                        "district":"New District", 
                        "country":"New Country"
                    }
            """.trimIndent()
            mockMvc.perform(put("/API/contacts/$contactId/address/$dwellingId").contentType(MediaType.APPLICATION_JSON).content(dwelling))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value(null))
                .andExpect(jsonPath("addresses[*].email").value("test.user@email.com"))
                .andExpect(jsonPath("addresses[*].phoneNumber").value("3015544789"))
                .andExpect(jsonPath("addresses[*].street").value(""))
                .andExpect(jsonPath("addresses[*].city").value("New City"))
                .andExpect(jsonPath("addresses[*].district").value("New District"))
                .andExpect(jsonPath("addresses[*].country").value("New Country"))
        }




    }


}