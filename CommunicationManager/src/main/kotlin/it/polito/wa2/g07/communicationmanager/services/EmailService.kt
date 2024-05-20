package it.polito.wa2.g07.communicationmanager.services

import it.polito.wa2.g07.communicationmanager.SendEmailDTO

interface EmailService {
    fun sendEmail(sendEmailDTO: SendEmailDTO)
}