package it.polito.wa2.g07.crm.integrations.lab03

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc
class ProfessionalIntegrationTest: CrmApplicationTests() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var professionalRepository: ProfessionalRepository
    @Autowired
    lateinit var jobOfferRepository: ProfessionalRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class GetProfessionalTest {

        private var professional = Professional(
            Contact(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL
            ),
            "Torino",
            mutableSetOf("Proficient in Kotlin", "Can work well in a team"),
            0.0
        )

        @BeforeEach
        fun init() {
            jobOfferRepository.deleteAll()
            professionalRepository.deleteAll()

            professional = professionalRepository.save(professional)
        }

        @Test
        fun getProfessionals_noFilter() {
            mockMvc
                .get("/API/professionals").andExpect {
                    status { isOk() }
                    jsonPath("$.content[0].id") { value(professional.professionalId) }
                }
        }

        @Test
        fun getProfessionals_skillsFilter() {
            val validFilters = listOf(
                "kotlin",       // Only skill matches
                "kotlin,team",  // All skills match
                "java,team"     // One skill matches
            )

            for (filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("skills", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content") { isArray(); isNotEmpty() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No skill matches
            mockMvc
                .get("/API/professionals") {
                    queryParam("skills", "other")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content") { isArray(); isEmpty() }
                }
        }

        @Test
        fun getProfessionals_locationFilter() {
            val validFilters = listOf("Torino", "torino", "TORINO")

            // Match
            for(filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("location", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No match
            mockMvc
                .get("/API/professionals") {
                    queryParam("location", "Milano")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content") { isArray(); isEmpty() }
                }
        }

        @Test
        fun getProfessionals_employmentStateFilter() {
            val validFilters = listOf("unemployed", "Unemployed", "UNEMPLOYED")

            // Match
            for(filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("employmentState", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No match
            val invalidFilters = listOf("employed", "not_available")
            for (filter in invalidFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("employmentState", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content") { isArray(); isEmpty() }
                    }
            }

            // Invalid filter
            mockMvc
                .get("/API/professionals") {
                    queryParam("employmentState", "i am not valid")
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun getProfessionals_multipleFilters() {
            mockMvc
                .get("/API/professionals") {
                    queryParam("skills", "kotlin")
                    queryParam("location", "Torino")
                    queryParam("employmentState", "unemployed")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content[0].id") { value(professional.professionalId) }
                }
        }

        @Test
        fun getProfessionalById_exists() {
            mockMvc
                .get("/API/professionals/${professional.professionalId}")
                .andExpect {
                    status { isOk() }
                    content {
                        json(objectMapper.writeValueAsString(professional.toProfessionalDto()))
                    }
                }
        }

        @Test
        fun getProfessionalById_doesNotExist() {
            mockMvc
                .get("/API/professionals/${professional.professionalId+1}")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

}