package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.crm.controllers.lab02.ContactController
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@WebMvcTest(ProfessionalController::class)
class ProfessionalControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var professionalService: ProfessionalService

    @MockkBean
    private lateinit var contactService: ContactService


    private val mockEmailDTO = EmailDTO("company.test@example.org")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")

    private val mockResponseEmailDTO = EmailResponseDTO(1L,"company.test@example.org")
    private val mockResponseTelephoneDTO = TelephoneResponseDTO(2L,"34242424242")
    private val mockResponseDwellingDTO = DwellingResponseDTO(3L,"Via Roma, 18", "Torino", "TO", "IT")

    private val mockContactDTO = ContactDTO(
        1L,
        "Mario",
        "Rossi",
        ContactCategory.PROFESSIONAL,
        listOf(mockResponseEmailDTO, mockResponseTelephoneDTO, mockResponseDwellingDTO),
        "RSSMRA70A01L219K"
    )

    @Nested
    inner class PostProfessional{
        @BeforeEach
        fun initMocks(){
            every { professionalService.createProfessional(any(CreateProfessionalDTO::class)) } answers {
                val professionalDTO = firstArg<CreateProfessionalDTO>()
                if (professionalDTO.contactInfo.category != "PROFESSIONAL"){
                    throw InvalidParamsException("You must register a Professional user")
                }else{
                    professionalDTO.toEntity().toProfessionalDto()
                }
            }
        }

        @Test
        fun saveProfessional(){
            val createProfessionalDTO = CreateProfessionalDTO(
                CreateContactDTO(
                    "Professional1Name",
                    "Professional1Surname",
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO,mockTelephoneDTO,mockDwellingDTO)
                ),
                "Torino",
                setOf("PHP","Java","Angular"),
                100.0,
                EmploymentState.UNEMPLOYED,
                "notes test"

            )

            mockMvc
                .post("/API/professionals"){
                    contentType = MediaType.APPLICATION_JSON
                    content= jsonMapper().writeValueAsString(createProfessionalDTO)

                }.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.contactInfo.name"){value(createProfessionalDTO.contactInfo.name)}

                    }
                }

        }
    }

}