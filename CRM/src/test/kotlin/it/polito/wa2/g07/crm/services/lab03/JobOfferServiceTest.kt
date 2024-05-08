package it.polito.wa2.g07.crm.services.lab03

import io.mockk.*

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferCreateDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.ReducedCustomerDTO
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException

import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.JobOfferRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class JobOfferServiceTest {

    private val customerRepository= mockk<CustomerRepository>()
    private val jobOfferRepository= mockk<JobOfferRepository>()
    private val service = JobOfferServiceImpl(jobOfferRepository,customerRepository)
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
                customer = ReducedCustomerDTO(0L, ReducedContactDTO(0L,"Mario","Rossi",ContactCategory.CUSTOMER),null),
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
}