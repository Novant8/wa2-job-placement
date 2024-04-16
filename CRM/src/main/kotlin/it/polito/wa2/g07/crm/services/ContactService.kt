package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.ReducedContactDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface ContactService {
    fun getContacts(filterBy: ContactFilterBy, query: String, pageable: Pageable): Page<ReducedContactDTO>
}