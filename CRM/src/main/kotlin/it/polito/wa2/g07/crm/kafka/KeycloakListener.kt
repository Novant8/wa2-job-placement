package it.polito.wa2.g07.crm.kafka

import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.EmailDTO
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.services.lab02.ContactService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KeycloakListener(private val contactService: ContactService) {

    @KafkaListener(id = "CRMListener", topics = ["IAM-REGISTER"], containerFactory = "registerContainerFactory")
    fun listen(registerEventValue: RegisterEventValue) {
        val createContactDTO = CreateContactDTO(
            name = registerEventValue.firstName,
            surname = registerEventValue.lastName,
            category = ContactCategory.UNKNOWN.toString(),
            ssn = null,
            addresses = listOf(EmailDTO(registerEventValue.email)),
            userId = registerEventValue.userId
        )
        contactService.create(createContactDTO)
    }

}