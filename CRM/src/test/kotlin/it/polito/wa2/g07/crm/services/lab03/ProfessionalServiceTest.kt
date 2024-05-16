package it.polito.wa2.g07.crm.services.lab03

import io.mockk.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.EmploymentState
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*
import kotlin.collections.HashSet

class ProfessionalServiceTest {

    private val mockContact: Contact= Contact(
        "Mario",
        "Rossi",
        ContactCategory.PROFESSIONAL,
        "RSSMRA70A01L219K"
    )

    private val mockContact2: Contact = Contact(
        "Laura",
        "Binchi",
        ContactCategory.CUSTOMER,
        "LLBB0088LL4657K"
    )

    private val mockProfessional: Professional= Professional(
        mockContact,
        "Torino",
        setOf("mockSkill1","mockSkill2"),
        100.0,
        EmploymentState.UNEMPLOYED,
        "mockNotes"
    )



    private val mockMail = Email("mario.rossi@example.org")
    private val mockTelephone = Telephone("34242424242")
    private val mockDwelling = Dwelling("Via Roma, 18", "Torino", "TO", "IT")

    fun initMockContact() {
        mockMail.id = 1L
        mockMail.email = "mario.rossi@example.org"
        mockMail.contacts.add(mockContact)

        mockTelephone.id = 2L
        mockTelephone.number = "34242424242"
        mockTelephone.contacts.add(mockContact)

        mockDwelling.id = 3L
        mockDwelling.street = "Via Roma, 18"
        mockDwelling.city = "Torino"
        mockDwelling.district = "TO"
        mockDwelling.country = "IT"
        mockDwelling.contacts.add(mockContact)

        mockContact.addresses = mutableSetOf(
            mockMail,
            mockTelephone,
            mockDwelling
        )

        mockContact.contactId = 1L
        mockProfessional.professionalId= 4L
        mockContact2.contactId=5L
    }

    init {
        initMockContact()
    }
    val professionalRepository = mockk<ProfessionalRepository>()
    val contactRepository = mockk<ContactRepository>()

