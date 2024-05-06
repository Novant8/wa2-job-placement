package it.polito.wa2.g07.crm.controllers.lab03

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalReducedDTO
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalReducedDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@WebMvcTest(ProfessionalController::class)
class ProfessionalControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var professionalService: ProfessionalService

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

        private var pageImpl: PageImpl<ProfessionalReducedDTO>

        init {
            professional.professionalId = 1L
            professional.contactInfo.contactId = 1L
            this.pageImpl = PageImpl(listOf(professional.toProfessionalReducedDto()))
        }

        @BeforeEach
        fun initMocks() {
            every { professionalService.searchProfessionals(any(ProfessionalFilterDTO::class), any(Pageable::class)) } returns pageImpl
            every { professionalService.getProfessionalById(any(Long::class)) } throws EntityNotFoundException("Professional not found")
            every { professionalService.getProfessionalById(professional.professionalId) } returns professional.toProfessionalDto()
        }

        @Test
        fun searchProfessionals_success() {
            mockMvc
                .get("/API/professionals")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                        jsonPath("$.content[0].contactInfo.id") { value(professional.contactInfo.contactId) }
                        jsonPath("$.content[0].contactInfo.name") { value(professional.contactInfo.name) }
                        jsonPath("$.content[0].contactInfo.surname") { value(professional.contactInfo.surname) }
                        jsonPath("$.content[0].contactInfo.category") { value(professional.contactInfo.category.toString()) }
                        jsonPath("$.content[0].location") { value(professional.location) }
                        jsonPath("$.content[0].skills") { value(containsInAnyOrder(*professional.skills.toTypedArray())) }
                        jsonPath("$.content[0].dailyRate") { doesNotExist() }
                        jsonPath("$.content[0].notes") { doesNotExist() }
                    }
                }
        }

        @Test
        fun getProfessionalById_found() {
            mockMvc
                .get("/API/professionals/${professional.professionalId}")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.id") { value(professional.professionalId) }
                        jsonPath("$.contactInfo.id") { value(professional.contactInfo.contactId) }
                        jsonPath("$.contactInfo.name") { value(professional.contactInfo.name) }
                        jsonPath("$.contactInfo.surname") { value(professional.contactInfo.surname) }
                        jsonPath("$.contactInfo.category") { value(professional.contactInfo.category.toString()) }
                        jsonPath("$.location") { value(professional.location) }
                        jsonPath("$.skills") { value(containsInAnyOrder(*professional.skills.toTypedArray())) }
                        jsonPath("$.dailyRate") { value(professional.dailyRate) }
                        jsonPath("$.notes") { value(professional.notes) }
                    }
                }
        }

        @Test
        fun getProfessionalById_notFound() {
            mockMvc
                .get("/API/professionals/${professional.professionalId+1}")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

}