package it.polito.wa2.g07.crm.controllers.lab03

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*
import javax.swing.text.html.Option

@WebMvcTest(JobOfferController::class)
class JobOfferControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var jobOfferService: JobOfferService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Nested
    inner class UpdateJobOfferTests {

        private val mockJobOffer = JobOffer(
            requiredSkills = mutableSetOf("skill1", "skill2"),
            30,
            "This is a description"
        )

        private val mockCustomer = Customer(
            Contact(
                "Mario",
                "Rossi",
                ContactCategory.CUSTOMER,
                "RSSMRA70A01L219K"
            ),
            "mock Customer"
        )

        private val mockProfessional = Professional(
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

        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockJobOffer.offerId = 1L
        }

        @BeforeEach
        fun initMocks() {
            every { jobOfferService.updateJobOfferStatus(any(Long::class), any(JobOfferUpdateDTO::class)) } throws EntityNotFoundException("Job offer not found")
            every { jobOfferService.updateJobOfferStatus(mockJobOffer.offerId, any(JobOfferUpdateDTO::class)) } answers {
                val jobOfferUpdateDTO = secondArg<JobOfferUpdateDTO>()
                mockJobOffer.status = jobOfferUpdateDTO.status
                if(jobOfferUpdateDTO.status == OfferStatus.CANDIDATE_PROPOSAL) {
                    mockJobOffer.professional = mockProfessional
                }
                mockJobOffer.toJobOfferDTO()
            }
            every { jobOfferService.updateJobOfferStatus(mockJobOffer.offerId, match { !mockJobOffer.status.canUpdateTo(it.status) }) } throws InvalidParamsException("Cannot update to status")
            every { jobOfferService.updateJobOfferStatus(mockJobOffer.offerId, match { it.status == OfferStatus.CANDIDATE_PROPOSAL && it.professionalId != mockProfessional.professionalId }) } throws EntityNotFoundException("Professional not found")
            every { jobOfferService.updateJobOfferStatus(mockJobOffer.offerId, match { it.status == OfferStatus.CANDIDATE_PROPOSAL && it.professionalId == null }) } throws InvalidParamsException("Professional not given")
        }

        @BeforeEach
        fun resetJobOfferStatus() {
            mockJobOffer.status = OfferStatus.CREATED
            mockJobOffer.professional = null
        }

        @Test
        fun updateJobOfferStatus_noProfessional() {
            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.id") { value(mockJobOffer.offerId) }
                        jsonPath("$.offerStatus") { value(OfferStatus.SELECTION_PHASE.toString()) }
                        jsonPath("$.professional") { doesNotExist() }
                    }
                }
        }

        @Test
        fun updateJobOfferStatus_withProfessional() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE

            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, mockProfessional.professionalId))
                }.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.id") { value(mockJobOffer.offerId) }
                        jsonPath("$.offerStatus") { value(OfferStatus.CANDIDATE_PROPOSAL.toString()) }
                        jsonPath("$.professional.id") { value(mockProfessional.professionalId) }
                    }
                }
        }

        @Test
        fun updateJobOfferStatus_invalidStatus() {
            // Cannot update to status
            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.DONE))
                }.andExpect {
                    status { isBadRequest() }
                }

            // Status does not exist
            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{ "status": "invalid" }"""
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateJobOfferStatus_offerNotFound() {
            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId + 1}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.DONE))
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun updateJobOfferStatus_professionalNotGiven() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE

            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL))
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun updateJobOfferStatus_professionalNotFound() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE

            mockMvc
                .post("/API/joboffers/${mockJobOffer.offerId}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, mockProfessional.professionalId + 1))
                }.andExpect {
                    status { isNotFound() }
                }
        }

    }

    @Nested
    inner class GetJobOfferTest{
        private val mockJobOffer = JobOffer(
            requiredSkills = mutableSetOf("skill1", "skill2"),
            30,
            "This is a description"
        )

        private val mockCustomer = Customer(
            Contact(
                "Mario",
                "Rossi",
                ContactCategory.CUSTOMER,
                "RSSMRA70A01L219K"
            ),
            "mock Customer"
        )

        private val mockProfessional = Professional(
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

        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockJobOffer.offerId = 1L
        }
        private val pageImpl = PageImpl(listOf(mockJobOffer.toJobOfferReducedDTO()))
        private val mockValue = ValueDTO(20.0)
        @BeforeEach
        fun initMocks() {
            every { jobOfferService.searchJobOffer(any(JobOfferFilterDTO::class), any(Pageable::class)) } returns pageImpl
            every { jobOfferService.searchJobOfferById(any(Long::class)) } throws EntityNotFoundException("The job offer was not found")
            every { jobOfferService.searchJobOfferById(mockJobOffer.offerId) } returns mockJobOffer.toJobOfferDTO()
            every { jobOfferService.getJobOfferValue(any(Long::class)) } throws EntityNotFoundException("The job offer was not found")
            every { jobOfferService.getJobOfferValue(mockJobOffer.offerId) } returns 20.0
            every { jobOfferService.getJobOfferValue(mockJobOffer.offerId+1) } returns null

         //

        }
        @Test
        fun getAllJobOffer(){
            mockMvc.get("/API/joboffers"){
                contentType = MediaType.APPLICATION_JSON
                content= objectMapper.writeValueAsString(JobOfferFilterDTO())

            }.andExpect {
                    status { isOk() }
            }
            verify(exactly = 1) { jobOfferService.searchJobOffer(JobOfferFilterDTO(),any()) }
        }
        @Test
        fun getJobOfferSpecific(){
            mockMvc.get("/API/joboffers/${mockJobOffer.offerId}")
                .andExpect {
                status { isOk() }
            }
            verify(exactly = 1) { jobOfferService.searchJobOfferById(mockJobOffer.offerId) }

        }
        @Test
        fun getJobOfferSpecificNotFound(){
            mockMvc.get("/API/joboffers/${mockJobOffer.offerId+1}")
                .andExpect {
                    status { isNotFound() }
                }
            verify(exactly = 1) { jobOfferService.searchJobOfferById(mockJobOffer.offerId+1) }
        }
        @Test
        fun getJobOfferValue(){

                mockMvc.get("/API/joboffers/${mockJobOffer.offerId}/value")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.value") { value(20.0) }
                    }
                verify(exactly = 1) { jobOfferService.getJobOfferValue(mockJobOffer.offerId) }

        }
        @Test
        fun getJobOfferValueNotValid(){

            mockMvc.get("/API/joboffers/${mockJobOffer.offerId+1}/value")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.value") { value(null) }
                }
            verify(exactly = 1) { jobOfferService.getJobOfferValue(mockJobOffer.offerId+1) }

        }
        @Test
        fun getJobOfferValueNotFound(){

            mockMvc.get("/API/joboffers/${mockJobOffer.offerId+2}/value")
                .andExpect {
                    status { isNotFound() }

                }
            verify(exactly = 1) { jobOfferService.getJobOfferValue(mockJobOffer.offerId+2) }

        }
    }

}