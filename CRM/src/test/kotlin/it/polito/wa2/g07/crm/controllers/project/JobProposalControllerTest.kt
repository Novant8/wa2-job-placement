package it.polito.wa2.g07.crm.controllers.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.lab03.toCustomerDto
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
import it.polito.wa2.g07.crm.services.project.JobProposalService
import netscape.javascript.JSObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(JobProposalController::class)
@AutoConfigureMockMvc(addFilters = false)
class JobProposalControllerTest (@Autowired val mockMvc: MockMvc){
    @MockkBean
    private lateinit var jobProposalService:JobProposalService
    @MockkBean
    private lateinit var jobOfferService:JobOfferService
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class GetJobProposalTests{

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
                "Luca",
                "Bianchi",
                ContactCategory.PROFESSIONAL,
                "RSSMRA70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )

        private val mockJobProposal = JobProposal(
            jobOffer = mockJobOffer
        )

        init {
            mockJobProposal.proposalID= 1L
            mockProfessional.professionalId=1L
            mockCustomer.addJobProposal(mockJobProposal)
            mockProfessional.addJobProposal(mockJobProposal)
            mockCustomer.addPlacement(mockJobOffer)
        }

        @BeforeEach
        fun initMocks(){
            every{ jobProposalService.searchJobProposalById(any(Long::class))} throws EntityNotFoundException("The job proposal was not found")
            every {jobProposalService.searchJobProposalById(mockJobProposal.proposalID)} returns mockJobProposal.toJobProposalDTO()
            every {jobProposalService.searchJobProposalByJobOfferAndProfessional(any(Long::class), any(Long::class))}throws EntityNotFoundException("The job proposal was not found")
            every {jobProposalService.searchJobProposalByJobOfferAndProfessional(mockJobProposal.proposalID, mockProfessional.professionalId)} returns mockJobProposal.toJobProposalDTO()
        }

        @Test
        fun getJobProposalSpecific(){
            mockMvc.get("/API/jobProposals/${mockJobProposal.proposalID}")
                .andExpect {
                    status { isOk() }
                }
                verify (exactly = 1){ jobProposalService.searchJobProposalById(mockJobProposal.proposalID)  }
        }

        @Test
        fun getJobProposalSpecificNotFound(){
            mockMvc.get("/API/jobProposals/${mockJobProposal.proposalID +1 }")
                .andExpect {
                    status { isNotFound() }
                }
            verify (exactly = 1){ jobProposalService.searchJobProposalById(mockJobProposal.proposalID +1)  }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessional(){
            mockMvc.get("/API/jobProposals/${mockJobProposal.proposalID}/${mockProfessional.professionalId}")
                .andExpect {
                    status { isOk() }
                }
            verify (exactly = 1){ jobProposalService.searchJobProposalByJobOfferAndProfessional(mockJobProposal.proposalID, mockProfessional.professionalId)  }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessionalNotFoundByID(){
            mockMvc.get("/API/jobProposals/${mockJobProposal.proposalID+1}/${mockProfessional.professionalId}")
                .andExpect {
                    status { isNotFound() }
                }
            verify (exactly = 1){ jobProposalService.searchJobProposalByJobOfferAndProfessional(mockJobProposal.proposalID+1, mockProfessional.professionalId)  }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessionalNotFoundByProfessional(){
            mockMvc.get("/API/jobProposals/${mockJobProposal.proposalID}/${mockProfessional.professionalId+1}")
                .andExpect {
                    status { isNotFound() }
                }
            verify (exactly = 1){ jobProposalService.searchJobProposalByJobOfferAndProfessional(mockJobProposal.proposalID, mockProfessional.professionalId+1)  }
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PostJobProposalTests{
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
                "Luca",
                "Bianchi",
                ContactCategory.PROFESSIONAL,
                "RSSMRA70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )



        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockProfessional.professionalId=1L
            mockCustomer.customerId= 1L
            mockJobOffer.offerId = 1L

        }

        private var mockJobProposal = JobProposalDTO(
            0L,
            mockCustomer.toCustomerDto(),
            mockProfessional.toProfessionalDto(),
            mockJobOffer.toJobOfferDTO(),
            null,
            ProposalStatus.CREATED,
            false,
            null
        )

        @BeforeEach
        fun initMocks(){
            every { jobProposalService.createJobProposal(mockCustomer.customerId,any(Long::class),mockJobOffer.offerId) } throws EntityNotFoundException("Professional was not found")
            every { jobProposalService.createJobProposal(any(Long::class),mockProfessional.professionalId,mockJobOffer.offerId) } throws EntityNotFoundException("Customer was not found")
            every { jobProposalService.createJobProposal(mockCustomer.customerId,mockProfessional.professionalId,any(Long::class)) } throws EntityNotFoundException("Job Offer was not found")
            every { jobProposalService.createJobProposal(mockCustomer.customerId,mockProfessional.professionalId,mockJobOffer.offerId) } answers  {mockJobProposal}

        }

        @Test
        fun createJobProposal(){
            mockMvc.post("/API/jobProposals/${mockCustomer.customerId}/${mockProfessional.professionalId}/${mockJobOffer.offerId}")
                .andExpect {
                    status { isCreated() }
                }
            verify(exactly = 1) { jobProposalService.createJobProposal(mockCustomer.customerId,mockProfessional.professionalId, mockJobOffer.offerId) }
        }

        @Test
        fun createJobProposal_NoCustomer(){
            mockMvc.post("/API/jobProposals/${mockCustomer.customerId+1}/${mockProfessional.professionalId}/${mockJobOffer.offerId}")
                .andExpect {
                    status { isNotFound() }
                }
            verify(exactly = 1) { jobProposalService.createJobProposal(mockCustomer.customerId+1,mockProfessional.professionalId, mockJobOffer.offerId) }
        }

        @Test
        fun createJobProposal_NoProfessional(){
            mockMvc.post("/API/jobProposals/${mockCustomer.customerId}/${mockProfessional.professionalId+1}/${mockJobOffer.offerId}")
                .andExpect {
                    status { isNotFound() }
                }
            verify(exactly = 1) { jobProposalService.createJobProposal(mockCustomer.customerId,mockProfessional.professionalId+1, mockJobOffer.offerId) }
        }

        @Test
        fun createJobProposal_NoJobOffer(){
            mockMvc.post("/API/jobProposals/${mockCustomer.customerId}/${mockProfessional.professionalId}/${mockJobOffer.offerId +1}")
                .andExpect {
                    status { isNotFound() }
                }
            verify(exactly = 1) { jobProposalService.createJobProposal(mockCustomer.customerId,mockProfessional.professionalId, mockJobOffer.offerId+1) }
        }

    }

    @Nested
    @WithMockUser(roles = [ "operator", "customer" ])
    inner class PutJobProposalTests {
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
                "Luca",
                "Bianchi",
                ContactCategory.PROFESSIONAL,
                "RSSMRA70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )

        private val mockJobProposal = JobProposal(
            jobOffer = mockJobOffer
        )


        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockProfessional.professionalId = 1L
            mockCustomer.customerId = 1L
            mockJobOffer.offerId = 1L
            mockJobProposal.proposalID = 1L
        }

        private var mockJobProposalDTO = JobProposalDTO(
            0L,
            mockCustomer.toCustomerDto(),
            mockProfessional.toProfessionalDto(),
            mockJobOffer.toJobOfferDTO(),
            null,
            ProposalStatus.CREATED,
            false,
            null
        )

        @BeforeEach
        fun initMocks() {
            every {
                jobProposalService.customerConfirmDecline(
                    any(Long::class),
                    any(Long::class),
                    any(Boolean::class)
                )
            } throws EntityNotFoundException("The job proposal was not found")
            every {
                jobProposalService.customerConfirmDecline(
                    mockJobProposal.proposalID,
                    any(Long::class),
                    any(Boolean::class)
                )
            } throws EntityNotFoundException("The customer does not belong to this proposal")
            every {
                jobProposalService.customerConfirmDecline(
                    mockJobProposal.proposalID,
                    mockCustomer.customerId,
                    any(Boolean::class)
                )
            } answers { mockJobProposalDTO }


            every {
                jobProposalService.professionalConfirmDecline(
                    any(Long::class),
                    any(Long::class),
                    any(Boolean::class)
                )
            } throws EntityNotFoundException("The job proposal was not found")
            every {
                jobProposalService.professionalConfirmDecline(
                    mockJobProposal.proposalID,
                    any(Long::class),
                    any(Boolean::class)
                )
            } throws EntityNotFoundException("The professional does not belong to this proposal")
            every {
                jobProposalService.professionalConfirmDecline(
                    mockJobProposal.proposalID,
                    mockProfessional.professionalId,
                    any(Boolean::class)
                )
            } answers { mockJobProposalDTO }

            every{jobProposalService.loadDocument(any(Long::class), any())} throws EntityNotFoundException("The job proposal was not found")
            every{jobProposalService.loadDocument(mockJobProposal.proposalID, any())} answers {mockJobProposalDTO}

            every{jobProposalService.loadSignedDocument(any(Long::class), any())} throws EntityNotFoundException("The job proposal was not found")
            every{jobProposalService.loadSignedDocument(mockJobProposal.proposalID, any())} answers {mockJobProposalDTO}

        }

        @Test
        fun customerConfirm(){
            val body = "true"
            mockJobProposalDTO.customerConfirmation= true


            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/${mockCustomer.customerId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.customerConfirmation") {value(true)}
                    }
                }
        }
        @Test
        fun customerDecline(){
            val body = "false"
            mockJobProposalDTO.status= ProposalStatus.DECLINED


            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/${mockCustomer.customerId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.customerConfirmation") {value( mockJobProposalDTO.customerConfirmation)}
                        jsonPath("$.status"){value(mockJobProposalDTO.status.toString())}
                    }
                }
        }

        @Test
        fun customerAcceptDecline_NoProposal(){
            val body = "true"

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID+1}/${mockCustomer.customerId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun customerAcceptDecline_NoCustomer(){
            val body = "true"

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/${mockCustomer.customerId+1}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun professionalConfirm(){
            val body = "true"
            mockJobProposalDTO.status= ProposalStatus.ACCEPTED


            mockMvc.put("/API/jobProposals/professional/${mockJobProposal.proposalID}/${mockProfessional.professionalId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.status") {value(mockJobProposalDTO.status.toString())}
                    }
                }
        }
        @Test
        fun professionalDecline(){
            val body = "false"
            mockJobProposalDTO.status= ProposalStatus.DECLINED


            mockMvc.put("/API/jobProposals/professional/${mockJobProposal.proposalID}/${mockProfessional.professionalId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.status"){value(mockJobProposalDTO.status.toString())}
                    }
                }
        }

