package it.polito.wa2.g07.crm.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.repositories.ContactRepository
import it.polito.wa2.g07.crm.repositories.MessageRepository
import it.polito.wa2.g07.crm.repositories.AddressRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
//Create a new context every new test method
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
        val sender2=Dwelling("Piazza Centrale","Milano","Lombardia","Italia")

        val msg1  = Message(
            subject =  "There is a message for you",
            sender = sender1,
            body= "lorem ipsum dolor sit amet",
        )
        val msg2 = Message(
            subject = "Richiesta info",
            sender = sender2,
            body = "Vorrei sapere..."
        )
        var msg1_id :Long =0 ;
        var msg2_id :Long=0;
        init {
            msg1.addEvent(MessageEvent(msg1,MessageStatus.RECEIVED, LocalDateTime.now(),"The message is received"))
            msg2.addEvent(MessageEvent(msg1,MessageStatus.RECEIVED, LocalDateTime.now(),"Please read the message"))

        }

    }
    @BeforeEach
    fun initDb() {
        //Each test the DB start clean
        addressRepository.save(sender1)
        addressRepository.save(sender2)
        msg1_id = messageRepository.save(msg1).messageID
        msg2_id = messageRepository.save(msg2).messageID

    }

    @Test
    fun getAllMessages (){
        mockMvc.get("/API/messages"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("content"){ isArray()}}
            content { jsonPath("totalElements"){value(2)} }
            content { jsonPath("$.content[0].subject"){ value("There is a message for you")}}
            content { jsonPath("$.content[0].sender.email"){ value("lorem@ipsum.com")}}
            content { jsonPath("$.content[0].sender.channel"){ value("email")}}
        }
    }


    @Test
    fun getSpecificMessage(){
        mockMvc.get("/API/messages/$msg1_id"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }


    }
    @Test
    fun createMessageTelephone(){
        val message_1 =
            """
                {
                    "sender":{
                        "phoneNumber": "011-765352x",
                        "channel" : "phone"
                    },
                    "subject" : "Action required",
                    "body" : "please visit http://your-bank.com"  
                }
            """.trimIndent()
       mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_1
        }.andExpect{
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject"){ value("Action required")}}
            content { jsonPath("$.sender.phoneNumber"){ value("011-765352x")}}
            content { jsonPath("$.sender.channel"){ value("phone")}}
            content { jsonPath("$.body"){value("please visit http://your-bank.com")} }
        }
        //missing subject
       val message_2 =
            """
                {
                    "sender":{
                        "phoneNumber": "011-765352x",
                        "channel" : "phone"
                    },
                    "body" : "please visit http://your-bank.com"  
                }
            """.trimIndent()
        mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_2
        }.andExpect{
            status { isBadRequest()}
        }



        //check that the address is the same and not duplicated




    }
    @Test
    fun checkNoDuplicateAddressForTheSameSender_telephone() {
        val message_1 =
        """
                {
                    "sender":{
                        "phoneNumber": "011-765352x",
                        "channel" : "phone"
                    },
                    "subject" : "Action required",
                    "body" : "please visit http://your-bank.com"  
                }
        """.trimIndent()
        val msg_1_id=  JSONObject(mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_1
        }.andExpect{
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject"){ value("Action required")}}
            content { jsonPath("$.sender.phoneNumber"){ value("011-765352x")}}
            content { jsonPath("$.sender.channel"){ value("phone")}}
            content { jsonPath("$.body"){value("please visit http://your-bank.com")} }
        }.andReturn().response.contentAsString).getString("id")
        val message_2 =
            """
                {
                    "sender":{
                        "phoneNumber": "011-765352x",
                        "channel" : "phone"
                    },
                    "subject" : "Action required x 2",
                    "body" : "please visit http://your-bank.com"  
                }
            """.trimIndent()
        val msg_2_id=  JSONObject( mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_2
        }.andExpect{
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject"){ value("Action required x 2")}}
            content { jsonPath("$.sender.phoneNumber"){ value("011-765352x")}}
            content { jsonPath("$.sender.channel"){ value("phone")}}
            content { jsonPath("$.body"){value("please visit http://your-bank.com")} }

        }.andReturn().response.contentAsString).getString("id")
        assertEquals(   messageRepository.getMessageByMessageID(msg_1_id.toLong())!!.sender.id,
                        messageRepository.getMessageByMessageID(msg_2_id.toLong())!!.sender.id)
    }

    @Test
    fun createMessageDwelling(){
        val message_1 =
            """
                {
                    "sender":{
                        "street": "Via Roma",
                        "city": "Torino",
                        "channel" : "dwelling"
                    },
                    "subject" : "Raccomandata",
                    "body" : "Gentile x, la contatto in merito..."  
                }
            """.trimIndent()
        mockMvc.post("/API/messages") {
            contentType = MediaType.APPLICATION_JSON
            content = message_1
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject") { value("Raccomandata") } }
            content { jsonPath("$.sender.street") { value("Via Roma") } }
            content { jsonPath("$.sender.city") { value("Torino") } }
            content { jsonPath("$.sender.channel") { value("dwelling") } }
            content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
        }
        val message_2 =
            """
                {
                    "sender":{
                        "street": "Via Roma",
                        "channel" : "dwelling"
                    },
                    "subject" : "Raccomandata",
                    "body" : "Gentile x, la contatto in merito..."  
                }
            """.trimIndent()
        mockMvc.post("/API/messages") {
            contentType = MediaType.APPLICATION_JSON
            content = message_2
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun checkNoDuplicateAddressForTheSameSender_dwelling() {
        val message_1 =
            """
                {
                    "sender":{
                        "street": "Via Roma",
                        "city": "Torino",
                        "channel" : "dwelling"
                    },
                    "subject" : "Raccomandata",
                    "body" : "Gentile x, la contatto in merito..."  
                }
            """.trimIndent()
        val msg_1_id=  JSONObject(mockMvc.post("/API/messages") {
            contentType = MediaType.APPLICATION_JSON
            content = message_1
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject") { value("Raccomandata") } }
            content { jsonPath("$.sender.street") { value("Via Roma") } }
            content { jsonPath("$.sender.city") { value("Torino") } }
            content { jsonPath("$.sender.channel") { value("dwelling") } }
            content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
        }.andReturn().response.contentAsString).getString("id")

        val message_2 =
            """
                {
                    "sender":{
                        "street": "Via Roma",
                        "city": "Torino",
                        "district": "Piemonte",
                        "country": "Italy",
                        "channel" : "dwelling"
                    },
                    "subject" : "Raccomandata",
                    "body" : "Gentile x, la contatto in merito..."  
                }
            """.trimIndent()
        val msg_2_id=  JSONObject(mockMvc.post("/API/messages") {
            contentType = MediaType.APPLICATION_JSON
            content = message_2
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.subject") { value("Raccomandata") } }
            content { jsonPath("$.sender.street") { value("Via Roma") } }
            content { jsonPath("$.sender.city") { value("Torino") } }
            content { jsonPath("$.sender.country").doesNotExist()  }  //there is already an address incomplete with the same street and district
            content { jsonPath("$.sender.district").doesNotExist()  }
            content { jsonPath("$.sender.channel") { value("dwelling") } }
            content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
        }.andReturn().response.contentAsString).getString("id")
        assertEquals(   messageRepository.getMessageByMessageID(msg_1_id.toLong())!!.sender.id,
                        messageRepository.getMessageByMessageID(msg_2_id.toLong())!!.sender.id)


    }


    @Test
    fun checkSameAddressDifferentChannel(){
        val message_1 =
            """
                {
                    "sender":{
                        "phoneNumber": "A",
                        "channel" : "phone"
                    },
                    "subject" : "TEST",
                    "body" : "TEST"  
                }
            """.trimIndent()
        val message_2=
            """
                {
                    "sender":{
                        "email": "A",
                        "channel" : "email"
                    },
                    "subject" : "TEST",
                    "body" : "TEST"  
                }
            """.trimIndent()

        val msg_1_id=  JSONObject( mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_1
        }.andExpect{
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn().response.contentAsString).getString("id")

        val msg_2_id=  JSONObject( mockMvc.post("/API/messages"){
            contentType = MediaType.APPLICATION_JSON
            content = message_2
        }.andExpect{
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn().response.contentAsString).getString("id")

        assertNotEquals(    messageRepository.getMessageByMessageID(msg_1_id.toLong())!!.sender.id,
                            messageRepository.getMessageByMessageID(msg_2_id.toLong())!!.sender.id)
    }
}