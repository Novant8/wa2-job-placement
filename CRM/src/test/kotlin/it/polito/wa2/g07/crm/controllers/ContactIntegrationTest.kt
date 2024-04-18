package it.polito.wa2.g07.crm.controllers


import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@AutoConfigureMockMvc
//Create a new context every new test method
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ContactIntegrationTest:CrmApplicationTests() {
    @Autowired
     lateinit var mockMvc: MockMvc

     @Autowired
     lateinit var contactRepo: ContactRepository

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
    @Test
    fun postNewContact (){
        val contact1 = """
            {
                "name": "prova",
                "surname": "test",
                "category": "customer",
                "addresses": [
                    {
                        "type": "mail",
                        "mail": "test@mail.com"
                    }, 
                    {
                        "type": "phone",
                        "phone_number": "123456789"
                    }
                ]
            }
        """.trimIndent()

        mockMvc.perform(post("/API/contacts/").content(contact1).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated)
        /*
                mockMvc.post("/API/contacts"){
                    contentType = MediaType.APPLICATION_JSON
                    content= contact1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                */


    }
}