    val service = ProfessionalServiceImpl(professionalRepository,contactRepository)
    @Nested
    inner class CreateProfessionalTests{
        private val professionalSlot = slot<Professional>()

        @BeforeEach
        fun initMocks(){
            every { professionalRepository.save(capture(professionalSlot)) } answers {firstArg<Professional>()}
            every {professionalRepository.delete(any(Professional::class))} returns Unit
        }

        @Test
        fun createProfessional(){
            val createProfessionalDTO= CreateProfessionalDTO(
                CreateContactDTO(
                    "Luigi",
                    "Verdi",
                    ContactCategory.PROFESSIONAL.name,
                    null,
                    listOf()
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                "mockNotes"
            )

            val result = service.createProfessional(createProfessionalDTO)

            val expectedDTO = ProfessionalDTO(
                0L,
                ContactDTO(
                    0L,
                    createProfessionalDTO.contactInfo.name!!,
                    createProfessionalDTO.contactInfo.surname!!,
                    ContactCategory.PROFESSIONAL,
                    listOf(),
                    createProfessionalDTO.contactInfo.ssn
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                "mockNotes"
            )
            Assertions.assertEquals(result,expectedDTO)
            verify { professionalRepository.save(any(Professional::class)) }
            verify (exactly=0) {professionalRepository.delete(any(Professional::class))}
        }

        @Test
        fun createProfessional_noNotes(){
            val createProfessionalDTO= CreateProfessionalDTO(
                CreateContactDTO(
                    "Luigi",
                    "Verdi",
                    ContactCategory.PROFESSIONAL.name,
                    null,
                    listOf()
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                null
            )

            val result = service.createProfessional(createProfessionalDTO)

            val expectedDTO = ProfessionalDTO(
                0L,
                ContactDTO(
                    0L,
                    createProfessionalDTO.contactInfo.name!!,
                    createProfessionalDTO.contactInfo.surname!!,
                    ContactCategory.PROFESSIONAL,
                    listOf(),
                    createProfessionalDTO.contactInfo.ssn
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                null
            )
            Assertions.assertEquals(result,expectedDTO)
            verify { professionalRepository.save(any(Professional::class)) }
            verify (exactly=0) {professionalRepository.delete(any(Professional::class))}
        }

        @Test
        fun createProfessionalWrongCategory(){

            val createProfessionalDTO= CreateProfessionalDTO(
                CreateContactDTO(
                    "Luigi",
                    "Verdi",
                    ContactCategory.CUSTOMER.name,
                    null,
                    listOf(
                        EmailDTO("luigi.verdi@example.org"),
                        TelephoneDTO("34798989898"),
                        DwellingDTO("Via Roma, 19", "Torino", "TO", "IT")
                    )
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                null
            )
            assertThrows<InvalidParamsException> {
                service.createProfessional(createProfessionalDTO)
            }
            verify(exactly = 0) {professionalRepository.save(any(Professional::class))}
            verify (exactly =0) {professionalRepository.delete(any(Professional::class))}

        }

    }

    @Nested
    inner class AssociateContactToProfessional(){
        private val professionalSlot = slot<Professional>()
        private val contactSlot = slot <Contact>()
        private val usedContactIds = HashSet<Long>()

        @BeforeEach
        fun initMocks(){
            every {professionalRepository.save(capture(professionalSlot))} answers {firstArg<Professional>()}
            every {professionalRepository.delete(any(Professional::class))} returns Unit
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every {contactRepository.findById(mockContact.contactId)} returns Optional.of(mockContact)
            every {contactRepository.findById(mockContact2.contactId)} returns Optional.of(mockContact2)
            every { professionalRepository.findByContactInfo(any(Contact::class)) } answers {
                val contact:Contact = firstArg<Contact>()
                if (usedContactIds.contains(contact.contactId)){
                    Optional.of(mockProfessional)
                }else {
                    Optional.empty()
                }
            }
        }

        @Test
        fun associateValidContact(){
            val result = service.bindContactToProfessional(mockContact.contactId,"Torino",setOf("mockSkill1","mockSkill2"),100.0,EmploymentState.UNEMPLOYED,"New Professional")
            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )
            val expectedDTO = ProfessionalDTO(
                0L,
                ContactDTO(
                    mockContact.contactId,
                    mockContact.name,
                    mockContact.surname,
                    mockContact.category,
                    expectedAddresses,
                    mockContact.ssn
                ),
                "Torino",
                setOf("mockSkill1","mockSkill2"),
                100.0,
                EmploymentState.UNEMPLOYED,
                "New Professional"

            )
            Assertions.assertEquals(result,expectedDTO)
            verify { professionalRepository.save((any(Professional::class))) }
            verify (exactly = 0) {professionalRepository.delete(any(Professional::class))}

        }
        @Test
        fun associateUnknownContact(){
            assertThrows<EntityNotFoundException> {
                service.bindContactToProfessional(50L,"Torino", setOf("mock1","mock2"),100.0,EmploymentState.UNEMPLOYED,"Test Notes")
            }
            verify(exactly = 0) {professionalRepository.save(any(Professional::class))}
            verify(exactly = 0) {professionalRepository.delete(any(Professional::class))}
        }
        @Test
        fun associateAlreadyConnectedContact(){
            val result = service.bindContactToProfessional(mockContact.contactId,"Torino", setOf("mock1","mock2"),100.0,EmploymentState.UNEMPLOYED,"Test Notes")

            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )

            val expectedDTO = ProfessionalDTO (
                0L,
                ContactDTO(
                    mockContact.contactId,
                    mockContact.name,
                    mockContact.surname,
                    mockContact.category,
                    expectedAddresses,
                    mockContact.ssn
                ),
                "Torino",
                setOf("mock1","mock2"),
                100.0,EmploymentState.UNEMPLOYED,
                "Test Notes"

            )
            Assertions.assertEquals(result,expectedDTO)
            verify {professionalRepository.save((any(Professional::class)))}
            verify (exactly = 0) {professionalRepository.delete(any(Professional::class))}

            usedContactIds.add(mockContact.contactId)

            assertThrows<ContactAssociationException> {
                service.bindContactToProfessional(mockContact.contactId,"Torino", setOf("mock1","mock2"),100.0,EmploymentState.UNEMPLOYED,"Test Notes")
            }

        }

        @Test
        fun associateCustomerContact(){

            assertThrows<InvalidParamsException> {
                service.bindContactToProfessional(mockContact2.contactId,"Torino", setOf("mock1","mock2"),100.0,EmploymentState.UNEMPLOYED,"Test Notes")
            }
            verify(exactly = 0) { professionalRepository.save(any(Professional::class)) }
            verify(exactly = 0) { professionalRepository.delete(any(Professional::class)) }
        }
    }
    val professionalRepository = mockk<ProfessionalRepository>()
    val contactRepository = mockk<ContactRepository>()
    val service = ProfessionalServiceImpl(professionalRepository, contactRepository )


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

        private var pageImpl: PageImpl<Professional>

        init {
            professional.professionalId = 1L
            professional.contactInfo.contactId = 1L
            this.pageImpl = PageImpl(listOf(professional))
        }

        @BeforeEach
        fun initMocks() {
            every { professionalRepository.findAll(any(), any(Pageable::class)) } returns PageImpl(listOf(professional))
            every { professionalRepository.findById(any(Long::class)) } returns Optional.empty()
            every { professionalRepository.findById(professional.professionalId) } returns Optional.of(professional)
        }

        @Test
        fun searchProfessionals_success() {
            val result = service.searchProfessionals(ProfessionalFilterDTO(location = "Torino"), PageRequest.of(0, 10))
            assertEquals(result, pageImpl.map { it.toProfessionalReducedDto() })
        }

        @Test
        fun getProfessionalById_found() {
            val result = service.getProfessionalById(professional.professionalId)
            assertEquals(result, professional.toProfessionalDto())
        }

        @Test
        fun getProfessionalById_notFound() {
            assertThrows<EntityNotFoundException> {
                service.getProfessionalById(professional.professionalId + 1)
            }
        }

    }



    @Nested
    inner class AssociateContactToProfessional(){
        private val customerSlot = slot<Customer>()
        private val contactSlot = slot<Contact>()
        private val professionalSlot = slot<Professional>()
        private val usedContactIds = HashSet<Long>()

        private val mockContact: Contact = Contact(
            "Mario",
            "Rossi",
            ContactCategory.CUSTOMER,
            "RSSMRA70A01L219K"
        )

        private val mockContact2: Contact = Contact(
            "Laura",
            "Binchi",
            ContactCategory.PROFESSIONAL,
            "LLBB0088LL4657K"
        )

        private val mockProfessional:Professional = Professional(
            mockContact2,
            "TO", setOf("Ita"),20.0,EmploymentState.UNEMPLOYED,null
        )

        private val mockMail = Email("mario.rossi@example.org")
        private val mockTelephone = Telephone("34242424242")
        private val mockDwelling = Dwelling("Via Roma, 18", "Torino", "TO", "IT")

        fun initMockContact() {
            mockMail.id = 1L
            mockMail.email = "mario.rossi@example.org"
            mockMail.contacts.add(mockContact)

            mockTelephone.id = 2L
            mockTelephone.number = "34242424242"
            mockTelephone.contacts.add(mockContact)

            mockDwelling.id = 3L
            mockDwelling.street = "Via Roma, 18"
            mockDwelling.city = "Torino"
            mockDwelling.district = "TO"
            mockDwelling.country = "IT"
            mockDwelling.contacts.add(mockContact)

            mockContact.addresses = mutableSetOf(
                mockMail,
                mockTelephone,
                mockDwelling
            )
            mockContact2.addresses = mutableSetOf(
                mockMail,
                mockTelephone,
                mockDwelling
            )

            mockContact.contactId = 1L
            mockProfessional.professionalId= 4L
            mockContact2.contactId=5L
        }

        init {
            initMockContact()
        }


        @BeforeEach
        fun initMocks(){
            every { professionalRepository.save(capture(professionalSlot)) } answers {firstArg<Professional>()}
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every {contactRepository.findById(mockContact.contactId)} returns Optional.of(mockContact)
            every {contactRepository.findById(mockContact2.contactId)} returns Optional.of(mockContact2)
            every {professionalRepository.findByContactInfo(any(Contact::class))} answers {
                val contact:Contact = firstArg<Contact>()
                if (usedContactIds.contains(contact.contactId)){
                    Optional.of(mockProfessional)
                }else {
                    Optional.empty()
                }
            }
        }

        @Test
        fun associateValidContact(){
            val createProfessionalReducedDTO = CreateProfessionalReducedDTO(  "TO", setOf("Ita"),20.0,null)

            val result = service.bindContactToProfessional(mockContact2.contactId,createProfessionalReducedDTO)

            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )
            val expectedDTO = ProfessionalDTO(
                0L,
                ContactDTO(
                    mockContact2.contactId,
                    mockContact2.name,
                    mockContact2.surname,
                    mockContact2.category,
                    expectedAddresses,
                    mockContact2.ssn
                ),
                "TO", setOf("Ita"),EmploymentState.UNEMPLOYED,20.0,null
            )

            Assertions.assertEquals( expectedDTO,result)
            verify { professionalRepository.save((any(Professional::class))) }

        }

        @Test
        fun associateUnknownContact(){
            val createProfessionalReducedDTO = CreateProfessionalReducedDTO(  "TO", setOf("Ita"),20.0,null)

            assertThrows<EntityNotFoundException> {
                service.bindContactToProfessional(20L,createProfessionalReducedDTO)
            }
            verify(exactly = 0) { professionalRepository.save(any(Professional::class)) }
        }

        @Test
        fun associateAlreadyConnectedContact(){
            val createProfessionalReducedDTO = CreateProfessionalReducedDTO(  "TO", setOf("Ita"),20.0,null)

            val result = service.bindContactToProfessional(mockContact2.contactId,createProfessionalReducedDTO)

            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )

            verify { professionalRepository.save((any(Professional::class))) }


            usedContactIds.add(mockContact2.contactId)

            assertThrows<ContactAssociationException> {
                service.bindContactToProfessional(mockContact2.contactId,createProfessionalReducedDTO)

            }
            //verify(exactly = 0) { customerRepository.save(any(Customer::class)) }
            //verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
        }

        @Test
        fun associateProfessionalContact(){
            val createProfessionalReducedDTO = CreateProfessionalReducedDTO(  "TO", setOf("Ita"),20.0,null)

            assertThrows<InvalidParamsException> {
                service.bindContactToProfessional(mockContact.contactId,createProfessionalReducedDTO)
            }
            verify(exactly = 0) { professionalRepository.save(any(Professional::class)) }

        }
    }
}