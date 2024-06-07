package it.polito.wa2.g07.crm.integrations.lab03

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferUpdateDTO
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc(addFilters = false)
class JobOfferIntegrationTest: CrmApplicationTests() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var jobOfferRepository: JobOfferRepository

    @Autowired
    lateinit var professionalRepository: ProfessionalRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class RetriveJobOffer {
        private var customerID_1 = 0L
        private var jobOfferID_1 = 0L
        private var jobOfferID_2 = 0L
        private var professionalID_1 = 0L


        @BeforeEach()
        fun init() {
            jobOfferRepository.deleteAll()
            customerRepository.deleteAll()
            professionalRepository.deleteAll()
            contactRepository.deleteAll()


            val contact = Contact("Company", "Test", ContactCategory.CUSTOMER)
            val contactP = Contact("Professional", "Worker", ContactCategory.PROFESSIONAL)

            val customer = Customer(contact, "Affidabile")
            val professional = Professional(contactP, "Torino", setOf("ITA"), 20.0, EmploymentState.UNEMPLOYED)

            val jobOffer =
                JobOffer( mutableSetOf("ITA", "ENG"), 45, "this is a job", OfferStatus.CREATED, "notes")
            val jobOffer2 = JobOffer(
                mutableSetOf("ITA", "ENG"),
                45,
                "this is a job",
                OfferStatus.CANDIDATE_PROPOSAL,
                "notes"
            )
            jobOffer2.professional = professional

            professionalID_1 = professionalRepository.save(professional).professionalId
            customer.addPlacement(jobOffer)
            customer.addPlacement(jobOffer2)

            customerID_1 = customerRepository.save(customer).customerId

            jobOfferID_1 = jobOfferRepository.save(jobOffer).offerId
            jobOfferID_2 = jobOfferRepository.save(jobOffer2).offerId




        }

        @Test
        fun getJobOffers() {
            mockMvc.get("/API/joboffers/") {

            }.andExpect {
                status { isOk() }
                content { MockMvcResultMatchers.jsonPath("$.content[0].id").isNotEmpty }
                content { jsonPath("$.content[0].description") { value("this is a job") } }
                content { jsonPath("$.content[0].customer.contactInfo.name") { value("Company") } }
                content { jsonPath("$.content[0].customer.contactInfo.surname") { value("Test") } }
                content { jsonPath("$.content[0].offerStatus") { value("CREATED") } }
                content { jsonPath("$.content[0].professional") { value(null) } }

            }
        }

        @Test
        fun getJobOffersFilteredByStatus() {

            mockMvc.get("/API/joboffers/") {
                queryParam("status", "CANDIDATE_PROPOSAL")
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.content[0].id") { value(jobOfferID_2) } }
                content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
                content { jsonPath("$.content[0].professional.id") { value(professionalID_1) } }
                content { jsonPath("$.content[0].offerStatus") { value("CANDIDATE_PROPOSAL") } }
            }
            mockMvc.get("/API/joboffers/") {
                queryParam("status", "CREATED")
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.content[0].id") { value(jobOfferID_1) } }
                content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
                content { jsonPath("$.content[0].professional") { value(null) } }
                content { jsonPath("$.content[0].offerStatus") { value("CREATED") } }
            }
        }

        @Test
        fun getJobOffersFilteredByCustomer() {
            mockMvc.get("/API/joboffers/") {
                queryParam("customerId", customerID_1.toString())
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.content[0].id") { value(jobOfferID_1) } }
                content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
            }
        }

        @Test
        fun getJobOffersFilteredByProfessional() {
            mockMvc.get("/API/joboffers/") {
                queryParam("professionalId", professionalID_1.toString())
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.content[0].id") { value(jobOfferID_2) } }
                content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
                content { jsonPath("$.content[0].professional.id") { value(professionalID_1) } }
            }
        }

        @Test
        fun getJobOffersFilteredByCustomerAndProfessional()
        {
            fun getJobOffersFilteredByProfessional() {
                mockMvc.get("/API/joboffers/") {
                    queryParam("professionalId", professionalID_1.toString())
                    queryParam("customerId", customerID_1.toString())
                }.andExpect {
                    status { isOk() }
                    content { jsonPath("$.content[0].id") { value(jobOfferID_2) } }
                    content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
                    content { jsonPath("$.content[0].professional.id") { value(professionalID_1) } }
                }
            }
        }
        @Test
        fun getJobOffersFilteredByMultipleStatus() {
            mockMvc.get("/API/joboffers/") {
                queryParam("status", "CREATED")
                queryParam("status","CANDIDATE_PROPOSAL")
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.content[0].id") { value(jobOfferID_1) } }
                content { jsonPath("$.content[0].customer.id") { value(customerID_1) } }
                content { jsonPath("$.content[1].id") { value(jobOfferID_2) } }
                content { jsonPath("$.content[1].customer.id") { value(customerID_1) } }
                content { jsonPath("$.content[0].professional") { value(null) } }
                content { jsonPath("$.content[0].offerStatus") { value("CREATED") } }
                content { jsonPath("$.content[1].offerStatus") { value("CANDIDATE_PROPOSAL") } }
            }

        }
        @Test
        fun getJobOffersById(){
            mockMvc.get("/API/joboffers/$jobOfferID_1") {
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.id") { value(jobOfferID_1) } }
                content { jsonPath("$.customer.id") { value(customerID_1) } }
                content { jsonPath("$.professional") { value(null) } }
             }
        }
        @Test
        fun getJobOffersById_NotFound(){
            mockMvc.get("/API/joboffers/3525") {
            }.andExpect {
                status { isNotFound() }
            }
        }
        @Test
        fun getJobOffersValueNull(){
            mockMvc.get("/API/joboffers/$jobOfferID_1/value") {
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.value") { value(null) } }
            }
        }
        @Test
        fun getJobOffersValue(){
            mockMvc.get("/API/joboffers/$jobOfferID_2/value") {
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.value") { value(180.0) } }
            }
        }
        @Test
        fun getJobOffersValue_NotFound(){
            mockMvc.get("/API/joboffers/345345/value") {
            }.andExpect {
                status { isNotFound() }

            }
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class UpdateJobOffer {

        private var jobOffer = JobOffer(
            requiredSkills = mutableSetOf("skill1", "skill2"),
            30,
            "This is a description"
        )

        private var customer = Customer(
            Contact(
                "Mario",
                "Rossi",
                ContactCategory.CUSTOMER,
                "RSSMRA70A01L219K"
            ),
            "mock Customer"
        )

        private var professional = Professional(
            Contact(
                "Mario",
                "Rossi",
                ContactCategory.CUSTOMER,
                "RSSMRA70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )

        @BeforeEach
        fun init() {
            jobOfferRepository.deleteAll()
            professionalRepository.deleteAll()
            customerRepository.deleteAll()
            customer.addPlacement(jobOffer)
            customer = customerRepository.save(customer)
            jobOffer = jobOfferRepository.save(jobOffer)
            professional = professionalRepository.save(professional)
        }

        private fun updateStatus(updateDTO: JobOfferUpdateDTO) {
            mockMvc
                .post("/API/joboffers/${jobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(updateDTO)
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.id") { value(jobOffer.offerId) }
                        jsonPath("$.offerStatus") { value(updateDTO.status.toString()) }
                        jsonPath("$.professional.id") {
                            if(updateDTO.status != OfferStatus.ABORTED) {
                                if (updateDTO.status >= OfferStatus.CANDIDATE_PROPOSAL) {
                                    value(professional.professionalId)
                                } else {
                                    doesNotExist()
                                }
                            }
                        }
                    }
                }

            val professionalDb = professionalRepository.findById(professional.professionalId).get()
            assertEquals(
                professionalDb.employmentState,
                if(updateDTO.status == OfferStatus.CONSOLIDATED) EmploymentState.EMPLOYED else EmploymentState.UNEMPLOYED
            )
        }

        @Test
        fun updateJobOfferStatus_completeCycle() {
            val updateDTOs = listOf(
                JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE),
                JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId),
                JobOfferUpdateDTO(OfferStatus.CONSOLIDATED),
                JobOfferUpdateDTO(OfferStatus.DONE)
            )

            for(updateDTO in updateDTOs) {
                updateStatus(updateDTO)
            }
        }

        @Test
        fun updateJobOfferStatus_midwayAbort() {
            val updateDTOs = listOf(
                JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE),
                JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId),
                JobOfferUpdateDTO(OfferStatus.ABORTED)
            )

            for(updateDTO in updateDTOs) {
                updateStatus(updateDTO)
            }
        }

        @Test
        fun updateJobOfferStatus_twoCycles() {
            val updateDTOs = listOf(
                JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE),
                JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId),
                JobOfferUpdateDTO(OfferStatus.CONSOLIDATED),
                JobOfferUpdateDTO(OfferStatus.DONE),
                JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE),
                JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId),
                JobOfferUpdateDTO(OfferStatus.CONSOLIDATED),
                JobOfferUpdateDTO(OfferStatus.DONE),
            )

            for(updateDTO in updateDTOs) {
                updateStatus(updateDTO)
            }
        }

        @Test
        fun updateJobOfferStatus_invalidStatus() {
            mockMvc
                .post("/API/joboffers/${jobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.DONE))
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateJobOfferStatus_offerNotFound() {
            mockMvc
                .post("/API/joboffers/${jobOffer.offerId + 1}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateJobOfferStatus_professionalNotFound() {
            updateStatus(JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))

            mockMvc
                .post("/API/joboffers/${jobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId + 1))
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateJobOfferStatus_professionalNotGiven() {
            updateStatus(JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))

            mockMvc
                .post("/API/joboffers/${jobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL))
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateJobOfferStatus_professionalNotAvailable() {
            updateStatus(JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))
            updateStatus(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, professional.professionalId))

            professional.employmentState = EmploymentState.EMPLOYED
            professional = professionalRepository.save(professional)
            mockMvc
                .post("/API/joboffers/${jobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CONSOLIDATED))
                }.andExpect {
                    status { isBadRequest() }
                }
        }

    }
}