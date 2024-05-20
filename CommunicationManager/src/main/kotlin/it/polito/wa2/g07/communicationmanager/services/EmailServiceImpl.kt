package it.polito.wa2.g07.communicationmanager.services

import it.polito.wa2.g07.communicationmanager.SendEmailDTO
import org.apache.camel.ProducerTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class EmailServiceImpl : EmailService {

    @Autowired
    private lateinit var producerTemplate: ProducerTemplate
    
    @Value("\${gmail.username}")
    private lateinit var from: String

    override fun sendEmail(sendEmailDTO: SendEmailDTO) {
        val headers = mapOf(
            "From" to from,
            "To" to sendEmailDTO.to,
            "Subject" to sendEmailDTO.subject
        )
        producerTemplate.sendBodyAndHeaders("seda:sendEmail", sendEmailDTO.body, headers)
    }
}