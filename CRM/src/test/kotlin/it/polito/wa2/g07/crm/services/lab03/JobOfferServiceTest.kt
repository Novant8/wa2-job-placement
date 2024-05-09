package it.polito.wa2.g07.crm.services.lab03

import io.mockk.*

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab03.*

import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException

import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import java.util.*

class JobOfferServiceTest {

    private val customerRepository= mockk<CustomerRepository>()
    private val jobOfferRepository= mockk<JobOfferRepository>()
    private val professionalRepository = mockk<ProfessionalRepository>()

    private val service = JobOfferServiceImpl(jobOfferRepository,customerRepository,professionalRepository)
    @Nested
    inner class CreateJobOffer(){
        private val jobOfferSlot = slot<JobOffer>()
        private val mockContact: Contact = Contact(
            "Mario",
            "Rossi",
            ContactCategory.CUSTOMER,
            "RSSMRA70A01L219K"
        )

        private val mockCustomer:Customer = Customer(
            mockContact,
            "mock Customer"
        )

        init {
            mockCustomer.customerId=1L;
        }

        @BeforeEach
        fun initMocks(){

            every{ jobOfferRepository.save(capture(jobOfferSlot))} answers { firstArg<JobOffer>()}
            every{ customerRepository.findById(any(Long::class))} returns  Optional.empty()
            every{ customerRepository.findById(mockCustomer.customerId)} returns   Optional.of(mockCustomer)

        }
       @Test
        fun createJobOffer(){
            val createJobOfferDTO = JobOfferCreateDTO("test",mutableSetOf("skill1"), 90, notes = "nota")

            val expectedDto = JobOfferDTO(
                customer = ReducedCustomerDTO(1L, ReducedContactDTO(0L,"Mario","Rossi",ContactCategory.CUSTOMER),null),
                duration = 90,
                description = "test",
                requiredSkills=mutableSetOf("skill1"),
                notes = "nota",
                id=0L,
                professional = null,
                value = null,
                offerStatus = OfferStatus.CREATED
            )
            val result = service.createJobOffer(mockCustomer.customerId,createJobOfferDTO)
            assertEquals(result, expectedDto)
            verify (exactly = 1) { jobOfferRepository.save(any(JobOffer::class)) }
            verify (exactly = 1) { customerRepository.findById(any(Long::class)) }

        }
        @Test
        fun createJobOfferNotExistCustomer(){
            val createJobOfferDTO = JobOfferCreateDTO("Descrizione",mutableSetOf("skill1"), 90, notes = "nota")
            assertThrows<EntityNotFoundException> {
                 service.createJobOffer(10,createJobOfferDTO)
            }


            verify{ jobOfferRepository.save(any(JobOffer::class)) wasNot called}
            verify (exactly = 1) { customerRepository.findById(any(Long::class))  }

        }

    }

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
            every { jobOfferRepository.save(any(JobOffer::class)) } answers { firstArg<JobOffer>() }
            every { jobOfferRepository.findById(any(Long::class)) } returns Optional.empty()
            every { jobOfferRepository.findById(mockJobOffer.offerId) } returns Optional.of(mockJobOffer)
            every { professionalRepository.findById(any(Long::class)) } returns Optional.empty()
            every { professionalRepository.findById(mockProfessional.professionalId) } returns Optional.of(mockProfessional)
        }

        @BeforeEach
        fun resetJobOfferStatus() {
            mockJobOffer.status = OfferStatus.CREATED
            mockJobOffer.professional = null
        }

        @Test
        fun updateJobOfferStatus_validStatus_noProfessional() {
            val result = service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))

            assertEquals(result, mockJobOffer.toJobOfferDTO())
            verify { jobOfferRepository.save(any(JobOffer::class)) }
        }

        @Test
        fun updateJobOfferStatus_validStatus_withProfessional() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE
            val result = service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, mockProfessional.professionalId))

            assertEquals(result, mockJobOffer.toJobOfferDTO())
            assertEquals(result.professional, mockProfessional.toProfessionalReducedDTO_Basic())
            verify { jobOfferRepository.save(any(JobOffer::class)) }
        }

        @Test
        fun updateJobOfferStatus_removeProfessional() {
            mockJobOffer.status = OfferStatus.CONSOLIDATED
            mockJobOffer.professional = mockProfessional

            val result = service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))
            assertNull(result.professional)
            verify { jobOfferRepository.save(any(JobOffer::class)) }
        }

        @Test
        fun updateJobOfferStatus_invalidStatus() {
            assertThrows<InvalidParamsException> {
                service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.DONE))
            }

            verify(exactly = 0) { jobOfferRepository.save(any(JobOffer::class)) }
        }

        @Test
        fun updateJobOfferStatus_offerNotFound() {
            assertThrows<EntityNotFoundException> {
                service.updateJobOfferStatus(mockJobOffer.offerId + 1, JobOfferUpdateDTO(OfferStatus.SELECTION_PHASE))
            }

            verify(exactly = 0) { jobOfferRepository.save(any(JobOffer::class)) }
        }

        @Test
        fun updateJobOfferStatus_professionalNotGiven() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE

            assertThrows<InvalidParamsException> {
                service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL))
            }

            verify(exactly = 0) { jobOfferRepository.save(any(JobOffer::class)) }
        }


        @Test
        fun updateJobOfferStatus_professionalNotFound() {
            mockJobOffer.status = OfferStatus.SELECTION_PHASE

            assertThrows<EntityNotFoundException> {
                service.updateJobOfferStatus(mockJobOffer.offerId, JobOfferUpdateDTO(OfferStatus.CANDIDATE_PROPOSAL, mockProfessional.professionalId + 1))
            }

            verify(exactly = 0) { jobOfferRepository.save(any(JobOffer::class)) }
        }

    }
}