        @Test
        fun professionalAcceptDecline_NoProposal(){
            val body = "true"

            mockMvc.put("/API/jobProposals/professional/${mockJobProposal.proposalID+1}/${mockProfessional.professionalId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun professionalAcceptDecline_NoProfessional(){
            val body = "true"

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/${mockProfessional.professionalId+1}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun loadDocument (){
            val documentId = 1L
            mockJobProposalDTO.documentId= documentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/document"){
                contentType = MediaType.APPLICATION_JSON
                content = documentId
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.documentId"){value(mockJobProposalDTO.documentId)}
                    }
                }
        }

        @Test
        fun loadDocumentNull (){
            val documentId = null
            mockJobProposalDTO.documentId= documentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/document"){
                contentType = MediaType.APPLICATION_JSON
                content = documentId
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.documentId"){value(mockJobProposalDTO.documentId)}
                    }
                }
        }

        @Test
        fun loadDocument_NoProposal (){
            val documentId = 1L
            mockJobProposalDTO.documentId= documentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID+1}/document"){
                contentType = MediaType.APPLICATION_JSON
                content = documentId
            }
                .andExpect {
                    status { isNotFound() }

                }
        }

        @Test
        fun loadSignedDocument (){
            val signedDocumentId = 1L
            mockJobProposalDTO.professionalSignedContract= signedDocumentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.professionalSignedContract"){value(mockJobProposalDTO.professionalSignedContract)}
                    }
                }
        }

        @Test
        fun loadSignedDocumentNull (){
            val signedDocumentId = null
            mockJobProposalDTO.professionalSignedContract= signedDocumentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.professionalSignedContract"){value(mockJobProposalDTO.professionalSignedContract)}
                    }
                }
        }

        @Test
        fun loadSignedDocument_NoProposal (){
            val signedDocumentId = 1L
            mockJobProposalDTO.professionalSignedContract= signedDocumentId

            mockMvc.put("/API/jobProposals/${mockJobProposal.proposalID+1}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isNotFound() }

                }
        }
    }
}