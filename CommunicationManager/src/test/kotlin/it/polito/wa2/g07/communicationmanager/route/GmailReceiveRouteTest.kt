package it.polito.wa2.g07.communicationmanager.route


import org.apache.camel.CamelContext
import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.AdviceWith
import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpoints
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.apache.camel.test.spring.junit5.UseAdviceWith


@SpringBootTest
@CamelSpringBootTest
@MockEndpoints
@UseAdviceWith
internal class GmailReceiveRouteTest {

    @Autowired
    private lateinit var template: ProducerTemplate

    @EndpointInject("mock:http://localhost:8080/API/messages")
    private lateinit var mock: MockEndpoint

    @Autowired
    private lateinit var context: CamelContext


    @Test
    @Throws(InterruptedException::class)
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
            a.mockEndpoints()
             a.weaveAddLast().log("Message sent to endpoint: \${body}") // Log finale della rotta

        }

        // Assicurati che la route sia avviata
        context.start()


        mock.expectedMessageCount(1)
        mock.expectedHeaderReceived(Exchange.HTTP_METHOD, "POST")
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

        //send mock email id to the route
        template.sendBodyAndHeaders("direct:RetriveMail",null , testHeaders)


//        mock.setResultWaitTime(30000)
        mock.assertIsSatisfied()
    }
}