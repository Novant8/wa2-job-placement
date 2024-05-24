package it.polito.wa2.g07.communicationmanager.services

import it.polito.wa2.g07.communicationmanager.dtos.SendEmailDTO

interface EmailService {
    fun sendEmail(sendEmailDTO: SendEmailDTO)
}