package it.polito.wa2.g07.communicationmanager.routes

import com.google.api.client.util.StringUtils
import it.polito.wa2.g07.communicationmanager.dtos.EmailDTO
import it.polito.wa2.g07.communicationmanager.dtos.MessageCreateDTO
import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel


import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.apache.camel.component.http.HttpMethods

import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.logging.Logger

@Component
class GmailRoute: RouteBuilder() {

    @EndpointInject("google-mail:messages/get")
    lateinit var ep: GoogleMailEndpoint

    @Value("\${comm-manager.crm-url}")
    private lateinit var crmUrl: String

    override fun configure() {

        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com")
            .routeId("RetriveMail")
            .process {
                ep.runLoggingLevel=LoggingLevel.DEBUG
                val log: Logger = Logger.getLogger("u")
                log.info("it.getIn()->" + it.getIn())
                val id = it.getIn().getHeader("CamelGoogleMailId").toString()
                log.info(id)
                val message = ep.client.users().messages().get("me", id).execute()
                log.info(ep.client.users().messages().get("me", id).userId)
                log.info(ep.client.users().messages().get("me", id).uriTemplate)


               val subject = message.payload.headers.
                find{it.name.equals("subject",true)}?.get("value")?.toString() ?: ""
                val from = message.payload.headers.
                find{it.name.equals("from",true)}?.get("value")?.toString() ?: ""
                val senderMail = if(from.contains("<")) {
                    from.split("<")[1].dropLast(1)
                } else {
                    from
                }
                // val body = StringUtils.newStringUtf8(Base64.decodeBase64(message.payload.parts[0].body.data))
                val bodyB64 = message.payload.body?.data ?: message.payload.parts[0]?.body?.data
                val body = StringUtils.newStringUtf8(Base64.getDecoder().decode(bodyB64))

                val messageCreateDTO = MessageCreateDTO(
                    EmailDTO(senderMail),
                    subject,
                    body
                )

                it.getIn().body = messageCreateDTO
                log.info(messageCreateDTO.toString())
            }
            .log("Received e-mail from \${body.sender.email}")
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant(ContentType.APPLICATION_JSON))
            .to(crmUrl)
    }
}
