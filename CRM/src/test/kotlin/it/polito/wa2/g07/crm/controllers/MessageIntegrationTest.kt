package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.entities.Email
import it.polito.wa2.g07.crm.repositories.ContactRepository
import it.polito.wa2.g07.crm.repositories.MessageRepository
import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.entities.MessageEvent
import it.polito.wa2.g07.crm.entities.MessageStatus
import it.polito.wa2.g07.crm.repositories.AddressRepository
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
//Create a new context every new test method
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MessageIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var messageRepository: MessageRepository
    @Autowired
    lateinit var addressRepository: AddressRepository
    companion object {

            @Container
            val db = PostgreSQLContainer("postgres:latest")
            val documentStoreEndpoint = "/API/documents/"
            @JvmStatic
            @DynamicPropertySource
            fun properties(registry: DynamicPropertyRegistry) {
                registry.add("spring.datasource.url", db::getJdbcUrl)
                registry.add("spring.datasource.username", db::getUsername)
                registry.add("spring.datasource.password", db::getPassword)
                registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            }

        /* Prepopulate the DB*/
        val sender1=Email("lorem@ipsum.com")
        val msg1  = Message(
            subject =  "There is a message for you",
            sender = sender1,
            body= "lorem ipsum dolor sit amet",
        )
        init {
            msg1.addEvent(MessageEvent(msg1,MessageStatus.RECEIVED, LocalDateTime.now(),"The message is received"))
        }

    }
    @BeforeEach
    fun initDb() {
        //Each test the DB start clean
        addressRepository.save(sender1)
        messageRepository.save(msg1)

    }

    @Test
    fun getAllMessages (){
        mockMvc.get("/API/messages"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("content"){ isArray()}}
            content { jsonPath("totalElements"){value(1)} }
            content { jsonPath("$.content[0].subject"){ value("There is a message for you")}}
            content { jsonPath("$.content[0].sender.email"){ value("lorem@ipsum.com")}}
            content { jsonPath("$.content[0].sender.channel"){ value("email")}}
        }
    }
    @Test
    fun getSpecificMessage(){
        mockMvc.get("/API/messages/1"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

}