package it.polito.wa2.g07.communicationmanager.routes

import org.apache.camel.EndpointInject
import org.springframework.stereotype.Component
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint

@Component
class GmailRoute(): RouteBuilder() {
    @EndpointInject("google-mail:messages/get")
    lateinit var ep: GoogleMailEndpoint

    override fun configure() {
        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com")
            .process {
                val id = it.getIn().getHeader("CamelGoogleMailId").toString()
                val message = ep.client.users().messages().get("me", id).execute()
                val subject = message.payload.headers.
                find{it.name.equals("subject",true)}?.get("value")?.toString() ?: ""
                val from = message.payload.headers.
                find{it.name.equals("from",true)}?.get("value")?.toString() ?: ""

                //it.getIn().setBody(email(from, subject, message.snippet))
                it.getIn().setBody("body")
            }
            .log("Funziono")
           // .to("bean:emailRepository?method=save")
            //.to("http://localhost:8080/API/customers")
            .to("file:tmp/out")
    }
}
