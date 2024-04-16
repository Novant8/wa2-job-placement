package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.Category
import it.polito.wa2.g07.crm.repositories.ContactRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ContactServiceImpl(private val contactRepository: ContactRepository) : ContactService{

    override fun getContacts(
        filterBy: ContactFilterBy,
        query: String,
        pageable: Pageable
    ): Page<ReducedContactDTO> {
        val result = when (filterBy) {
            ContactFilterBy.NONE ->         contactRepository.findAll(pageable)
            ContactFilterBy.FULL_NAME ->    contactRepository.findAllByFullNameLike(query, pageable)
            ContactFilterBy.SSN ->          contactRepository.findAllBySSN(query, pageable)
            ContactFilterBy.EMAIL ->        contactRepository.findAllByEmail(query, pageable)
            ContactFilterBy.TELEPHONE ->    contactRepository.findAllByTelephone(query, pageable)
            ContactFilterBy.ADDRESS ->      contactRepository.findAllByDwellingLike(query, pageable)
            ContactFilterBy.CATEGORY ->     contactRepository.findAllByCategory(Category.valueOf(query), pageable)
        }
        return result.map { it.toReducedContactDTO() }
    }

}