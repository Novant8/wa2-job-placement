package it.polito.wa2.g07.crm.services.lab03

import io.mockk.every
import io.mockk.mockk
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalReducedDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class ProfessionalServiceTest {

    val professionalRepository = mockk<ProfessionalRepository>()

    val service = ProfessionalServiceImpl(professionalRepository)

    @Nested
    inner class GetProfessionalTests {

        private val professional = Professional(
            Contact(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL
            ),
            "Torino",
            mutableSetOf("Proficient in Kotlin", "Can work well in a team"),
            0.0
        )

        private var pageImpl: PageImpl<Professional>

        init {
            professional.professionalId = 1L
            professional.contactInfo.contactId = 1L
            this.pageImpl = PageImpl(listOf(professional))
        }

        @BeforeEach
        fun initMocks() {
            every { professionalRepository.findAll(any(), any(Pageable::class)) } returns PageImpl(listOf(professional))
            every { professionalRepository.findById(any(Long::class)) } returns Optional.empty()
            every { professionalRepository.findById(professional.professionalId) } returns Optional.of(professional)
        }

        @Test
        fun searchProfessionals_success() {
            val result = service.searchProfessionals(ProfessionalFilterDTO(location = "Torino"), PageRequest.of(0, 10))
            assertEquals(result, pageImpl.map { it.toProfessionalReducedDto() })
        }

        @Test
        fun getProfessionalById_found() {
            val result = service.getProfessionalById(professional.professionalId)
            assertEquals(result, professional.toProfessionalDto())
        }

        @Test
        fun getProfessionalById_notFound() {
            assertThrows<EntityNotFoundException> {
                service.getProfessionalById(professional.professionalId + 1)
            }
        }

    }

}