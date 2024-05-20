package it.polito.wa2.g07.communicationmanager.routes

import com.google.api.services.gmail.model.Draft
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import org.apache.camel.EndpointInject
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64


@Component
class GmailSendRoute : RouteBuilder() {


    @EndpointInject("google-mail:messages/send")
    lateinit var ep: GoogleMailEndpoint

    override fun configure() {

     /*   from("seda:sendEmail")
            .log(LoggingLevel.INFO,"SENDING EMAIL NOTIFICATION")
            .to("smtps://{{spring.mail.host}}:{{spring.mail.port}}?username={{spring.mail.username}}&password={{spring.mail.password}}&mail.smtp.auth=auth&mail.smtp.starttls.enable=starttls")
            .log(LoggingLevel.INFO,"EMAIL NOTIFICATION SENT")
            .stop();*/

        from("seda:sendEmail")
            .process{
                val from = it.getIn().getHeader("From ")
                val to = it.getIn().getHeader("To")
                val subject = it.getIn().getHeader("Subject")
                val body = it.getIn().body as String

                val message = Message()
                message.payload = MessagePart()
                message.payload.body = MessagePartBody()
                message.payload.body.data = Base64.getEncoder().encodeToString("Stuff".toByteArray())


            }



      //  ep.client.users().messages().send(email, message).execute()

        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com")
            .log(LoggingLevel.INFO, "Read from the input file")
            .setHeader("mediaContent",simple("text/plain"))
            .setHeader("content",simple("ciao"))
            .setHeader("userId",simple("me"))
            .setHeader("to", simple("webapp2.2024.g07@gmail.com"))
            .to("google-mail:MESSAGES/send?userId=webapp2.2024.g07@gmail.com&mediaContent=text/plain")

    }

}
