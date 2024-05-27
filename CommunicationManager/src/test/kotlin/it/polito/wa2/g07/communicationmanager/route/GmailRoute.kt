package it.polito.wa2.g07.communicationmanager.route


import org.apache.camel.CamelContext
import org.apache.camel.Endpoint
import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.AdviceWith
import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.logging.Logger


@SpringBootTest
@CamelSpringBootTest
@MockEndpointsAndSkip
 class GmailRoute {

    @Autowired
    private lateinit var defaultContext: CamelContext

    @Autowired
    private lateinit var template: ProducerTemplate

    @EndpointInject("mock:google-mail:messages/get")
    private  lateinit var mockMail: MockEndpoint

    @EndpointInject("mock:direct:foo")
    private lateinit var mockInput: MockEndpoint

    @EndpointInject("mock:http://localhost:8080/API/messages")
    private lateinit var  mockResult:MockEndpoint

    @Autowired
    private lateinit var context:CamelContext


    @Test
    fun whenSendBody_thenGreetingReceivedSuccessfully() {
        //mock!!.expectedBodiesReceived("Hello Baeldung Readers!")
        AdviceWith.adviceWith(
            context, "RetriveMail"
        ) { a: AdviceWithRouteBuilder ->
           // a.interceptFrom("google").to("mock:http://localhost:8080/API/messages")
            //a.interceptSendToEndpoint("mock:google-mail:messages/get")
            val ep = context.getEndpoint("google-mail:messages/get",GoogleMailEndpoint::class.java)

            a.interceptFrom(ep.apiName.toString()).stop()
            a.interceptFrom( ep.endpointUri ).log("intercettato").stop()
            a.interceptFrom( ep.endpointBaseUri ).log("intercettato").stop()
            a.interceptFrom("https://gmail.googleapis.com/gmail/v1/users/me/messages/353").log("intercettato").stop()
            a.interceptFrom("google-mail:messages/get").log("intercettato").stop()


        }


        mockInput.isLog=true
        mockMail.isLog=true
        mockResult.isLog=true
        val log: Logger = Logger.getLogger("x")
        log.info("I'm starting")
       // template!!.sendBody("mock:google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com2","")
        // template!!.requestBody(mock,"ciao1")
       // template.requestBody(mock,"ciao2")
        val headers = mapOf(
            "CamelGoogleMailId" to  353,
        )
        //template.setDefaultEndpointUri("mock:google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com2")
        //template.sendBody("direct:foo", "Hello World");
        template.requestBodyAndHeaders("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com","body",headers)
       // template.sendBodyAndHeaders("google-mail:messages/get","body",headers)
       // template.requestBodyAndHeaders("direct:foo","body",headers)
       // template.sendBody(mockInput,"ciao2")
        mockResult.expectedBodyReceived()
        mockMail.expectedHeaderReceived("ciao2", headers)
        //mock.expectedBodiesReceived("Hello World");
      //  mock!!.expectedBodiesReceivedInAnyOrder("ciao2")
       // mock!!.assertIsSatisfied()
     //   mockResult!!.expectedBodiesReceived("ciao2")
    //    mockResult!!.assertIsSatisfied()

       // template!!.sendBodyAndHeaders("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com2","r" , mapOf())
        mockMail.assertIsSatisfied()
        mockResult.assertIsSatisfied()
    }
}