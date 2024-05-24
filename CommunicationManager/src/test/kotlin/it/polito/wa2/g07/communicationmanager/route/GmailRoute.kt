package it.polito.wa2.g07.communicationmanager.route
import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint

import org.apache.camel.test.spring.junit5.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
@CamelSpringBootTest
@MockEndpoints("file:output")
internal class GreetingsFileRouterUnitTest {
    @Autowired
    private val template: ProducerTemplate? = null

    @EndpointInject("mock:file:output")
    private val mock: MockEndpoint? = null

    @Test
    @Throws(InterruptedException::class)
    fun whenSendBody_thenGreetingReceivedSuccessfully() {
        mock!!.expectedBodiesReceived("Hello Baeldung Readers!")
        template!!.sendBody("direct:start", null)
        mock.assertIsSatisfied()
    }
}