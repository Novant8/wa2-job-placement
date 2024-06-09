package it.polito.wa2.g07.communicationmanager.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.communicationmanager.dtos.SendEmailDTO
import it.polito.wa2.g07.communicationmanager.services.EmailService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(EmailController::class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = ["operator"])
class EmailControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var emailService: EmailService

    @BeforeEach
    fun initMocks() {
        every { emailService.sendEmail(any(SendEmailDTO::class)) } returns Unit
    }

    @Test
    fun sendMail_success() {
        val sendEmailDTO = SendEmailDTO(
            to = "receiver@example.org",
            subject = "This is a subject",
            body = "This is a body"
        )
        mockMvc.post("/API/emails") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(sendEmailDTO)
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun sendMail_invalidRequest() {
        val invalidEmaiDTOs = listOf(
            SendEmailDTO(to = "not a mail", subject = "This is a subject", body = "This is a body"),
            SendEmailDTO(to = "receiver@example.org", subject = ""),
            SendEmailDTO(to = "receiver@example.org", subject = " ")
        )

        for (sendEmailDTO in invalidEmaiDTOs) {
            mockMvc.post("/API/emails") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(sendEmailDTO)
            }.andExpect {
                status { isUnprocessableEntity() }
            }
        }
    }

}
