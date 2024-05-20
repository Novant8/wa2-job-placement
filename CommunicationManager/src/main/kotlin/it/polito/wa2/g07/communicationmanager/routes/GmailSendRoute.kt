package it.polito.wa2.g07.communicationmanager.routes

import com.google.api.services.gmail.model.*
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component
class GmailSendRoute : RouteBuilder() {

    override fun configure() {
        from("seda:sendEmail")
            .process{
                val from = it.getIn().getHeader("From") as String
                val to = it.getIn().getHeader("To") as String
                val subject = it.getIn().getHeader("Subject") as String
                val body = it.getIn().body as String

                val emailRaw = """
                    To: $to
                    From: $from
                    Subject: $subject
                    Content-Type: text/plain; charset=UTF-8
                    
                    $body
                """.trimIndent()
                val emailRawEncoded = Base64.getEncoder().encodeToString(emailRaw.toByteArray(Charsets.UTF_8))

                val message = Message().setRaw(emailRawEncoded)
                it.getIn().setHeader("CamelGoogleMail.content", message)
            }
            .setBody(simple(null))
            .removeHeaders("From|To|Subject")
            .to("google-mail:messages/send?userId=me")
    }

}
