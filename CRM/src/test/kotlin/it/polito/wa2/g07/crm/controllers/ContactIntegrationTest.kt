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

        @BeforeEach
        fun init(){
            val contactDto = Contact(
                    "User",
                    "Test",
                    ContactCategory.CUSTOMER,
                    null,
            )
            contactDto.contactId= 1L
            contactDto.addresses= mutableSetOf(Telephone("12345667889"))
            contactRepository.save(contactDto)
        }

        @Test
        fun postEmail (){
            val email = "{\"email\":\"test.user@email.com\"}"
            mockMvc.perform(post("/API/contacts/1/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isOk)
        }
        @Test
        fun postBlankEmail(){
            val email= "{\"email\":}"
            mockMvc.perform(post("/API/contacts/1/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isBadRequest)
        }

        @Test

        fun postEmailToNonExistingUser(){
            val email = "{\"email\":\"test.user@email.com\"}"
            mockMvc.perform(post("/API/contacts/2/email").contentType(MediaType.APPLICATION_JSON).content(email)).andExpect(
                status().isNotFound)
        }

    }

    @Nested
    inner class GetContactById{
        private val id = 1L
        @BeforeEach
        fun init(){

            val contactDto = CreateContactDTO("Test", "User", "customer",null, listOf(TelephoneDTO("12345667889"),EmailDTO("test.user@email.com"), DwellingDTO("123 Main St", "City", "District","Country")))
            contactRepository.save(contactDto.toEntity())
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