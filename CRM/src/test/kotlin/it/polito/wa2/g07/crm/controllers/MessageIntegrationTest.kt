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
import org.junit.jupiter.api.Nested

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
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@AutoConfigureMockMvc
class MessageIntegrationTest:CrmApplicationTests()  {
    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var messageRepository: MessageRepository
    @Autowired
    lateinit var addressRepository: AddressRepository

    var msg1_id :Long = 0
    var msg2_id :Long = 0
    var msg3_id :Long = 0
    @BeforeEach
    fun initDb() {
        //Each test the DB start clean
        addressRepository.deleteAll()
        messageRepository.deleteAll()
        val sender1=Email("lorem@ipsum.com")
        val sender2=Dwelling("Piazza Centrale","Milano","Lombardia","Italia")
        val sender3=Telephone("011-43553#352")
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
        val msg3 = Message(
            subject = "Assumetemi pls",
            sender = sender3,
            body= "pls assumetemi"
        )

        msg1.addEvent(MessageEvent(msg1,MessageStatus.RECEIVED, LocalDateTime.now(),"The message is received"))

        msg2.addEvent(MessageEvent(msg2,MessageStatus.RECEIVED, LocalDateTime.now(),"Please read the message"))
        msg2.addEvent(MessageEvent(msg2,MessageStatus.READ, LocalDateTime.now(),"Spam"))
        msg2.addEvent(MessageEvent(msg2,MessageStatus.DISCARDED, LocalDateTime.now(),"Invalid document"))

        msg3.addEvent(MessageEvent(msg3,MessageStatus.RECEIVED, LocalDateTime.now()))
        msg3.addEvent(MessageEvent(msg3,MessageStatus.READ, LocalDateTime.now()))
        msg3.addEvent(MessageEvent(msg3,MessageStatus.PROCESSING, LocalDateTime.now()))
        addressRepository.save(sender1)
        addressRepository.save(sender2)
        addressRepository.save(sender3)
        msg1_id = messageRepository.save(msg1).messageID
        msg2_id = messageRepository.save(msg2).messageID
        msg3_id = messageRepository.save(msg3).messageID

    }

    @Test
    fun getAllMessages (){
        mockMvc.get("/API/messages"){
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("content"){ isArray()}}
            content { jsonPath("totalElements"){value(3)} }
            content { jsonPath("$.content[0].subject"){ value("There is a message for you")}}
            content { jsonPath("$.content[0].sender.email"){ value("lorem@ipsum.com")}}
            content { jsonPath("$.content[0].sender.channel"){ value("email")}}

            content { jsonPath("$.content[1].subject"){ value("Richiesta info")}}
            content { jsonPath("$.content[1].sender.street"){ value("Piazza Centrale")}}
            content { jsonPath("$.content[1].sender.channel"){ value("dwelling")}}
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
        assertEquals(   messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                        messageRepository.findById(msg_2_id.toLong()).get().sender.id)

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
        assertEquals(   messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                        messageRepository.findById(msg_2_id.toLong()).get().sender.id)


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

        assertNotEquals(    messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                            messageRepository.findById(msg_2_id.toLong()).get().sender.id)
    }

        @Test
        fun PRIORITY_getPriority(){
             mockMvc.get("/API/messages/$msg1_id")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.priority") { value("0") } }
                }
        }
        @Test
        fun PRIORITY_putPriority(){
            mockMvc.put("/API/messages/$msg1_id/priority"){
                content =  """{"priority":3}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { jsonPath("$.priority") { value("3") } }
            }

            mockMvc.get("/API/messages/$msg1_id")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.priority") { value("3") } }
                }
        }
        @Test
        fun PRIORITY_putPriorityNotValid(){
            mockMvc.put("/API/messages/$msg1_id/priority"){
                content =  """{"priority":-1}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
            mockMvc.put("/API/messages/$msg1_id/priority"){
                content =  """{"priority":"A"}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
            mockMvc.put("/API/messages/$msg1_id/priority"){
                content =  """"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

    @Test
    fun MESSAGE_getMessageState(){
        mockMvc.get("/API/messages/$msg1_id").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.lastEvent.status") { value("RECEIVED") } }
            content { jsonPath("$.lastEvent.comments") { value("The message is received") } }
            content { jsonPath("$.lastEvent.timestamp") { isNotEmpty() } }
        }
        mockMvc.get("/API/messages/$msg2_id").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.lastEvent.status") { value("DISCARDED") } }
            content { jsonPath("$.lastEvent.comments") { value("Invalid document") } }
            content { jsonPath("$.lastEvent.timestamp") { isNotEmpty() } }
        }
        mockMvc.get("/API/messages/$msg3_id").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.lastEvent.status") { value("PROCESSING") } }
            content { jsonPath("$.lastEvent.comments").doesNotExist() }
            content { jsonPath("$.lastEvent.timestamp") { isNotEmpty() } }
        }
    }
    @Test
    fun HISTORY_getMessageHistory(){
        mockMvc.get("/API/messages/1010543/history").andExpect {
            status { isNotFound() }
        }
        mockMvc.get("/API/messages/$msg1_id/history").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.totalElements") { value(1) } }
            content { jsonPath("$.content[0].status") { value("RECEIVED") } }
        }
        mockMvc.get("/API/messages/$msg2_id/history").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.totalElements") { value(3) } }
            content { jsonPath("$.content[2].status") { value("RECEIVED") } }
            content { jsonPath("$.content[1].status") { value("READ") } }
            content { jsonPath("$.content[0].status") { value("DISCARDED") } }
        }
        mockMvc.get("/API/messages/$msg3_id/history").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { jsonPath("$.totalElements") { value(3) } }
            content { jsonPath("$.content[2].status") { value("RECEIVED") } }
            content { jsonPath("$.content[1].status") { value("READ") } }
            content { jsonPath("$.content[0].status") { value("PROCESSING") } }
        }
    }
    @Nested
    inner class Message_History{
        @Test
        fun postMessageHistoryValid(){
            mockMvc.post("/API/messages/$msg1_id"){
                content =  """{"status":"READ","comments":"Read immediatly"}""""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { jsonPath("$.status") { value("READ") } }
                content { jsonPath("$.timestamp").exists()}
                content { jsonPath("$.comments"){value("Read immediatly") } }
            }

            mockMvc.post("/API/messages/$msg1_id"){
                content =  """{"status":"DONE"}""""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { jsonPath("$.status") { value("DONE") } }
                content { jsonPath("$.timestamp").exists()}
                content { jsonPath("$.comments").doesNotExist()}
            }

            mockMvc.get("/API/messages/$msg1_id/history").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { jsonPath("$.totalElements") { value(3) } }
                content { jsonPath("$.content[2].status") { value("RECEIVED") } }
                content { jsonPath("$.content[1].status") { value("READ") } }
                content { jsonPath("$.content[0].status") { value("DONE") } }
            }


        }
        @Test
        fun postMessageHistoryInvalid(){
            mockMvc.post("/API/messages/435643653"){
                content =  """{"status":"READ","comments":"Read immediatly"}""""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }

            mockMvc.post("/API/messages/$msg1_id"){
                content =  """{"status":"Read","comments":"Read immediatly"}""""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                content { jsonPath("$.detail") { value("Failed to read request") } }
            }

            mockMvc.post("/API/messages/$msg1_id"){
                content =  """{"status":"FAILED","comments":"Read immediatly"}""""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                content { jsonPath("$.detail") { value("The status cannot be assigned to the message") } }
            }
        }
    }

}