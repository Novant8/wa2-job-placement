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


@WebMvcTest(MessageController::class)
class MessageControllerTest (@Autowired val mockMvc: MockMvc){

    @MockkBean
    private lateinit var messageService: MessageService
    private val mockEmailDTO = EmailDTO("mario.rossi@example.org")
    //private val mockAddressDTO= AddressDTO()
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")
    private val mockMessageEventDTO =MessageEventDTO(MessageStatus.RECEIVED, LocalDateTime.now(),"test comment")
    private val mockMessageDTO = MessageDTO(
        1,
        mockEmailDTO,
        "test subject",
        "test body",
        1,
        LocalDateTime.now(),
        mockMessageEventDTO
    )
    private val mockReducedMessageDTO= ReducedMessageDTO(
        mockMessageDTO.id,
        mockMessageDTO.subject,
        mockMessageDTO.sender
    )


    @Nested
    inner class GetMessageTest {
        private val pageImpl = PageImpl(listOf(mockReducedMessageDTO))
        private val query ="query"

        @BeforeEach
        fun initMocks(){
            every { messageService.getMessages(any(Pageable::class)) } returns pageImpl
        }

        @Test
        fun getMessages_noParams(){
            mockMvc
                .get("/API/messages")
                .andExpect {
                    status { isOk() }
                    verify(exactly = 1) {messageService.getMessages(PageRequest.of(0,20))  }
                    content {
                        jsonPath("$.content[0].id"){value(mockReducedMessageDTO.id)}
                        jsonPath("$.content[0].subject"){value(mockReducedMessageDTO.subject)}
                        jsonPath("$.content[0].sender"){isMap()}

                    }
                }
        }



    }



}