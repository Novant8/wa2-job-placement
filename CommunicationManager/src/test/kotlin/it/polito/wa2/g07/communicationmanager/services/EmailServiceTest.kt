package it.polito.wa2.g07.communicationmanager.services

import it.polito.wa2.g07.communicationmanager.dtos.SendEmailDTO
import org.apache.camel.CamelContext
import org.apache.camel.EndpointInject
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip
import org.apache.camel.test.spring.junit5.UseAdviceWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@MockEndpointsAndSkip("seda:sendEmail")
class EmailServiceTest {

    @EndpointInject("mock:seda:sendEmail")
    private lateinit var routeMock: MockEndpoint

    @Value("\${gmail.username}")
    private lateinit var from: String

    @Autowired
    private lateinit var service: EmailService

    @Autowired
    private lateinit var context: CamelContext

    @BeforeEach
    fun startContext() {
        context.start()
    }

    @Test
    fun sendEmail_success() {
        val sendEmailDTO = SendEmailDTO(
            to = "receiver@example.org",
            subject = "This is a subject",
            body = "This is a body"
        )
        service.sendEmail(sendEmailDTO)

        routeMock.assertIsSatisfied()

        val headerFrom = routeMock.exchanges[0]?.getIn()?.getHeader("From")
        val to = routeMock.exchanges[0]?.getIn()?.getHeader("To")
        val subject = routeMock.exchanges[0]?.getIn()?.getHeader("Subject")
        val body = routeMock.exchanges[0]?.getIn()?.body
        assertEquals(headerFrom, from)
        assertEquals(to, sendEmailDTO.to)
        assertEquals(subject, sendEmailDTO.subject)
        assertEquals(body, sendEmailDTO.body)
    }

}