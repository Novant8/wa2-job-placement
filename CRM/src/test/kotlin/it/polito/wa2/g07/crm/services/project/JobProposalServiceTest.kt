package it.polito.wa2.g07.crm.services.project

import io.mockk.*
import it.polito.wa2.g07.crm.dtos.lab02.ContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.dtos.project.JobProposalDTO
import it.polito.wa2.g07.crm.dtos.project.toJobProposalDTO
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.JobProposalValidationException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import it.polito.wa2.g07.crm.repositories.project.JobProposalRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class JobProposalServiceTest {
    private val customerRepository= mockk<CustomerRepository>()
    private val jobOfferRepository= mockk<JobOfferRepository>()
    private val professionalRepository = mockk<ProfessionalRepository>()
    private val jobProposalRepository = mockk<JobProposalRepository>()

    private val service = JobProposalServiceImpl(jobOfferRepository,customerRepository,professionalRepository,jobProposalRepository)

    @Nested
    inner class CreateJobProposal(){

        private val jobProposalSlot = slot<JobProposal>()

        private val mockCustomerContact: Contact = Contact(
            "Mario",
            "Rossi",
            ContactCategory.CUSTOMER,
            "RSSMRA70A01L219K"
        )

        private val mockProfessionalContact : Contact = Contact(
            "Luca",
            "Bianchi",
            ContactCategory.PROFESSIONAL,
            "LCCBNH70A01L219K"
        )

        private val mockCustomer: Customer = Customer(
            mockCustomerContact,
            "mock Customer"
        )

        private val mockProfessional: Professional = Professional(
            mockProfessionalContact,
            "Torino",
            setOf("skill1"),
            5.0,
        )

        private val mockJobOffer:JobOffer = JobOffer(
            mutableSetOf("skill1", "skill2"),
            5L,
            "jobOffer description "
        )

        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockCustomer.customerId = 1L
            mockProfessional.professionalId= 1L
            mockJobOffer.offerId = 1L
        }

        @BeforeEach
        fun initMocks(){

            every{ jobProposalRepository.save(capture(jobProposalSlot))} answers { firstArg<JobProposal>()}
            every{ customerRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ customerRepository.findById(mockCustomer.customerId)} returns   Optional.of(mockCustomer)
            every{ professionalRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ professionalRepository.findById(mockProfessional.professionalId)} returns   Optional.of(mockProfessional)
            every{ jobOfferRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ jobOfferRepository.findById(mockJobOffer.offerId)} returns   Optional.of(mockJobOffer)
        }

        @Test
        fun createJobOffer(){
            val expectedDTO = JobProposalDTO(
                customer =  CustomerDTO(1L, ContactDTO(0L, "Mario", "Rossi", ContactCategory.CUSTOMER, emptyList(),"RSSMRA70A01L219K"),"mock Customer" ),
                professional = ProfessionalDTO(1L, ContactDTO(0L, "Luca", "Bianchi", ContactCategory.PROFESSIONAL, emptyList(),"LCCBNH70A01L219K"), "Torino", setOf("skill1"),5.0,EmploymentState.UNEMPLOYED,null,null  ),
                jobOffer = JobOfferDTO(1L,"jobOffer description ",  ReducedCustomerDTO(1L, ReducedContactDTO(0L,"Mario","Rossi",ContactCategory.CUSTOMER),null),mutableSetOf("skill1", "skill2"), 5L ,OfferStatus.CREATED, null,null,null,
                    emptyList(),
                    emptyList() ),
                customerConfirmation = false,
                status = ProposalStatus.CREATED,
                id = 0L,
                documentId = null,
                professionalSignedContract = null
            )

            val result = service.createJobProposal(mockCustomer.customerId, mockProfessional.professionalId, mockJobOffer.offerId)
            assertEquals (result,expectedDTO)
            verify (exactly = 1) { customerRepository.findById(any(Long::class)) }
            verify (exactly = 1) { professionalRepository.findById(any(Long::class)) }
            verify (exactly = 1) { jobOfferRepository.findById(any(Long::class)) }
            verify (exactly = 1) { jobProposalRepository.save(any(JobProposal::class)) }
        }

        @Test
        fun createJobProposalNotExistCustomer(){
            assertThrows<EntityNotFoundException> {
                service.createJobProposal(10,1L,1L)
            }


            verify{ jobOfferRepository.save(any(JobOffer::class)) wasNot called}
            verify (exactly = 1) { customerRepository.findById(any(Long::class))  }

        }

        @Test
        fun createJobProposalNotExistProfessional(){
            assertThrows<EntityNotFoundException> {
                service.createJobProposal(1L,10,1L)
            }


            verify{ jobOfferRepository.save(any(JobOffer::class)) wasNot called}
            verify (exactly = 1) { professionalRepository.findById(any(Long::class))  }

        }

        @Test
        fun createJobProposalNotExistJobOffer(){
            assertThrows<EntityNotFoundException> {
                service.createJobProposal(1L,1L,10)
            }


            verify{ jobOfferRepository.save(any(JobOffer::class)) wasNot called}
            verify (exactly = 1) { jobOfferRepository.findById(any(Long::class))  }

        }
    }
    @Nested
    inner class GetJobProposal{

        private val mockCustomerContact: Contact = Contact(
            "Mario",
            "Rossi",
            ContactCategory.CUSTOMER,
            "RSSMRA70A01L219K"
        )

        private val mockProfessionalContact : Contact = Contact(
            "Luca",
            "Bianchi",
            ContactCategory.PROFESSIONAL,
            "LCCBNH70A01L219K"
        )

        private val mockCustomer: Customer = Customer(
            mockCustomerContact,
            "mock Customer"
        )

        private val mockProfessional: Professional = Professional(
            mockProfessionalContact,
            "Torino",
            setOf("skill1"),
            5.0,
        )

        private val mockJobOffer:JobOffer = JobOffer(
            mutableSetOf("skill1", "skill2"),
            5L,
            "jobOffer description "
        )
        private val mockJobProposal:JobProposal= JobProposal(
           mockJobOffer
        )

        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockCustomer.customerId = 1L
            mockProfessional.professionalId= 1L
            mockJobOffer.offerId = 1L
            mockJobProposal.proposalID= 1L
            mockCustomer.addJobProposal(mockJobProposal)
            mockProfessional.addJobProposal(mockJobProposal)
        }

        @BeforeEach
        fun initMocks(){
            every{ jobProposalRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ jobProposalRepository.findById(mockJobProposal.proposalID)} returns   Optional.of(mockJobProposal)
            every{ jobProposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(any(Long::class), any(Long::class), any(ProposalStatus::class))} returns  Optional.empty()
            every{ jobProposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(mockJobOffer.offerId, mockProfessional.professionalId, ProposalStatus.DECLINED)} returns  Optional.of(mockJobProposal)

        }

        @Test
        fun getJobProposalByID (){
            val result = service.searchJobProposalById(mockJobProposal.proposalID)
            verify (exactly = 1){ jobProposalRepository.findById(mockJobProposal.proposalID) }
            assertEquals(result,mockJobProposal.toJobProposalDTO())
        }

        @Test
        fun getJobProposalByID_NotFound (){
            assertThrows<EntityNotFoundException> {
                service.searchJobProposalById(mockJobProposal.proposalID+1)
            }
            verify{ jobProposalRepository.findById(mockJobProposal.proposalID) wasNot called }
        }
        @Test
        fun getJobProposalByOfferAndProfessional (){
            val result = service.searchJobProposalByJobOfferAndProfessional(mockJobOffer.offerId, mockProfessional.professionalId)
            verify (exactly = 1){ jobProposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(mockJobOffer.offerId, mockProfessional.professionalId, ProposalStatus.DECLINED) }
            assertEquals(result,mockJobProposal.toJobProposalDTO())
        }

        @Test
        fun getJobProposalByOfferAndProfessional_NoOffer (){
            assertThrows<EntityNotFoundException> {
                service.searchJobProposalByJobOfferAndProfessional(mockJobOffer.offerId+1, mockProfessional.professionalId)
            }
            verify{ jobProposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(mockJobOffer.offerId, mockProfessional.professionalId, ProposalStatus.DECLINED) wasNot called }
        }

        @Test
        fun getJobProposalByOfferAndProfessional_NoProfessional (){
            assertThrows<EntityNotFoundException> {
                service.searchJobProposalByJobOfferAndProfessional(mockJobOffer.offerId, mockProfessional.professionalId+1)
            }
            verify{ jobProposalRepository.findByJobOffer_OfferIdAndProfessional_ProfessionalIdAndStatusNot(mockJobOffer.offerId, mockProfessional.professionalId, ProposalStatus.DECLINED) wasNot called }
        }


    }

    @Nested
    inner class UpdateJobProposal{
        private val mockCustomerContact: Contact = Contact(
            "Mario",
            "Rossi",
            ContactCategory.CUSTOMER,
            "RSSMRA70A01L219K"
        )

        private val mockProfessionalContact : Contact = Contact(
            "Luca",
            "Bianchi",
            ContactCategory.PROFESSIONAL,
            "LCCBNH70A01L219K"
        )

        private val mockCustomer: Customer = Customer(
            mockCustomerContact,
            "mock Customer"
        )

        private val mockProfessional: Professional = Professional(
            mockProfessionalContact,
            "Torino",
            setOf("skill1"),
            5.0,
        )

        private val mockJobOffer:JobOffer = JobOffer(
            mutableSetOf("skill1", "skill2"),
            5L,
            "jobOffer description "
        )
        private val mockJobProposal:JobProposal= JobProposal(
            mockJobOffer
        )

        init {
            mockCustomer.addPlacement(mockJobOffer)
            mockCustomer.customerId = 1L
            mockProfessional.professionalId= 1L
            mockJobOffer.offerId = 1L
            mockJobProposal.proposalID= 1L
            mockCustomer.addJobProposal(mockJobProposal)
            mockProfessional.addJobProposal(mockJobProposal)
        }
        @BeforeEach
        fun initMocks(){
            every{ jobProposalRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ jobProposalRepository.findById(mockJobProposal.proposalID)} returns   Optional.of(mockJobProposal)
            every{ jobProposalRepository.save(any(JobProposal::class))} answers  {firstArg<JobProposal>()}
        }

        @BeforeEach
        fun resetJobProposalStatus() {
            mockJobProposal.status= ProposalStatus.CREATED
            mockJobProposal.documentId= null;
            mockJobProposal.customerConfirm = false
            mockJobProposal.professionalSignedContract= null
        }

        @Test
        fun customerConfirm (){
            mockJobProposal.documentId = 1L
            val result = service.customerConfirmDecline(mockJobProposal.proposalID,mockCustomer.customerId, true)
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }
        @Test
        fun customerConfirm_Error (){
          assertThrows<JobProposalValidationException> {
              service.customerConfirmDecline(mockJobProposal.proposalID,mockCustomer.customerId, true)
          }
            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }
        }

        @Test
        fun customerDecline (){
            val result = service.customerConfirmDecline(mockJobProposal.proposalID,mockCustomer.customerId, false)
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }

        @Test
        fun customerConfirmDecline_NoCustomer(){
            assertThrows<EntityNotFoundException> {
                service.customerConfirmDecline(mockJobProposal.proposalID, mockCustomer.customerId+1, true)
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }

        @Test
        fun customerConfirmDecline_NoCProposal(){
            assertThrows<EntityNotFoundException> {
                service.customerConfirmDecline(mockJobProposal.proposalID+1, mockCustomer.customerId, true)
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }

        @Test
        fun professionalConfirm (){
            mockJobProposal.customerConfirm = true
            mockJobProposal.documentId = 1L
            mockJobProposal.professionalSignedContract= 1L
            val result = service.professionalConfirmDecline(mockJobProposal.proposalID,mockProfessional.professionalId, true)
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }

        @Test
        fun professionalDecline (){
            mockJobProposal.customerConfirm = true
            val result = service.professionalConfirmDecline(mockJobProposal.proposalID,mockProfessional.professionalId, false)
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }

        @Test
        fun professionalConfirmDecline_NoCustomer(){
            assertThrows<EntityNotFoundException> {
                service.professionalConfirmDecline(mockJobProposal.proposalID, mockProfessional.professionalId+1, true)
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }

        @Test
        fun professionalConfirmDecline_NoCProposal(){
            assertThrows<EntityNotFoundException> {
                service.professionalConfirmDecline(mockJobProposal.proposalID+1, mockProfessional.professionalId, true)
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }

        @Test
        fun loadDocument (){
            val result = service.loadDocument(mockJobProposal.proposalID, 1L )
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }
        @Test
        fun loadDocumentNull (){
            val result = service.loadDocument(mockJobProposal.proposalID, null )
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }
        @Test
        fun loadDocument_NoProposal(){
            assertThrows<EntityNotFoundException> {
                service.loadDocument(mockJobProposal.proposalID+1, 1L )
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }

        @Test
        fun loadSignedDocument (){
            mockJobProposal.documentId= 1L
            val result = service.loadSignedDocument(mockJobProposal.proposalID, 1L )
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }
        @Test
        fun loadSignedDocumentNull (){
            mockJobProposal.documentId= 1L
            val result = service.loadSignedDocument(mockJobProposal.proposalID, null )
            assertEquals(result,mockJobProposal.toJobProposalDTO())
            verify { jobProposalRepository.save(any(JobProposal::class)) }
        }
        @Test
        fun loadSignedDocument_NoProposal(){
            assertThrows<EntityNotFoundException> {
                service.loadSignedDocument(mockJobProposal.proposalID+1, 1L )
            }

            verify (exactly = 0){ jobProposalRepository.save(any(JobProposal::class)) }

        }



    }

}