package it.polito.wa2.g07.communicationmanager.route


import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.apache.camel.*
import org.apache.camel.builder.AdviceWith
import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpoints
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip
import org.apache.camel.test.spring.junit5.UseAdviceWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
@CamelSpringBootTest
@MockEndpoints
@UseAdviceWith
@MockEndpointsAndSkip("mail")
internal class GmailReceiveRouteTest {

    @Autowired
    private lateinit var template: ProducerTemplate

    @EndpointInject("mock:foo")
    private lateinit var mock: MockEndpoint

    @Autowired
    private lateinit var context: CamelContext


    @Test
    fun receivedEmail_success(){
        val emailId = "18f96c7a0d3a8c5a"
        val testHeaders = mapOf(
            "CamelGoogleMailId" to emailId
        )

        //create mock email
//        val message = Message().apply {
//            id = emailId
//            payload = MessagePart().apply {
//                headers = listOf(
//                    MessagePartHeader().apply {
//                        name= "From"
//                        value= "sender@example.com"
//                    },
//                    MessagePartHeader().apply {
//                        name= "Subject"
//                        value= "This is a subject"
//                    }
//                )
//                body = MessagePartBody().apply {
//                    data = Base64.getEncoder().encodeToString("This is the body".toByteArray(Charsets.UTF_8))
//                }
//            }
//        }


        AdviceWith.adviceWith(
            context, "RetriveMail"
        ) { a: AdviceWithRouteBuilder ->
            a.replaceFromWith("direct:RetriveMail")
            a.weaveByToUri("http://localhost:8080/API/messages").replace().to("mock:foo")
            a.weaveAddLast().log("Message sent to endpoint: \${body}") // Log finale della rotta

        }
        // Assicurati che la route sia avviata
        context.start()
        assertEquals(ServiceStatus.Started, context.getStatus());
        template.sendBodyAndHeaders("direct:RetriveMail",null , testHeaders)

        mock.expectedHeaderReceived(Exchange.HTTP_METHOD, "POST")
        mock.expectedBodyReceived()
        mock.expectedHeaderReceived(Exchange.CONTENT_TYPE, "application/json")
        mock.expectedBodiesReceived("""
            {
                "sender": {
                    "email": "sordello.andrea@gmail.com"
                },
                "subject": "mail2",
                "body": "test mail"
            }
        """.trimIndent())



        MockEndpoint.assertIsSatisfied(context)


    }

    @Test
    fun receivedEmail_insuccess(){
        val emailId = "INVALID_ID"
        val testHeaders = mapOf(
            "CamelGoogleMailId" to emailId
        )


        AdviceWith.adviceWith(
            context, "RetriveMail"
        ) { a: AdviceWithRouteBuilder ->
            a.replaceFromWith("direct:RetriveMail")
          //  a.weaveByToUri("http://localhost:8080/API/messages").replace().to("mock:foo")
            a.weaveAddLast().log("Message sent to endpoint: \${body}") // Log finale della rotta
            a.weaveAddLast().to("mock:foo")
        }
        // Assicurati che la route sia avviata
        context.start()
        assertEquals(ServiceStatus.Started, context.getStatus());

        assertThrows(CamelExecutionException::class.java) {
            template.sendBodyAndHeaders("direct:RetriveMail",null , testHeaders)
        }





    }
}