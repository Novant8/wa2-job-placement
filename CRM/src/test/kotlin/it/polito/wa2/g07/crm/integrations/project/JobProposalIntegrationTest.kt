package it.polito.wa2.g07.crm.integrations.project

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.verify
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab03.toCustomerDto
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
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
import org.springframework.test.web.servlet.put

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc(addFilters = false)
class JobProposalIntegrationTest :CrmApplicationTests(){
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
    lateinit var jobProposalRepository: JobProposalRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class GetJobProposal{

        private var customerID = 0L
        private var jobOfferID = 0L
        private var professionalID = 0L
        private var jobProposalID = 0L

        @BeforeEach
        fun init(){
            jobProposalRepository.deleteAll()
            jobOfferRepository.deleteAll()
            customerRepository.deleteAll()
            professionalRepository.deleteAll()
            contactRepository.deleteAll()

            val contactC = Contact("Company", "Test", ContactCategory.CUSTOMER)
            val contactP = Contact("Professional", "Worker", ContactCategory.PROFESSIONAL)

            val customer = Customer(contactC, "Affidabile")
            val professional = Professional(contactP, "Torino", setOf("ITA"), 20.0, EmploymentState.UNEMPLOYED)

            val jobOffer = JobOffer( mutableSetOf("ITA", "ENG"), 45, "this is a job", OfferStatus.CREATED, "notes")

            val jobProposal = JobProposal(jobOffer)

            customer.addPlacement(jobOffer)
            customer.addJobProposal(jobProposal)
            professional.addJobProposal(jobProposal)

            professionalID= professionalRepository.save(professional).professionalId
            customerID = customerRepository.save(customer).customerId
            jobOfferID = jobOfferRepository.save(jobOffer).offerId
            jobProposalID= jobProposalRepository.save(jobProposal).proposalID
        }

        @Test
        fun getProposalById(){
            mockMvc.get("/API/jobProposals/$jobProposalID"){
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.id") {value(jobProposalID)} }
                content { jsonPath("$.customer.id") {value(customerID)} }
                content { jsonPath("$.professional.id") {value(professionalID)} }
                content { jsonPath("$.jobOffer.id") {value(jobOfferID)} }
                content { jsonPath("$.documentId") {value(null)} }
                content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                content { jsonPath("$.customerConfirmation") {value(false)} }
                content { jsonPath("$.professionalSignedContract") {value(null)} }
            }
        }
        @Test
        fun getProposalById_NotFound(){
            mockMvc.get("/API/jobProposals/${jobProposalID + 1}") {
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessional(){
            mockMvc.get("/API/jobProposals/$jobOfferID/$professionalID"){
            }.andExpect {
                status { isOk() }
                content { jsonPath("$.id") {value(jobProposalID)} }
                content { jsonPath("$.customer.id") {value(customerID)} }
                content { jsonPath("$.professional.id") {value(professionalID)} }
                content { jsonPath("$.jobOffer.id") {value(jobOfferID)} }
                content { jsonPath("$.documentId") {value(null)} }
                content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                content { jsonPath("$.customerConfirmation") {value(false)} }
                content { jsonPath("$.professionalSignedContract") {value(null)} }
            }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessionalNotFoundByID(){
            mockMvc.get("/API/jobProposals/${jobProposalID + 1}/$professionalID") {
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun getJobProposalSpecificByIdAndProfessionalNotFoundByProfessional(){
            mockMvc.get("/API/jobProposals/$jobProposalID /${professionalID +1 }") {
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PostJobProposal{
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
                "Luca",
                "Bianchi",
                ContactCategory.PROFESSIONAL,
                "LCCBNC70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )

        @BeforeEach
        fun init() {
            jobProposalRepository.deleteAll()
            jobOfferRepository.deleteAll()
            professionalRepository.deleteAll()
            customerRepository.deleteAll()
            customer.addPlacement(jobOffer)
            customer = customerRepository.save(customer)
            jobOffer = jobOfferRepository.save(jobOffer)
            professional = professionalRepository.save(professional)
        }

        @Test
        fun createJobProposal(){
            mockMvc.post("/API/jobProposals/${customer.customerId}/${professional.professionalId}/${jobOffer.offerId}")
                .andExpect {
                    status { isCreated() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(null)} }
                    content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(null)} }
                }

        }

        @Test
        fun createJobProposal_NoCustomer(){
            mockMvc.post("/API/jobProposals/${customer.customerId+1}/${professional.professionalId}/${jobOffer.offerId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun createJobProposal_NoProfessional(){
            mockMvc.post("/API/jobProposals/${customer.customerId}/${professional.professionalId+1}/${jobOffer.offerId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun createJobProposal_NoJobOffer(){
            mockMvc.post("/API/jobProposals/${customer.customerId}/${professional.professionalId}/${jobOffer.offerId +1}")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

    @Nested
    @WithMockUser(roles = [ "operator", "customer" ])

    inner class PutJobProposal{
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
                "Luca",
                "Bianchi",
                ContactCategory.PROFESSIONAL,
                "LCCBNC70A01L219K"
            ),
            "Torino",
            skills = mutableSetOf("skill1", "skill2"),
            100.0
        )

        private var jobProposal = JobProposal(
            jobOffer = jobOffer
        )

        @BeforeEach
        fun init() {
            jobProposalRepository.deleteAll()
            jobOfferRepository.deleteAll()
            professionalRepository.deleteAll()
            customerRepository.deleteAll()
            customer.addPlacement(jobOffer)
            customer.addJobProposal(jobProposal)
            professional.addJobProposal(jobProposal)
            customer = customerRepository.save(customer)
            jobOffer = jobOfferRepository.save(jobOffer)
            professional = professionalRepository.save(professional)
            jobProposal=jobProposalRepository.save(jobProposal)
        }

        @Test
        fun customerConfirm(){
            val body = "true"

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/${customer.customerId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.customer.id") {value(customer.customerId)} }
                        content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                        content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                        content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                        content { jsonPath("$.status") {value(jobProposal.status.toString())} }
                        content { jsonPath("$.customerConfirmation") {value(true)} }
                        content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }
                    }
                }
        }

        @Test
        fun customerDecline(){
            val body = "false"

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/${customer.customerId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.DECLINED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }

                }
        }

        @Test
        fun customerAcceptDecline_NoProposal(){
            val body = "true"

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID+1}/${customer.customerId}"){
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

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/${customer.customerId+1}"){
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


            mockMvc.put("/API/jobProposals/professional/${jobProposal.proposalID}/${professional.professionalId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.ACCEPTED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }

                }
        }

        @Test
        fun professionalDecline(){
            val body = "false"


            mockMvc.put("/API/jobProposals/professional/${jobProposal.proposalID}/${professional.professionalId}"){
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.DECLINED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }

                }
        }

        @Test
        fun professionalAcceptDecline_NoProposal(){
            val body = "true"

            mockMvc.put("/API/jobProposals/professional/${jobProposal.proposalID+1}/${professional.professionalId}"){
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

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/${professional.professionalId+1}"){
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


            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/document"){
                contentType = MediaType.APPLICATION_JSON
                content = documentId
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }

                }
        }

        @Test
        fun loadDocumentNull (){
            val documentId = null


            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/document"){
                contentType = MediaType.APPLICATION_JSON
                content = documentId
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(jobProposal.professionalSignedContract)} }

                }
        }


        @Test
        fun loadDocument_NoProposal (){
            val documentId = 1L

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID+1}/document"){
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


            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(signedDocumentId)} }

                }
        }

        @Test
        fun loadSignedDocumentNull (){
            val signedDocumentId = null


            mockMvc.put("/API/jobProposals/${jobProposal.proposalID}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isOk() }
                    content { jsonPath("$.customer.id") {value(customer.customerId)} }
                    content { jsonPath("$.professional.id") {value(professional.professionalId)} }
                    content { jsonPath("$.jobOffer.id") {value(jobOffer.offerId)} }
                    content { jsonPath("$.documentId") {value(jobProposal.documentId)} }
                    content { jsonPath("$.status") {value(ProposalStatus.CREATED.toString())} }
                    content { jsonPath("$.customerConfirmation") {value(false)} }
                    content { jsonPath("$.professionalSignedContract") {value(signedDocumentId)} }

                }
        }


        @Test
        fun loadSignedDocument_NoProposal (){
            val signedDocumentId = 1L

            mockMvc.put("/API/jobProposals/${jobProposal.proposalID+1}/signedDocument"){
                contentType = MediaType.APPLICATION_JSON
                content = signedDocumentId
            }
                .andExpect {
                    status { isNotFound() }

                }
        }


    }
}