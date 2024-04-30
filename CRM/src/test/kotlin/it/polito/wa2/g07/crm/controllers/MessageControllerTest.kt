package it.polito.wa2.g07.crm.controllers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.MessageStatus
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
import org.junit.jupiter.api.Assertions.*

@WebMvcTest(MessageController::class)
class MessageControllerTest (@Autowired val mockMvc: MockMvc){

    @MockkBean
    private lateinit var messageService: MessageService
    private val mockEmailDTO = EmailDTO("mario.rossi@example.org")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")
    private val mockMessageEventDTO =MessageEventDTO(MessageStatus.RECEIVED, LocalDateTime.now(),"test comment")
    private val mockMessageDTO1 = MessageDTO(
        1,
        mockEmailDTO,
        "test subject",
        "test body",
        1,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO1= ReducedMessageDTO(
        mockMessageDTO1.id,
        mockMessageDTO1.subject,
        mockMessageDTO1.sender
    )
    private val mockMessageDTO2 = MessageDTO(
        2,
        mockTelephoneDTO,
        "test subject",
        "test body",
        2,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO2= ReducedMessageDTO(
        mockMessageDTO2.id,
        mockMessageDTO2.subject,
        mockMessageDTO2.sender
    )

    private val mockMessageDTO3 = MessageDTO(
        3,
        mockDwellingDTO,
        "test subject",
        "test body",
        3,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO3= ReducedMessageDTO(
        mockMessageDTO3.id,
        mockMessageDTO3.subject,
        mockMessageDTO3.sender
    )


    @Nested
    inner class GetMessageTest {
        private val pageImpl = PageImpl(listOf(mockReducedMessageDTO1,mockReducedMessageDTO2,mockReducedMessageDTO3))
       // private val query ="query"

        @BeforeEach
        fun initMocks(){
            every { messageService.getMessages(any(Pageable::class)) } returns pageImpl
            every { messageService.getMessage(any(Long::class))  } returns mockMessageDTO1
            every { messageService.getHistory(any(Long::class),any(Pageable::class))  } returns PageImpl(listOf(mockMessageEventDTO))
        }

        @Test
        fun getMessages_noParams() {

            mockMvc
                .get("/API/messages")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getMessages(PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.content[0].subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.content[0].sender.channel") { value("email")}
                        jsonPath("$.content[0].sender.email") { value(mockEmailDTO.email)}
                        jsonPath("$.content[1].id"){value(mockReducedMessageDTO2.id)}
                        jsonPath("$.content[1].subject"){value(mockReducedMessageDTO2.subject)}
                        jsonPath("$.content[1].sender.channel"){value("phone")}
                        jsonPath("$.content[1].sender.phoneNumber"){value(mockTelephoneDTO.phoneNumber)}
                        jsonPath("$.content[2].id"){value(mockReducedMessageDTO3.id)}
                        jsonPath("$.content[2].subject"){value(mockReducedMessageDTO3.subject)}
                        jsonPath("$.content[2].sender.channel"){value("dwelling")}
                        jsonPath("$.content[2].sender.street"){value(mockDwellingDTO.street)}
                        jsonPath("$.content[2].sender.city"){value(mockDwellingDTO.city)}
                        jsonPath("$.content[2].sender.district"){value(mockDwellingDTO.district)}
                        jsonPath("$.content[2].sender.country"){value(mockDwellingDTO.country)}
                    }

                }
        }

        @Test
        fun getMessage_Id() {

            mockMvc
                .get("/API/messages/1")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getMessage(1) }
                    content {
                        jsonPath("$.id") { value(mockReducedMessageDTO1.id) }
                        jsonPath("$.subject") { value(mockReducedMessageDTO1.subject) }
                        jsonPath("$.sender.channel") { value("email")}
                        jsonPath("$.sender.email") { value(mockEmailDTO.email)}

                    }
                }
        }

        @Test
        fun getMessage_StateChange(){
            mockMvc
                .get("/API/messages/1/history")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) { messageService.getHistory(1,PageRequest.of(0, 20)) }
                    content {
                        jsonPath("$.content[0].status") { value(mockMessageEventDTO.status.toString()) }
                        jsonPath("$.content[0].timestamp") { value(mockMessageEventDTO.timestamp.toString().dropLast(2)) }
                        jsonPath("$.content[0].comments") { value(mockMessageEventDTO.comments)}


                    }
                }


        }

    }

}