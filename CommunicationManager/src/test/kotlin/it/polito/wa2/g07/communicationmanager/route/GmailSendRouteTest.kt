package it.polito.wa2.g07.communicationmanager.route

import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import com.google.api.services.gmail.model.Message
import org.springframework.beans.factory.annotation.Value
import java.util.*


@SpringBootTest
@CamelSpringBootTest
@MockEndpointsAndSkip("google-mail:messages/send")
internal class GmailSendRouteTest {
    @Autowired
    private lateinit var template: ProducerTemplate

    @EndpointInject("mock:google-mail:messages/send")
    private lateinit var mock: MockEndpoint

    @Value("\${gmail.username}")
    private lateinit var from: String

    @Test
    @Throws(InterruptedException::class)
    fun sendEmail_success() {
        val headers = mapOf(
            "From" to from,
            "To" to "receiver@example.org",
            "Subject" to "This is a subject"
        )
        val body = "This is a body"
        template.sendBodyAndHeaders("seda:sendEmail", body, headers)

        val expectedRaw = """
            To: ${headers["To"]}
            From: ${headers["From"]}
            Subject: ${headers["Subject"]}
            Content-Type: text/plain; charset=UTF-8
            
            $body
        """.trimIndent()
        val expectedRawEncoded = Base64.getEncoder().encodeToString(expectedRaw.toByteArray(Charsets.UTF_8))

        mock.expectedHeaderReceived("CamelGoogleMail.content", Message().setRaw(expectedRawEncoded))
        mock.assertIsSatisfied()
    }
}