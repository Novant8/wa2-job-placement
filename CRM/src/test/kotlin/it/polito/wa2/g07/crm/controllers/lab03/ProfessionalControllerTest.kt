package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.module.kotlin.jsonMapper


import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.CreateProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalDTO
import it.polito.wa2.g07.crm.dtos.lab03.toEntity


import it.polito.wa2.g07.crm.entities.lab03.EmploymentState

import it.polito.wa2.g07.crm.exceptions.ContactAssociationException

import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab02.ContactService


import org.springframework.http.MediaType

import org.springframework.test.web.servlet.post
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.ProfessionalReducedDTO
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalReducedDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
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
import org.springframework.test.web.servlet.put
import kotlin.reflect.KClass


@WebMvcTest(ProfessionalController::class)
class ProfessionalControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var professionalService: ProfessionalService

    @MockkBean
    private lateinit var contactService: ContactService


    private val mockEmailDTO = EmailDTO("company.test@example.org")
    private val mockTelephoneDTO = TelephoneDTO("34242424242")
    private val mockDwellingDTO = DwellingDTO("Via Roma, 18", "Torino", "TO", "IT")

    private val mockResponseEmailDTO = EmailResponseDTO(1L, "company.test@example.org")
    private val mockResponseTelephoneDTO = TelephoneResponseDTO(2L, "34242424242")
    private val mockResponseDwellingDTO = DwellingResponseDTO(3L, "Via Roma, 18", "Torino", "TO", "IT")

    private val mockContactDTO = ContactDTO(
        1L,
        "Mario",
        "Rossi",
        ContactCategory.PROFESSIONAL,
        listOf(mockResponseEmailDTO, mockResponseTelephoneDTO, mockResponseDwellingDTO),
        "RSSMRA70A01L219K"
    )

    private val mockProfessionalDTO = ProfessionalDTO(
        1L,
        mockContactDTO,
        "Torino",
        setOf("Public Speaking", "Team working", "C Programming"),
        100.0,
        EmploymentState.UNEMPLOYED,
        "TestNotes23"
    )

    private val invalidEmailDTOs = listOf(
        EmailDTO(""),
        EmailDTO(" "),
        EmailDTO("invalid")
    )

    private val invalidTelephoneDTOs = listOf(
        TelephoneDTO(""),
        TelephoneDTO(" "),
        TelephoneDTO("invalid")
    )

    private val invalidDwellingDTOs = listOf(
        DwellingDTO("", "Torino", "TO", "IT"),
        DwellingDTO(" ", "Torino", "TO", "IT"),
        DwellingDTO("Via Garibaldi, 42", "", "TO", "IT"),
        DwellingDTO("Via Garibaldi, 42", " ", "TO", "IT"),
        DwellingDTO("Via Garibaldi, 42", "Torino", "", "IT"),
        DwellingDTO("Via Garibaldi, 42", "Torino", " ", "IT"),
        DwellingDTO("Via Garibaldi, 42", "Torino", "TO", ""),
        DwellingDTO("Via Garibaldi, 42", "Torino", "TO", " ")
    )

    @Nested
    inner class PostProfessional {
        @BeforeEach
        fun initMocks() {
            every { professionalService.createProfessional(any(CreateProfessionalDTO::class)) } answers {
                val professionalDTO = firstArg<CreateProfessionalDTO>()
                if (professionalDTO.contactInfo.category != "PROFESSIONAL") {
                    throw InvalidParamsException("You must register a Professional user")
                } else {
                    professionalDTO.toEntity().toProfessionalDto()
                }
            }
        }

        @Test
        fun saveProfessional() {
            val createProfessionalDTO = CreateProfessionalDTO(
                CreateContactDTO(
                    "Professional1Name",
                    "Professional1Surname",
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO, mockTelephoneDTO, mockDwellingDTO)
                ),
                "Torino",
                setOf("PHP", "Java", "Angular"),
                100.0,
                EmploymentState.UNEMPLOYED,
                "notes test"

            )

            mockMvc
                .post("/API/professionals") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createProfessionalDTO)

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
                        jsonPath("$.notes") { value(createProfessionalDTO.notes) }


                    }
                }

        }

        @Test
        fun saveProfessional_noNotes() {
            val createProfessionalDTO = CreateProfessionalDTO(
                CreateContactDTO(
                    "Professional1Name",
                    "Professional1Surname",
                    ContactCategory.PROFESSIONAL.name,
                    "VRDLGU70A01L219G",
                    listOf(mockEmailDTO, mockTelephoneDTO, mockDwellingDTO)
                ),
                "Torino",
                setOf("PHP", "Java", "Angular"),
                100.0,
                EmploymentState.UNEMPLOYED,
                null

            )

            mockMvc
                .post("/API/professionals") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(createProfessionalDTO)

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
    inner class AssociateContact {
        private val usedContactIds = HashSet<Long>()
        private val skills = setOf("PHP", "Java", "Angular")

        @BeforeEach
        fun initMocks() {
            every {
                professionalService.bindContactToProfessional(
                    any(Long::class), any(String::class),
                    skills, any(Double::class), any(EmploymentState::class), any(String::class)
                )
            } answers {
                val contactId: Long = firstArg<Long>()

                if (usedContactIds.contains(contactId)) {
                    throw ContactAssociationException("Contact with id : ${mockContactDTO.id} is already associated to another Professional ")
                } else if (contactId != mockContactDTO.id) {
                    throw EntityNotFoundException("Contact does not exists")
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
        fun associateValidContact() {
            val contactId = mockContactDTO.id
            val values = mapOf(
                "location" to "Torino",
                "skills" to skills,
                "dailyRate" to 100.0,
                "EmploymentState" to EmploymentState.UNEMPLOYED,
                "notes" to "TestNotes"
            )
            mockMvc
                .post("/API/contacts/$contactId/professionals")
                {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(values)
                }.andExpect {
                    status { isCreated() }
                }
        }

        @Test
        fun associateAlreadyConnectedContact(){
            val contactId = mockContactDTO.id
            val values = mapOf(
                "location" to "Torino",
                "skills" to skills,
                "dailyRate" to 100.0,
                "EmploymentState" to EmploymentState.UNEMPLOYED,
                "notes" to "TestNotes"
            )
            val values2 = mapOf(
                "location" to "Milano",
                "skills" to skills,
                "dailyRate" to 200.0,
                "EmploymentState" to EmploymentState.UNEMPLOYED,
                "notes" to "TestNotes2"
            )
            mockMvc.post("/API/contacts/$contactId/professionals"){
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(values)
            }.andExpect {
                status { isCreated() }
            }

            usedContactIds.add(contactId)

            mockMvc.post("/API/contacts/$contactId/professionals"){
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(values2)
            }.andExpect {
                status { isConflict() }
            }


        }
    }

    @Nested
    inner class PutProfessional() {
        private val contactId = mockContactDTO.id

        @BeforeEach
        fun initMocks() {
            every {
                professionalService.postProfessionalNotes(
                    any(Long::class),
                    any(String::class)
                )
            } throws EntityNotFoundException("Professional not found")
            every {
                professionalService.postProfessionalLocation(
                    any(Long::class),
                    any(String::class)
                )
            } throws EntityNotFoundException("Professional not found")
            every {
                professionalService.postProfessionalSkills(
                    any(Long::class),
                    any()
                )
            } throws EntityNotFoundException("Professional not found")
            every {
                professionalService.postProfessionalDailyRate(
                    any(Long::class),
                    any(Double::class)
                )
            } throws EntityNotFoundException("Professional not found")
            every {
                professionalService.postProfessionalEmploymentState(
                    any(Long::class),
                    any(EmploymentState::class)
                )
            } throws EntityNotFoundException("Professional not found")

            every { professionalService.postProfessionalNotes(mockProfessionalDTO.id, any(String::class)) } answers {
                val notes = secondArg<String>()
                ProfessionalDTO(
                    mockProfessionalDTO.id,
                    mockProfessionalDTO.contactInfo,
                    mockProfessionalDTO.location,
                    mockProfessionalDTO.skills,
                    mockProfessionalDTO.dailyRate,
                    mockProfessionalDTO.employmentState,
                    notes

                )

            }
            every { professionalService.postProfessionalLocation(mockProfessionalDTO.id, any(String::class)) } answers {
                val location = secondArg<String>()
                ProfessionalDTO(
                    mockProfessionalDTO.id,
                    mockProfessionalDTO.contactInfo,
                    location,
                    mockProfessionalDTO.skills,
                    mockProfessionalDTO.dailyRate,
                    mockProfessionalDTO.employmentState,
                    mockProfessionalDTO.notes

                )

            }
            every { professionalService.postProfessionalDailyRate(mockProfessionalDTO.id, any(Double::class)) } answers {
                val dailyRate = secondArg<Double>()
                ProfessionalDTO(
                    mockProfessionalDTO.id,
                    mockProfessionalDTO.contactInfo,
                    mockProfessionalDTO.location,
                    mockProfessionalDTO.skills,
                    dailyRate,
                    mockProfessionalDTO.employmentState,
                    mockProfessionalDTO.notes

                )

            }
            every { professionalService.postProfessionalEmploymentState(mockProfessionalDTO.id, any(EmploymentState::class)) } answers {
                val employmentState = secondArg<EmploymentState>()
                ProfessionalDTO(
                    mockProfessionalDTO.id,
                    mockProfessionalDTO.contactInfo,
                    mockProfessionalDTO.location,
                    mockProfessionalDTO.skills,
                    mockProfessionalDTO.dailyRate,
                    employmentState,
                    mockProfessionalDTO.notes

                )

            }
            every { professionalService.postProfessionalSkills(mockProfessionalDTO.id, any()) } answers {
                val skills = secondArg<Set<String>>()
                ProfessionalDTO(
                    mockProfessionalDTO.id,
                    mockProfessionalDTO.contactInfo,
                    mockProfessionalDTO.location,
                    skills,
                    mockProfessionalDTO.dailyRate,
                    mockProfessionalDTO.employmentState,
                    mockProfessionalDTO.notes

                )

            }
            every { contactService.updateAddress(any(Long::class), any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Contact not found")
            every { contactService.updateAddress(mockContactDTO.id, any(Long::class), any(AddressDTO::class)) } throws EntityNotFoundException("Address not found")
            for (addressResponseDTO in listOf(mockResponseEmailDTO, mockResponseTelephoneDTO, mockResponseDwellingDTO)) {
                every { contactService.updateAddress(mockContactDTO.id, addressResponseDTO.id, any(AddressDTO::class)) } answers {
                    val addressDTO = thirdArg<AddressDTO>()
                    if (addressDTO.addressType != addressResponseDTO.toAddressDTO().addressType)
                        throw InvalidParamsException("Mismatching address types")
                    ContactDTO(
                        mockContactDTO.id,
                        mockContactDTO.name,
                        mockContactDTO.surname,
                        mockContactDTO.category,
                        mockContactDTO.addresses.map { if (it.id == addressResponseDTO.id) addressDTO.toEntity().toAddressResponseDTO() else it },
                        mockContactDTO.ssn
                    )
                }
                every { contactService.updateAddress(mockContactDTO.id, addressResponseDTO.id, addressResponseDTO.toAddressDTO()) } throws DuplicateAddressException("Address already associated")
            }
            every { professionalService.getProfessionalById(any(Long::class)) } throws EntityNotFoundException("Professional not found")
            every { professionalService.getProfessionalById(mockProfessionalDTO.id) } answers { mockProfessionalDTO }

        }

        @Test
        fun updateNotes() {
            val updateNotes = mapOf("notes" to "TheNewNotes")
            mockMvc.put("/API/professionals/$contactId/notes") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateNotes)

            }.andExpect {
                status { isOk() }
                content {

                    jsonPath("$.notes") { value(updateNotes["notes"]) }
                }

            }
        }

        @Test
        fun updateNotes_UnknownProfessional() {
            val updateNotes = mapOf("notes" to "TheNewNotes")
            mockMvc.put("/API/professionals/800/notes") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateNotes)

            }.andExpect {
                status { isNotFound() }


            }
        }


        @Test
        fun updateLocation() {
            val updateLocation = mapOf("location" to "New Location")
            mockMvc.put("/API/professionals/$contactId/location") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateLocation)

            }.andExpect {
                status { isOk() }
                content {

                    jsonPath("$.location") { value(updateLocation["location"]) }
                }

            }
        }

        @Test
        fun updateLocation_UnknownProfessional() {
            val updateLocation = mapOf("location" to "New Location")
            mockMvc.put("/API/professionals/800/location") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateLocation)

            }.andExpect {
                status { isNotFound() }


            }
        }

        @Test
        fun updateDailyRate() {
            val updateDailyRate = mapOf("dailyRate" to 600.0)
            mockMvc.put("/API/professionals/$contactId/dailyRate") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateDailyRate)

            }.andExpect {
                status { isOk() }
                content {

                    jsonPath("$.dailyRate") { value(updateDailyRate["dailyRate"]) }
                }

            }
        }

        @Test
        fun updateDailyRate_UnknownProfessional() {
            val updateDailyRate = mapOf("dailyRate" to 600.0)
            mockMvc.put("/API/professionals/800/dailyRate") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateDailyRate)

            }.andExpect {
                status { isNotFound() }

            }
        }

        @Test
        fun updateSkills() {
            val updateSkills = mapOf("skills" to setOf("skillUpdate1","skillUpdate2"))
            mockMvc.put("/API/professionals/$contactId/skills") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateSkills)

            }.andExpect {
                status { isOk() }
                content {

                    jsonPath("$.skills[0]") { value(updateSkills["skills"]?.elementAt(0)) }
                    jsonPath("$.skills[1]") { value(updateSkills["skills"]?.elementAt(1)) }
                }

            }
        }

        @Test
        fun updateSkills_UnknownProfessional() {
            val updateSkills = mapOf("skills" to setOf("skillUpdate1","skillUpdate2"))
            mockMvc.put("/API/professionals/800/skills") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateSkills)

            }.andExpect {
                status { isNotFound() }


            }
        }


        @Test
        fun updateEmploymentState_UnknownProfessional() {
            val updateEmploymentState = mapOf("employmentState" to EmploymentState.EMPLOYED)
            mockMvc.put("/API/professionals/800/employmentState") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateEmploymentState)

            }.andExpect {
                status { isNotFound() }

            }
        }

        @Test
        fun updateEmploymentState() {
            val updateEmploymentState = mapOf("employmentState" to EmploymentState.EMPLOYED)
            mockMvc.put("/API/professionals/$contactId/employmentState") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper().writeValueAsString(updateEmploymentState)

            }.andExpect {
                status { isOk() }
                content {

                    jsonPath("$.employmentState") { value(updateEmploymentState["employmentState"].toString()) }
                }

            }
        }

        @Test
        fun updateMail_success() {
            val professionalId = mockProfessionalDTO.id
            val addressId = mockResponseEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/professionals/$professionalId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.contactInfo.addresses[*].email") { value(updatedEmailDTO.email) }
                        jsonPath("$.contactInfo.addresses[?(@.email == \"${mockEmailDTO.email}\")]") { doesNotExist() }
                    }
                }
        }

        @Test
        fun updateMail_invalidMail() {
            for (emailDTO in invalidEmailDTOs) {
                val professionalId = mockProfessionalDTO.id
                val addressId = mockResponseEmailDTO.id
                mockMvc
                    .put("/API/professionals/$professionalId/email/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(emailDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun updateMail_contactNotFound() {
            val professionalId = mockProfessionalDTO.id+20
            val addressId = mockResponseEmailDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/professionals/$professionalId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }

                }
        }

        @Test
        fun updateMail_addressNotFound() {
            val professionalId = mockProfessionalDTO.id+20
            val addressId = 70L
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/professionals/$professionalId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isNotFound() }

                }
        }

        @Test
        fun updateMail_invalidAddressType() {
            val professionalId = mockProfessionalDTO.id
            val addressId = mockResponseTelephoneDTO.id
            val updatedEmailDTO = EmailDTO("updated.mail@example.org")
            mockMvc
                .put("/API/professionals/$professionalId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedEmailDTO)
                }.andExpect {
                    status { isBadRequest() }

                }
        }

        @Test
        fun updateMail_duplicateMail() {
            val professionalId = mockProfessionalDTO.id
            val addressId = mockResponseEmailDTO.id

            mockMvc
                .put("/API/professionals/$professionalId/email/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(mockEmailDTO)
                }.andExpect {
                    status { isConflict() }

                }
        }

        @Test
        fun updateTelephone_success() {
            val professionalId = mockProfessionalDTO.id
            val addressId = mockResponseTelephoneDTO.id
            val updatedTelephoneDTO = TelephoneDTO("34298989898")
            mockMvc
                .put("/API/professionals/$professionalId/telephone/$addressId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = jsonMapper().writeValueAsString(updatedTelephoneDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.contactInfo.addresses[*].phoneNumber") { value(updatedTelephoneDTO.phoneNumber) }
                        jsonPath("$.contactInfo.addresses[?(@.phoneNumber == \"${mockTelephoneDTO.phoneNumber}\")]") { doesNotExist() }
                    }
                }
        }

        @Test
        fun updateTelephone_invalidTelephone() {
            for (telephoneDTO in invalidTelephoneDTOs) {
                val professionalId = mockProfessionalDTO.id
                val addressId = mockResponseTelephoneDTO.id
                mockMvc
                    .put("/API/professionals/$professionalId/telephone/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(telephoneDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }

        @Test
        fun updateDwelling_success() {
            val professionalId = mockProfessionalDTO.id
            val addressId = mockResponseDwellingDTO.id
            val validDwellingDTOs = listOf(
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", "IT"),
                DwellingDTO("Via Garibaldi, 42", "Torino", "TO", null),
                DwellingDTO("Via Garibaldi, 42", "Torino", null, null)
            )
            for (dwellingDTO in validDwellingDTOs) {
                mockMvc
                    .put("/API/professionals/$professionalId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isOk() }
                        content {
                            jsonPath("$.contactInfo.addresses[*].street") { value(dwellingDTO.street) }
                            jsonPath("$.contactInfo.addresses[?(@.street == \"${mockDwellingDTO.street}\")]") { doesNotExist() }
                        }
                    }
            }
        }

        @Test
        fun updateDwelling_invalidDwelling() {
            for (dwellingDTO in invalidDwellingDTOs) {
                val professionalId = mockProfessionalDTO.id
                val addressId = mockResponseDwellingDTO.id
                mockMvc
                    .put("/API/professionals/$professionalId/address/$addressId") {
                        contentType = MediaType.APPLICATION_JSON
                        content = jsonMapper().writeValueAsString(dwellingDTO)
                    }.andExpect {
                        status { isUnprocessableEntity() }
                    }
            }
        }



        }





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
                every {
                    professionalService.searchProfessionals(
                        any(ProfessionalFilterDTO::class),
                        any(Pageable::class)
                    )
                } returns pageImpl
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
                    .get("/API/professionals/${professional.professionalId + 1}")
                    .andExpect {
                        status { isNotFound() }
                    }
            }

        }



}