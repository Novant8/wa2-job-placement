package it.polito.wa2.g07.crm.controllers


import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.entities.*

import it.polito.wa2.g07.crm.repositories.MessageRepository
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertEquals
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

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import java.time.LocalDateTime
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc

class MessageIntegrationTest:CrmApplicationTests() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var contactRepository: ContactRepository
    @Autowired
    lateinit var addressRepository: AddressRepository

    var msg1_id: Long = 0
    var msg2_id: Long = 0
    var msg3_id: Long = 0
    val sender1 = Email("lorem@ipsum.com")
    val sender2 = Dwelling("Piazza Centrale", "Milano", "Lombardia", "Italia")
    val sender3 = Telephone("011-43553")
    val msg1 = Message(
        subject = "There is a message for you",
        sender = sender1,

        body = "lorem ipsum dolor sit amet",
    )
    val msg2 = Message(
        subject = "Richiesta info",
        sender = sender2,
        body = "Vorrei sapere..."
    )
    val msg3 = Message(
        subject = "Assumetemi pls",
        sender = sender3,
        body = "pls assumetemi"
    )
    @BeforeEach

    fun initDb() {
        //Each test the DB start clean

        messageRepository.deleteAll()
        contactRepository.deleteAll()
        addressRepository.deleteAll()

        msg1.addEvent(MessageEvent(msg1, MessageStatus.RECEIVED, LocalDateTime.now(), "The message is received"))

        msg2.addEvent(MessageEvent(msg2, MessageStatus.RECEIVED, LocalDateTime.now(), "Please read the message"))
        msg2.addEvent(MessageEvent(msg2, MessageStatus.READ, LocalDateTime.now(), "Spam"))
        msg2.addEvent(MessageEvent(msg2, MessageStatus.DISCARDED, LocalDateTime.now(), "Invalid document"))

        msg3.addEvent(MessageEvent(msg3, MessageStatus.RECEIVED, LocalDateTime.now()))
        msg3.addEvent(MessageEvent(msg3, MessageStatus.READ, LocalDateTime.now()))
        msg3.addEvent(MessageEvent(msg3, MessageStatus.PROCESSING, LocalDateTime.now()))
        addressRepository.save(sender1)
        addressRepository.save(sender2)
        addressRepository.save(sender3)
        msg1_id = messageRepository.save(msg1).messageID
        msg2_id = messageRepository.save(msg2).messageID
        msg3_id = messageRepository.save(msg3).messageID

    }


    @Nested
    inner class Message {


        @Nested
        inner class GetMessage() {
            @Test
            fun retriveAllMessages() {
                mockMvc.get("/API/messages") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("content") { isArray() } }
                    content { jsonPath("totalElements") { value(3) } }
                    content { jsonPath("$.content[0].subject") { value("There is a message for you") } }
                    content { jsonPath("$.content[0].sender.email") { value("lorem@ipsum.com") } }
                    content { jsonPath("$.content[0].channel") { value("EMAIL") } }

                    content { jsonPath("$.content[1].subject") { value("Richiesta info") } }
                    content { jsonPath("$.content[1].sender.street") { value("Piazza Centrale") } }
                    content { jsonPath("$.content[1].channel") { value("DWELLING") } }
                }
            }

            @Test
            fun retriveSpecificMessage() {
                mockMvc.get("/API/messages/$msg1_id") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }



            }


            @Test
            fun retriveAllMessagesFiltered(){
                mockMvc.get("/API/messages?filterBy=READ") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("content") { isArray() } }
                    content { jsonPath("totalElements") { value(0) } }
                }


                mockMvc.get("/API/messages?filterBy=RECEIVED") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("content") { isArray() } }
                    content { jsonPath("totalElements") { value(1) } }
                    content { jsonPath("$.content[0].subject") { value(msg1.subject) } }
                    content { jsonPath("$.content[0].sender.email") { value(((msg1.sender)as Email ).email) } }
                    content { jsonPath("$.content[0].channel") { value("EMAIL") } }
                }
            }
            @Test
            fun retriveAllMessagesMultiFiltered(){
                mockMvc.get("/API/messages?filterBy=RECEIVED&filterBy=PROCESSING") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("content") { isArray() } }
                    content { jsonPath("totalElements") { value(2) } }
                    content { jsonPath("$.content[0].id") { value(msg1.messageID) } }
                    content { jsonPath("$.content[1].id") { value(msg3.messageID) } }
                }
            }
            @Test
            fun filterWithWrongFilter(){
                mockMvc.get("/API/messages?filterBy=aaaaa") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isBadRequest() }
                }
                mockMvc.get("/API/messages?filterBy=") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isBadRequest() }
                }


            }

            @Test
            fun pagingResponse(){
                mockMvc.get("/API/messages?page=1&size=1") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("content") { isArray() } }
                    content { jsonPath("totalElements") { value(3) } }
                    content { jsonPath("numberOfElements") { value(1) } }
                    content { jsonPath("$.content[0].id") { value(msg2.messageID) } }
                }
            }
            @Test
            fun pagingAndFiltering(){
                fun pagingResponse(){
                    mockMvc.get("/API/messages?page=0&size=1&FilterBy=RECEIVED") {
                        accept = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        content { jsonPath("content") { isArray() } }
                        content { jsonPath("totalElements") { value(1) } }
                        content { jsonPath("numberOfElements") { value(1) } }
                        content { jsonPath("$.content[0].id") { value(msg1.messageID) } }
                    }

                }
            }
        }


        @Nested
        inner class PostMessage {
            @Test
            fun createMessageTelephone() {
                val message_1 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "011-765352"
                        },
                        "channel" : "TELEPHONE",
                        "subject" : "Action required",
                        "body" : "please visit http://your-bank.com"  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Action required") } }
                    content { jsonPath("$.sender.phoneNumber") { value("011-765352") } }
                    content { jsonPath("$.channel") { value("TELEPHONE") } }
                    content { jsonPath("$.body") { value("please visit http://your-bank.com") } }
                }

                //assertEquals( (list.content[0].addresses.toList()[0] as Telephone).number,"011-765352")

                //missing subject
                val message_2 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "011-765352",
                    
                        },
                        "channel" : "TELEPHONE",
                        "body" : "please visit http://your-bank.com"  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_2
                }.andExpect {
                    status { isBadRequest() }
                }
                val message_3 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "011-765352"
                        },
                        "channel" : "telephone",
                        "subject" : "Action required",
                        "body" : "please visit http://your-bank.com"  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_3
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Action required") } }
                    content { jsonPath("$.sender.phoneNumber") { value("011-765352") } }
                    content { jsonPath("$.channel") { value("TELEPHONE") } }
                    content { jsonPath("$.body") { value("please visit http://your-bank.com") } }
                }
                val list = contactRepository.findAllByTelephone("011-765352", pageable = PageRequest.of(0,10))
                assertEquals( list.content.size,1)
                assertEquals( list.content[0].name,"Auto-generated")
                val message_4 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "+39 011765352"
                        },
                        "channel" : "teLepHonE",
                        "subject" : "Action required",
                        "body" : "please visit http://your-bank.com"  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_4
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Action required") } }
                    content { jsonPath("$.sender.phoneNumber") { value("+39 011765352") } }
                    content { jsonPath("$.channel") { value("TELEPHONE") } }
                    content { jsonPath("$.body") { value("please visit http://your-bank.com") } }
                }

                val listC =  contactRepository.findAll()
                assertEquals( listC.size,2)
                assertEquals( listC[0].name,"Auto-generated")
                assertEquals( listC[1].name,"Auto-generated")
                }

            @Test
            fun checkNoDuplicateAddressForTheSameSenderTelephone() {
                val message_1 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "011-765352"
                        },
                        "channel" : "TELEPHONE",
                        "subject" : "Action required",
                        "body" : "please visit http://your-bank.com"  
                    }
                    """.trimIndent()
                val msg_1_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Action required") } }
                    content { jsonPath("$.sender.phoneNumber") { value("011-765352") } }
                    content { jsonPath("$.channel") { value("TELEPHONE") } }
                    content { jsonPath("$.body") { value("please visit http://your-bank.com") } }
                }.andReturn().response.contentAsString).getString("id")
                val message_2 =
                    """
                    {
                        "sender":{
                            "phoneNumber": "011-765352"
                        },
                        "channel" : "telephone",
                        "subject" : "Action required x 2",
                        "body" : "please visit http://your-bank.com"  
                    }
                """.trimIndent()
                val msg_2_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_2
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Action required x 2") } }
                    content { jsonPath("$.sender.phoneNumber") { value("011-765352") } }
                    content { jsonPath("$.channel") { value("TELEPHONE") } }
                    content { jsonPath("$.body") { value("please visit http://your-bank.com") } }

                }.andReturn().response.contentAsString).getString("id")
                assertEquals(
                    messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                    messageRepository.findById(msg_2_id.toLong()).get().sender.id
                )

                assertEquals( contactRepository.findAll().size,1)

            }


            @Test
            fun createMessageDwelling() {
                val message_1 =
                    """
                    {
                        "sender":{
                            "street": "Via Roma",
                            "city": "Torino"
                           
                        },
    
                        "channel" : "dwelling",
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
                    content { jsonPath("$.channel") { value("DWELLING") } }
                    content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
                }
                val message_2 =
                    """
                    {
                        "sender":{
                            "street": "Via Roma",
                            
                        },
                         "channel" : "dwelling",
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
                assertEquals( contactRepository.findAll().size,1)
            }

            @Test
            fun checkNoDuplicateAddressForTheSameSenderDwelling() {
                val message_1 =
                    """
                    {
                        "sender":{
                            "street": "Via Roma",
                            "city": "Torino"
                        
                        },
                         "channel" : "dwelling",
                        "subject" : "Raccomandata",
                        "body" : "Gentile x, la contatto in merito..."  
                    }
                """.trimIndent()
                val msg_1_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Raccomandata") } }
                    content { jsonPath("$.sender.street") { value("Via Roma") } }
                    content { jsonPath("$.sender.city") { value("Torino") } }
                    content { jsonPath("$.channel") { value("DWELLING") } }
                    content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
                }.andReturn().response.contentAsString).getString("id")

                val message_2 =
                    """
                    {
                        "sender":{
                            "street": "Via Roma",
                            "city": "Torino",
                            "district": "Piemonte",
                            "country": "Italy"
                         
                        },
                        "channel" : "dwelling",
                        "subject" : "Raccomandata",
                        "body" : "Gentile x, la contatto in merito..."  
                    }
                """.trimIndent()
                val msg_2_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_2
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Raccomandata") } }
                    content { jsonPath("$.sender.street") { value("Via Roma") } }
                    content { jsonPath("$.sender.city") { value("Torino") } }
                    content { jsonPath("$.sender.country").doesNotExist() }  //there is already an address incomplete with the same street and district
                    content { jsonPath("$.sender.district").doesNotExist() }
                    content { jsonPath("$.channel") { value("DWELLING") } }
                    content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
                }.andReturn().response.contentAsString).getString("id")
                assertEquals(
                    messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                    messageRepository.findById(msg_2_id.toLong()).get().sender.id
                )
                assertEquals( contactRepository.findAll().size,1)


            }
            @Test
            fun createMessageEmail(){
                val message_1 =
                    """
                    {
                        "sender":{
                                "email":"a.s@gmail.com"
                     
                        },
   
                        "channel" : "email",
                        "subject" : "Raccomandata",
                        "body" : "G"  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Raccomandata") } }
                    content { jsonPath("$.sender.email") { value("a.s@gmail.com") } }
                    content { jsonPath("$.channel") { value("EMAIL") } }
                    content { jsonPath("$.body") { value("G") } }
                }
                assertEquals( contactRepository.findAll().size,1)
            }
            @Test
            fun checkNoDuplicateAddressForTheSameSenderEmail(){
                val message_1 =
                    """
                    {
                        "sender":{
                            "email":"a.s@gmail.com"
                        
                        },
                         "channel" : "email",
                        "subject" : "Raccomandata",
                        "body" : "Gentile x, la contatto in merito..."  
                    }
                """.trimIndent()
                val msg_1_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Raccomandata") } }
                    content { jsonPath("$.sender.email") { value("a.s@gmail.com") } }
                    content { jsonPath("$.channel") { value("EMAIL") } }
                    content { jsonPath("$.body") { value("Gentile x, la contatto in merito...") } }
                }.andReturn().response.contentAsString).getString("id")

                val message_2 =
                    """
                    {
                        "sender":{
                            "email":"a.s@gmail.com"
                        
                        },
                         "channel" : "email",
                        "subject" : "Email",
                        "body" : "Gentile"  
                    }
                """.trimIndent()
                val msg_2_id = JSONObject(mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_2
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.subject") { value("Email") } }
                    content { jsonPath("$.sender.email") { value("a.s@gmail.com") } }
                    content { jsonPath("$.channel") { value("EMAIL") } }
                    content { jsonPath("$.body") { value("Gentile") } }
                }.andReturn().response.contentAsString).getString("id")
                assertEquals(
                    messageRepository.findById(msg_1_id.toLong()).get().sender.id,
                    messageRepository.findById(msg_2_id.toLong()).get().sender.id
                )
                assertEquals( contactRepository.findAll().size,1)
            }


            @Test
            fun createMessageEmptyDwelling() {
                val message_1 =
                    """
                    {
                        "sender":{
                            "street": "",
                            "city": ""
                      
                        },
    
                        "channel" : "dwelling",
                        "subject" : "Raccomandata",
                        "body" : "Gentile x, la contatto in merito..."  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isUnprocessableEntity() }
                }
                val message_2 =
                    """
                    {
                        "sender":{
                            "street": "Via Roma",
                            "city":"To"
                        },
                         "channel" : "dwelling",
                        "subject" : "",
                        "body" : ""  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_2
                }.andExpect {
                    status { isUnprocessableEntity() }

                }

                assertEquals( contactRepository.findAll().size,0)
            }
            @Test
            fun createMessageEmptyMixingAddress()
            {
                val message_1 =
                    """
                    {
                        "sender":{
                            "city": "TO",
                            "street":"Tdr",
                            "phoneNumber": "3423"
                        
                        },
    
                        "channel" : "dwelling",
                        "subject" : "Raccomandata",
                        "body" : "Gentile x, la contatto in merito..."  
                    }
                """.trimIndent()
                mockMvc.post("/API/messages") {
                    contentType = MediaType.APPLICATION_JSON
                    content = message_1
                }.andExpect {
                    status { isBadRequest() }
                }

                assertEquals( contactRepository.findAll().size,0)
            }

        }

    }

    @Nested
    inner class Priority{
        @Test
        fun getPriority() {
            mockMvc.get("/API/messages/$msg1_id")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.priority") { value("0") } }
                }
        }

        @Test
        fun putPriority() {
            mockMvc.put("/API/messages/$msg1_id/priority") {
                content = """{"priority":3}"""
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
        fun putPriorityNotValid() {
            mockMvc.put("/API/messages/$msg1_id/priority") {
                content = """{"priority":-1}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
            mockMvc.put("/API/messages/$msg1_id/priority") {
                content = """{"priority":"A"}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
            mockMvc.put("/API/messages/$msg1_id/priority") {
                content = """"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

    }

    @Nested
    inner class MessageHistory {
        @Test
        fun checkEventWhenGetMessageId() {
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
        fun getMessageHistory() {
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
            @Test
            fun postMessageHistoryValid() {
                mockMvc.post("/API/messages/$msg1_id") {
                    content = """{"status":"READ","comments":"Read immediatly"}""""
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.status") { value("READ") } }
                    content { jsonPath("$.timestamp").exists() }
                    content { jsonPath("$.comments") { value("Read immediatly") } }
                }

                mockMvc.post("/API/messages/$msg1_id") {
                    content = """{"status":"DONE"}""""
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { jsonPath("$.status") { value("DONE") } }
                    content { jsonPath("$.timestamp").exists() }
                    content { jsonPath("$.comments").doesNotExist() }
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
            fun postMessageHistoryInvalid() {
                mockMvc.post("/API/messages/435643653") {
                    content = """{"status":"READ","comments":"Read immediatly"}""""
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isNotFound() }
                }

                mockMvc.post("/API/messages/$msg1_id") {
                    content = """{"status":"Read","comments":"Read immediatly"}""""
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    content { jsonPath("$.detail") { value("Failed to read request") } }
                }

                mockMvc.post("/API/messages/$msg1_id") {
                    content = """{"status":"FAILED","comments":"Read immediatly"}""""
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    content { jsonPath("$.detail") { value("The status cannot be assigned to the message") } }
                }
            }
        }

    }

}