package it.polito.wa2.g07.crm.controllers

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.AddressType
import it.polito.wa2.g07.crm.entities.MessageStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.services.MessageService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.get
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put


@WebMvcTest(MessageController::class)
class MessageControllerTest (@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var messageService: MessageService
    private val mockEmailDTO = EmailDTO("mario.rossi@example.org")
    private val mockInvalidEmailDTO = EmailDTO("I'm not valid")
    private val mockInvalidTelephoneDTO = TelephoneDTO("I'm not valid")
    private val mockInvalidDwellingDTO = DwellingDTO("","","","")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")
    private val mockMessageEventDTO = MessageEventDTO(MessageStatus.RECEIVED, LocalDateTime.now(), "received")
    private val mockMessageEventDTO_read = MessageEventDTO(MessageStatus.READ, null, "read")
    private val mockMessageEventDTO_discarded = MessageEventDTO(MessageStatus.DISCARDED, null, "discarded")
    private val mockMessageEventDTO_processing = MessageEventDTO(MessageStatus.PROCESSING, null, "processing")
    private val mockMessageEventDTO_done = MessageEventDTO(MessageStatus.DONE, null, "done")
    private val mockMessageEventDTO_failed = MessageEventDTO(MessageStatus.FAILED, null, "failed")
    private val mockMessageDTO1 = MessageDTO(
        1,
        mockEmailDTO,
        AddressType.EMAIL,
        "test subject",
        "test body",
        1,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO1 = ReducedMessageDTO(
        mockMessageDTO1.id,
        mockMessageDTO1.subject,
        mockMessageDTO1.sender,
        mockMessageDTO1.channel.toString()

    )
    private val mockMessageDTO2 = MessageDTO(
        2,
        mockTelephoneDTO,
        AddressType.TELEPHONE,
        "test subject",
        "test body",
        2,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO2 = ReducedMessageDTO(
        mockMessageDTO2.id,
        mockMessageDTO2.subject,
        mockMessageDTO2.sender,
        mockMessageDTO2.channel.toString()
    )

    private val mockMessageDTO3 = MessageDTO(
        3,
        mockDwellingDTO,
        AddressType.DWELLING,
        "test subject",
        "test body",
        3,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO3 = ReducedMessageDTO(
        mockMessageDTO3.id,
        mockMessageDTO3.subject,
        mockMessageDTO3.sender,
        mockMessageDTO3.channel.toString()
    )


    @Nested
    inner class GetMessageTest {
        private val pageImpl = PageImpl(listOf(mockReducedMessageDTO1, mockReducedMessageDTO2, mockReducedMessageDTO3))


        @BeforeEach
        fun initMocks() {
            every { messageService.getMessages(any(), any(Pageable::class)) } returns pageImpl
            every { messageService.getMessage(any(Long::class)) } returns mockMessageDTO1
            every { messageService.getHistory(any(Long::class), any(Pageable::class)) } returns PageImpl(
                listOf(
                    mockMessageEventDTO
                )
            )
        }

        @Test
        fun getMessages_noParams() {

            mockMvc
                .get("/API/messages")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getMessages(filterBy = null, PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.content[0].subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.content[0].channel") { value(mockMessageDTO1.channel.toString()) }
                        jsonPath("$.content[0].sender.email") { value(mockEmailDTO.email) }
                        jsonPath("$.content[1].id") { value(mockReducedMessageDTO2.id) }
                        jsonPath("$.content[1].subject") { value(mockReducedMessageDTO2.subject) }
                        jsonPath("$.content[1].channel") { value(mockMessageDTO2.channel.toString()) }
                        jsonPath("$.content[1].sender.phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.content[2].id") { value(mockReducedMessageDTO3.id) }
                        jsonPath("$.content[2].subject") { value(mockReducedMessageDTO3.subject) }
                        jsonPath("$.content[2].channel") { value(mockMessageDTO3.channel.toString()) }
                        jsonPath("$.content[2].sender.street") { value(mockDwellingDTO.street) }
                        jsonPath("$.content[2].sender.city") { value(mockDwellingDTO.city) }
                        jsonPath("$.content[2].sender.district") { value(mockDwellingDTO.district) }
                        jsonPath("$.content[2].sender.country") { value(mockDwellingDTO.country) }
                    }

                }
        }

        @Test
        fun getMessages_filterParam() {

            mockMvc
                .get("/API/messages")
                {
                    queryParam("filterBy", MessageStatus.RECEIVED.toString())

                }
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) {
                        messageService.getMessages(
                            filterBy = listOf(MessageStatus.RECEIVED),
                            PageRequest.of(0, 20)
                        )
                    }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.content[0].subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.content[0].channel") { value(mockMessageDTO1.channel.toString()) }
                        jsonPath("$.content[0].sender.email") { value(mockEmailDTO.email) }
                        jsonPath("$.content[1].id") { value(mockReducedMessageDTO2.id) }
                        jsonPath("$.content[1].subject") { value(mockReducedMessageDTO2.subject) }
                        jsonPath("$.content[1].channel") { value(mockMessageDTO2.channel.toString()) }
                        jsonPath("$.content[1].sender.phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.content[2].id") { value(mockReducedMessageDTO3.id) }
                        jsonPath("$.content[2].subject") { value(mockReducedMessageDTO3.subject) }
                        jsonPath("$.content[2].channel") { value(mockMessageDTO3.channel.toString()) }
                        jsonPath("$.content[2].sender.street") { value(mockDwellingDTO.street) }
                        jsonPath("$.content[2].sender.city") { value(mockDwellingDTO.city) }
                        jsonPath("$.content[2].sender.district") { value(mockDwellingDTO.district) }
                        jsonPath("$.content[2].sender.country") { value(mockDwellingDTO.country) }
                    }

                }
        }

        @Test
        fun getMessages_filterParam_caseInsensitive() {

            mockMvc
                .get("/API/messages")
                {
                    queryParam("filterBy", MessageStatus.RECEIVED.toString().lowercase())

                }
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) {
                        messageService.getMessages(
                            filterBy = listOf(MessageStatus.RECEIVED),
                            PageRequest.of(0, 20)
                        )
                    }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.content[0].subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.content[0].channel") { value(mockMessageDTO1.channel.toString()) }
                        jsonPath("$.content[0].sender.email") { value(mockEmailDTO.email) }
                        jsonPath("$.content[1].id") { value(mockReducedMessageDTO2.id) }
                        jsonPath("$.content[1].subject") { value(mockReducedMessageDTO2.subject) }
                        jsonPath("$.content[1].channel") { value(mockMessageDTO2.channel.toString()) }
                        jsonPath("$.content[1].sender.phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.content[2].id") { value(mockReducedMessageDTO3.id) }
                        jsonPath("$.content[2].subject") { value(mockReducedMessageDTO3.subject) }
                        jsonPath("$.content[2].channel") { value(mockMessageDTO3.channel.toString()) }
                        jsonPath("$.content[2].sender.street") { value(mockDwellingDTO.street) }
                        jsonPath("$.content[2].sender.city") { value(mockDwellingDTO.city) }
                        jsonPath("$.content[2].sender.district") { value(mockDwellingDTO.district) }
                        jsonPath("$.content[2].sender.country") { value(mockDwellingDTO.country) }
                    }

                }
        }

        @Test
        fun getMessages_filterParam_emptyFilter() {

            mockMvc
                .get("/API/messages")
                {
                    queryParam("filterBy", "")

                }
                .andExpect {
                    status { isBadRequest() }


                }
        }

        @Test
        fun getMessages_filterParam_invalidFilter() {

            mockMvc
                .get("/API/messages")
                {
                    queryParam("filterBy", "not a valid filter")

                }
                .andExpect {
                    status { isBadRequest() }


                }
        }

        @Test
        fun getMessage_Id() {
            val messageID= mockMessageDTO1.id
            mockMvc
                .get("/API/messages/$messageID")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getMessage(messageID) }
                    content {
                        jsonPath("$.id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.channel") { value(mockMessageDTO1.channel.toString()) }
                        jsonPath("$.sender.email") { value(mockEmailDTO.email) }

                    }
                }
        }
        /*@Test
        fun getMessage_Id_NotFound() {
            mockMvc
                .get("/API/messages/2000"){
                    accept = MediaType.APPLICATION_JSON
                }
                .andExpect {
                    status { isNotFound() }

                }
        }*/

        @Test
        fun getMessage_StateChange() {
            mockMvc
                .get("/API/messages/1/history")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getHistory(1, PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].status") { value(mockMessageEventDTO.status.toString()) }
                        jsonPath("$.content[0].timestamp") { isString()/*value(mockMessageEventDTO.timestamp.toString())*/ }
                        jsonPath("$.content[0].comments") { value(mockMessageEventDTO.comments) }


                    }
                }


        }

    }

    @Nested
    inner class PostMessageTests {
        @Nested
        inner class PostMessageTestEmail {
            @BeforeEach
            fun initMocks() {
                every { messageService.createMessage(any(MessageCreateDTO::class)) } answers {
                    MessageDTO(
                        mockMessageDTO1.id,
                        mockMessageDTO1.sender,
                        mockMessageDTO1.channel,
                        mockMessageDTO1.subject,
                        mockMessageDTO1.body,
                        mockMessageDTO1.priority,
                        mockMessageDTO1.creationTimestamp,
                        mockMessageDTO1.lastEvent
                    )
                }
                every { messageService.updateStatus(any(Long::class),any(MessageEventDTO::class) )} answers {
                    MessageEventDTO(
                        mockMessageEventDTO_read.status,
                        mockMessageEventDTO_read.timestamp,
                        mockMessageEventDTO_read.comments
                    )
                }
            }

            @Test
            fun createMessage_test1() {
                val createMessageDTO = MessageCreateDTO(
                    mockEmailDTO,
                    AddressType.EMAIL.toString(),
                    "test subject",
                    "test body"
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isCreated() }
                        content {
                            jsonPath("$.sender.email") { value(mockEmailDTO.email) }
                            jsonPath("$.channel") { value(createMessageDTO.channel) }
                            jsonPath("$.subject") { value(createMessageDTO.subject) }
                            jsonPath("$.body") { value(createMessageDTO.body) }
                        }
                    }


            }


            @Test
            fun createMessage_wrongChannelType() {
                val createMessageDTO = MessageCreateDTO(
                    mockEmailDTO,
                    AddressType.TELEPHONE.toString(),
                    "test subject",
                    "test body"
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isBadRequest() }

                    }
            }

            @Test
            fun createMessage_EmptySubject() {
                val createMessageDTO = MessageCreateDTO(
                    mockEmailDTO,
                    AddressType.EMAIL.toString(),
                    "",
                    "test body"
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }

                    }
            }

            @Test
            fun createMessage_EmptyBody() {
                val createMessageDTO = MessageCreateDTO(
                    mockEmailDTO,
                    AddressType.EMAIL.toString(),
                    "test subject",
                    ""
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }

                    }
            }

            @Test
            fun createMessage_EmptyChannel() {
                val createMessageDTO = MessageCreateDTO(
                    mockEmailDTO,
                    "",
                    "test subject",
                    "test body"
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }
                    }
            }

            @Test
            fun createMessage_InvalidSender() {
                val createMessageDTO = MessageCreateDTO(
                    mockInvalidEmailDTO,
                    AddressType.EMAIL.toString(),
                    "test subject",
                    "test body"
                )
                mockMvc
                    .post("/API/messages")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(createMessageDTO)
                    }
                    .andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
            @Test
            fun changeMessageState_success(){
                val messageId=mockMessageDTO1.id
                val newState= mockMessageEventDTO_read
                mockMvc
                    .post("/API/messages/$messageId")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(newState)
                }
                    .andExpect {
                        status { isCreated() }
                        content {
                            jsonPath("$.status") { value(newState.status.toString()) }

                        }
                    }


            }
            @Test
            fun changeMessageState_InvalidState(){
                val messageId=mockMessageDTO1.id
                val newState= "not valid"
                mockMvc
                    .post("/API/messages/$messageId")
                    {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(newState)
                    }
                    .andExpect {
                        status { isBadRequest() }

                    }


            }
        }
    }
    @Nested
    inner class PostMessageTestTelephone {
        @BeforeEach
        fun initMocks() {
            every { messageService.createMessage(any(MessageCreateDTO::class)) } answers {
                MessageDTO(
                    mockMessageDTO2.id,
                    mockMessageDTO2.sender,
                    mockMessageDTO2.channel,
                    mockMessageDTO2.subject,
                    mockMessageDTO2.body,
                    mockMessageDTO2.priority,
                    mockMessageDTO2.creationTimestamp,
                    mockMessageDTO2.lastEvent
                )
            }
            every { messageService.updateStatus(any(Long::class),any(MessageEventDTO::class) )} answers {
                MessageEventDTO(
                    mockMessageEventDTO_done.status,
                    mockMessageEventDTO_done.timestamp,
                    mockMessageEventDTO_done.comments
                )
            }
        }

        @Test
        fun createMessage_test2() {
            val createMessageDTO = MessageCreateDTO(
                mockTelephoneDTO,
                AddressType.TELEPHONE.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.sender.phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.channel") { value(createMessageDTO.channel) }
                        jsonPath("$.subject") { value(createMessageDTO.subject) }
                        jsonPath("$.body") { value(createMessageDTO.body) }
                    }
                }


        }


        @Test
        fun createMessage_wrongChannelType() {
            val createMessageDTO = MessageCreateDTO(
                mockTelephoneDTO,
                AddressType.EMAIL.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isBadRequest() }

                }
        }

        @Test
        fun createMessage_EmptySubject() {
            val createMessageDTO = MessageCreateDTO(
                mockTelephoneDTO,
                AddressType.TELEPHONE.toString(),
                "",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }

                }
        }

        @Test
        fun createMessage_EmptyBody() {
            val createMessageDTO = MessageCreateDTO(
                mockTelephoneDTO,
                AddressType.TELEPHONE.toString(),
                "test subject",
                ""
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }

                }
        }

        @Test
        fun createMessage_EmptyChannel() {
            val createMessageDTO = MessageCreateDTO(
                mockTelephoneDTO,
                "",
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }
                }
        }

        @Test
        fun createMessage_InvalidSender() {
            val createMessageDTO = MessageCreateDTO(
                mockInvalidTelephoneDTO,
                AddressType.TELEPHONE.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }
                }
        }
        @Test
        fun changeMessageState_success(){
            val messageId=mockMessageDTO2.id
            val newState= mockMessageEventDTO_done
            mockMvc
                .post("/API/messages/$messageId")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(newState)
                }
                .andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.status") { value(newState.status.toString()) }

                    }
                }


        }
        @Test
        fun changeMessageState_InvalidState(){
            val messageId=mockMessageDTO2.id
            val newState= "not valid"
            mockMvc
                .post("/API/messages/$messageId")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(newState)
                }
                .andExpect {
                    status { isBadRequest() }

                }


        }
    }
    @Nested
    inner class PostMessageTestDwelling {
        @BeforeEach
        fun initMocks() {
            every { messageService.createMessage(any(MessageCreateDTO::class)) } answers {
                MessageDTO(
                    mockMessageDTO3.id,
                    mockMessageDTO3.sender,
                    mockMessageDTO3.channel,
                    mockMessageDTO3.subject,
                    mockMessageDTO3.body,
                    mockMessageDTO3.priority,
                    mockMessageDTO3.creationTimestamp,
                    mockMessageDTO3.lastEvent
                )
            }
            every { messageService.updateStatus(any(Long::class),any(MessageEventDTO::class) )} answers {
                MessageEventDTO(
                    mockMessageEventDTO_failed.status,
                    mockMessageEventDTO_failed.timestamp,
                    mockMessageEventDTO_failed.comments
                )
            }
        }

        @Test
        fun createMessage_test3() {
            val createMessageDTO = MessageCreateDTO(
                mockDwellingDTO,
                AddressType.DWELLING.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.sender.city") { value(mockDwellingDTO.city) }
                        jsonPath("$.sender.street") { value(mockDwellingDTO.street) }
                        jsonPath("$.sender.country") { value(mockDwellingDTO.country) }
                        jsonPath("$.sender.district") { value(mockDwellingDTO.district) }
                        jsonPath("$.channel") { value(createMessageDTO.channel) }
                        jsonPath("$.subject") { value(createMessageDTO.subject) }
                        jsonPath("$.body") { value(createMessageDTO.body) }
                    }
                }


        }


        @Test
        fun createMessage_wrongChannelType() {
            val createMessageDTO = MessageCreateDTO(
                mockDwellingDTO,
                AddressType.EMAIL.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isBadRequest() }

                }
        }

        @Test
        fun createMessage_EmptySubject() {
            val createMessageDTO = MessageCreateDTO(
                mockDwellingDTO,
                AddressType.DWELLING.toString(),
                "",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }

                }
        }

        @Test
        fun createMessage_EmptyBody() {
            val createMessageDTO = MessageCreateDTO(
                mockDwellingDTO,
                AddressType.DWELLING.toString(),
                "test subject",
                ""
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }

                }
        }

        @Test
        fun createMessage_EmptyChannel() {
            val createMessageDTO = MessageCreateDTO(
                mockDwellingDTO,
                "",
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }
                }
        }

        @Test
        fun createMessage_InvalidSender() {
            val createMessageDTO = MessageCreateDTO(
                mockInvalidDwellingDTO,
                AddressType.DWELLING.toString(),
                "test subject",
                "test body"
            )
            mockMvc
                .post("/API/messages")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createMessageDTO)
                }
                .andExpect {
                    status { isUnprocessableEntity() }
                }
        }

        @Test
        fun changeMessageState_success(){
            val messageId=mockMessageDTO3.id
            val newState= mockMessageEventDTO_failed
            mockMvc
                .post("/API/messages/$messageId")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(newState)
                }
                .andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.status") { value(newState.status.toString()) }

                    }
                }


        }
        @Test
        fun changeMessageState_InvalidState(){
            val messageId=mockMessageDTO3.id
            val newState= "not valid"
            mockMvc
                .post("/API/messages/$messageId")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(newState)
                }
                .andExpect {
                    status { isBadRequest() }

                }


        }
    }


    @Nested
    inner class PutMessageTests {
        @BeforeEach
        fun initMocks(){

            every {messageService.changePriority(any(Long::class),any(Int::class)) } throws EntityNotFoundException("Message Not Found")
            every {messageService.changePriority(mockMessageDTO1.id,any(Int::class))} answers {
                MessageDTO(
                    mockMessageDTO1.id,
                    mockMessageDTO1.sender,
                    mockMessageDTO1.channel,
                    mockMessageDTO1.subject,
                    mockMessageDTO1.body,
                    2,
                    mockMessageDTO1.creationTimestamp,
                    mockMessageDTO1.lastEvent
                )
            }


        }
        @Test
        fun changeMessagePriority_success(){
            val messageId=mockMessageDTO1.id
            val updatedPriority=  mapOf("priority" to 2)
            mockMvc
                .put("/API/messages/$messageId/priority")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedPriority)

                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.priority") {value(updatedPriority["priority"])}

                    }
                }
        }
        @Test
        fun changeMessagePriority_negative(){
            val messageId=mockMessageDTO1.id
            val updatedPriority=  mapOf("priority" to -1)
            mockMvc
                .put("/API/messages/$messageId/priority")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedPriority)

                }.andExpect {
                    status { isBadRequest() }

                }
        }
        @Test
        fun changeMessagePriority_blank(){
            val messageId=mockMessageDTO1.id
            val updatedPriority=  mapOf("priority" to "")
            mockMvc
                .put("/API/messages/$messageId/priority")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedPriority)

                }.andExpect {
                    status { isBadRequest() }

                }
        }
        @Test
        fun changeMessagePriority_MessageNotFound(){
            val messageId=mockMessageDTO1.id+50
            val updatedPriority=  mapOf("priority" to 2)
            mockMvc
                .put("/API/messages/$messageId/priority")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedPriority)

                }.andExpect {
                    status { isNotFound() }

                }
        }
    }

}

