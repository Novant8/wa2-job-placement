package it.polito.wa2.g07.crm.controllers



import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.repositories.ContactRepository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@AutoConfigureMockMvc
class ContactIntegrationTest:CrmApplicationTests() {
    @Autowired
     lateinit var mockMvc: MockMvc

     @Autowired
     lateinit var contactRepository: ContactRepository

//    @MockkBean
//    private lateinit var contactService:ContactService

    @Test
    fun getEmptyContact (){
        mockMvc.get("/API/contacts"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("content"){
                isArray()
                isEmpty()
            } }
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
                        "type": "phone",
                        "phoneNumber":"12345678"
                    },
                    {
                        "type": "email",
                        "email":"test.user@email.com"
                    },
                    {
                        "type": "dwelling",
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
                        "type": "phone",
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
                status().isNotModified)
        }

    }

    @Nested
    inner class GetContactById{
        private var id = 1L

        @BeforeEach
        fun init(){
            contactRepository.deleteAll()
            val contactDto = CreateContactDTO("Test", "User", "customer",null, listOf(TelephoneDTO("12345667889"),EmailDTO("test.user@email.com"), DwellingDTO("123 Main St", "City", "District","Country")))
            id = contactRepository.save(contactDto.toEntity()).contactId
        }

        @Test
        fun getContact (){
            mockMvc.perform(get("/API/contacts/$id"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("surname").value("User"))
                .andExpect(jsonPath("category").value("CUSTOMER"))
                .andExpect(jsonPath("ssn").value(null))
                /* MUTABLE SET THAT SAVES ADDRESSES IN THE ENTITY CHANGES THE ORDER OF ITS ELEMENTS EVERY TEST
                .andExpect(jsonPath("addresses[0].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("addresses[1].email").value("test.user@email.com"))
                */
        }
    }


}