package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.crm.controllers.lab02.ContactController
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.ProfessionalService
import org.junit.Before
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.argThat
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
                        jsonPath("$.contactInfo.surname"){value(createProfessionalDTO.contactInfo.surname)}
                        jsonPath("$.contactInfo.category"){value(createProfessionalDTO.contactInfo.category)}
                        jsonPath("$.contactInfo.ssn"){value(createProfessionalDTO.contactInfo.ssn)}
                        jsonPath("$.contactInfo.addresses[*].email"){value(mockEmailDTO.email)}
                        jsonPath("$.contactInfo.addresses[*].phoneNumber"){value(mockTelephoneDTO.phoneNumber)}
                        jsonPath("$.contactInfo.addresses[*].street"){value(mockDwellingDTO.street)}
                        jsonPath("$.contactInfo.addresses[*].district"){value(mockDwellingDTO.district)}
                        jsonPath("$.contactInfo.addresses[*].city"){value(mockDwellingDTO.city)}
                        jsonPath("$.contactInfo.addresses[*].country"){value(mockDwellingDTO.country)}
                        jsonPath("$.location"){value(createProfessionalDTO.location)}
                        jsonPath("$.skills[0]"){value(createProfessionalDTO.skills.elementAt(1))}
                        jsonPath("$.skills[1]"){value(createProfessionalDTO.skills.elementAt(0))}
                        jsonPath("$.skills[2]"){value(createProfessionalDTO.skills.elementAt(2))}
                        jsonPath("$.dailyRate"){value(createProfessionalDTO.dailyRate)}
                        jsonPath("$.employmentState"){value(createProfessionalDTO.employmentState.toString())}
                        jsonPath("$.notes"){value(createProfessionalDTO.notes)}


                    }
                }

        }
        @Test
        fun saveProfessional_noNotes(){
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
                null

            )

            mockMvc
                .post("/API/professionals"){
                    contentType = MediaType.APPLICATION_JSON
                    content= jsonMapper().writeValueAsString(createProfessionalDTO)

                }.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.contactInfo.name") { value(createProfessionalDTO.contactInfo.name) }
                        jsonPath("$.contactInfo.surname") { value(createProfessionalDTO.contactInfo.surname) }
                        jsonPath("$.contactInfo.category") { value(createProfessionalDTO.contactInfo.category) }
                        jsonPath("$.contactInfo.ssn") { value(createProfessionalDTO.contactInfo.ssn) }
                        jsonPath("$.contactInfo.addresses[*].email") { value(mockEmailDTO.email) }
                        jsonPath("$.contactInfo.addresses[*].phoneNumber") { value(mockTelephoneDTO.phoneNumber) }
                        jsonPath("$.contactInfo.addresses[*].street") { value(mockDwellingDTO.street) }
                        jsonPath("$.contactInfo.addresses[*].district") { value(mockDwellingDTO.district) }
                        jsonPath("$.contactInfo.addresses[*].city") { value(mockDwellingDTO.city) }
                        jsonPath("$.contactInfo.addresses[*].country") { value(mockDwellingDTO.country) }
                        jsonPath("$.location") { value(createProfessionalDTO.location) }
                        jsonPath("$.skills[0]") { value(createProfessionalDTO.skills.elementAt(1)) }
                        jsonPath("$.skills[1]") { value(createProfessionalDTO.skills.elementAt(0)) }
                        jsonPath("$.skills[2]") { value(createProfessionalDTO.skills.elementAt(2)) }
                        jsonPath("$.dailyRate") { value(createProfessionalDTO.dailyRate) }
                        jsonPath("$.employmentState") { value(createProfessionalDTO.employmentState.toString()) }
                        jsonPath("$.notes") {
                            value(null)

                        }
                    }
                }
        }
    }

    @Nested
    inner class AssociateContact{
        private val registeredContactIds = HashSet<Long>()
        private val skills= setOf("PHP","Java","Angular")

        @BeforeEach
        fun initMocks(){
            every { professionalService.bindContactToProfessional(any(Long::class),any(String::class),
                skills,any(Double::class),any(EmploymentState::class),any(String::class)) } answers {
                val contactId:Long=firstArg<Long>()



                if(registeredContactIds.contains(contactId))
                {
                    throw  ContactAssociationException("Contact with id : ${mockContactDTO.id} is already associated to another Professional ")
                }else if (contactId !=mockContactDTO.id){
                    throw  EntityNotFoundException("Contact does not exists")
                }



                ProfessionalDTO(
                    1L,
                    ContactDTO(
                        mockContactDTO.id,
                        mockContactDTO.name,
                        mockContactDTO.surname,
                        mockContactDTO.category,
                        listOf(mockContactDTO.addresses).flatten(),
                        mockContactDTO.ssn
                    ),
                    "Torino",
                    skills,
                    100.0,
                    EmploymentState.UNEMPLOYED,
                    "TestNotes"

                )

            }
        }

        @Test
        fun associateValidContact(){
            val contactId = mockContactDTO.id
            val values = mapOf("location" to "Torino","skills" to skills,"dailyRate" to 100.0,"EmploymentState" to EmploymentState.UNEMPLOYED, "notes" to "TestNotes")
            mockMvc
                .post("/API/contacts/$contactId/professionals")
                {
                    contentType= MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(values)
                }.andExpect {
                    status { isCreated() }
                }
        }


    }

}