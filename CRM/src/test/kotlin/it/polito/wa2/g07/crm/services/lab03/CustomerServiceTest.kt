package it.polito.wa2.g07.crm.services.lab03

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.project.KeycloakUserService
import org.junit.jupiter.api.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class CustomerServiceTest {
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

    private val mockCustomer:Customer = Customer(
        mockContact,
        "mock Customer"
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
        mockCustomer.customerId= 4L
        mockContact2.contactId=5L
    }

    init {
        initMockContact()
    }

    private val contactRepository= mockk<ContactRepository>()
    private val customerRepository= mockk<CustomerRepository>()
    private val contactService = mockk<ContactService>()
    private val keycloakUserService = mockk<KeycloakUserService>()
    private val service = CustomerServiceImpl(customerRepository,contactRepository,contactService,keycloakUserService)

     @Nested
     inner class CreateCustomerTests{

         private val customerSlot = slot<Customer>()


         @BeforeEach
         fun initMocks(){
             every { customerRepository.save(capture(customerSlot)) } answers {firstArg<Customer>()}
             every { customerRepository.delete(any(Customer::class))} returns Unit
             every { contactService.create(any(CreateContactDTO::class)) } answers {
                 val contact = firstArg<CreateContactDTO>().toEntity()
                 every { contactRepository.findById(contact.contactId) } returns Optional.of(contact)
                 contact.toContactDto()
             }
             every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
         }

         @Test
         fun createCustomer(){
             val createCustomerDTO= CreateCustomerDTO(
                 CreateContactDTO(
                     "Luigi",
                     "Verdi",
                     ContactCategory.CUSTOMER.name,
                     null,
                     listOf()
                 ),
                 "New Customer"
             )

             val result = service.createCustomer(createCustomerDTO)

             val expectedDTO = CustomerDTO(
                 0L,
                 ContactDTO(
                     0L,
                     createCustomerDTO.contact.name!!,
                     createCustomerDTO.contact.surname!!,
                     ContactCategory.CUSTOMER,
                     listOf(),
                     createCustomerDTO.contact.ssn
                 ),
                 "New Customer"
             )
             Assertions.assertEquals(result, expectedDTO)
             verify { customerRepository.save(any(Customer::class)) }
             verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
         }

         @Test
         fun createCustomer_noNotes(){
             val createCustomerDTO= CreateCustomerDTO(
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
                 null
             )

             val result = service.createCustomer(createCustomerDTO)

             val expectedAddresses = listOf(
                 EmailResponseDTO(0L, "luigi.verdi@example.org"),
                 TelephoneResponseDTO(0L, "34798989898"),
                 DwellingResponseDTO(0L, "Via Roma, 19", "Torino", "TO", "IT")
             )

             val expectedDTO = CustomerDTO(
                 0L,
                 ContactDTO(
                     0L,
                     createCustomerDTO.contact.name!!,
                     createCustomerDTO.contact.surname!!,
                     ContactCategory.CUSTOMER,
                     expectedAddresses,
                     createCustomerDTO.contact.ssn
                 ),
                 null
             )
             Assertions.assertEquals(result, expectedDTO)
             verify { customerRepository.save(any(Customer::class)) }
             verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
         }

         @Test
         fun createNoCustomerCategory(){
             val createCustomerDTO= CreateCustomerDTO(
                 CreateContactDTO(
                     "Luigi",
                     "Verdi",
                     ContactCategory.PROFESSIONAL.name,
                     null,
                     listOf(
                         EmailDTO("luigi.verdi@example.org"),
                         TelephoneDTO("34798989898"),
                         DwellingDTO("Via Roma, 19", "Torino", "TO", "IT")
                     )
                 ),
                 null
             )

             assertThrows<InvalidParamsException> {
                 service.createCustomer(createCustomerDTO)
             }
             verify(exactly = 0) { customerRepository.save(any(Customer::class)) }
             verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
         }
     }

    @Nested
    inner class AssociateContactToCustomer(){
        private val customerSlot = slot<Customer>()
        private val contactSlot = slot<Contact>()
        private val usedContactIds = HashSet<Long>()
        @BeforeEach
        fun initMocks(){
            every { keycloakUserService.setUserAsCustomer(any(String::class)) } returns Unit
            every { customerRepository.save(capture(customerSlot)) } answers {firstArg<Customer>()}
            every {customerRepository.delete(any(Customer::class))} returns Unit
            every { contactRepository.findById(any(Long::class)) } returns Optional.empty()
            every {contactRepository.findById(mockContact.contactId)} returns Optional.of(mockContact)
            every {contactRepository.findById(mockContact2.contactId)} returns Optional.of(mockContact2)
            every {customerRepository.findByContactInfo(any(Contact::class))} answers {
                val contact:Contact = firstArg<Contact>()
                if (usedContactIds.contains(contact.contactId)){
                    Optional.of(mockCustomer)
                }else {
                    Optional.empty()
                }
            }
        }

        @Test
        fun associateValidContact(){

            val result = service.bindContactToCustomer(mockContact.contactId,"New Customer")

            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )
            val expectedDTO = CustomerDTO(
                0L,
                    ContactDTO(
                        mockContact.contactId,
                        mockContact.name,
                        mockContact.surname,
                        mockContact.category,
                        expectedAddresses,
                        mockContact.ssn
                    ),
                "New Customer"
            )

            Assertions.assertEquals(result, expectedDTO)
            verify { customerRepository.save((any(Customer::class))) }
            verify (exactly = 0) {customerRepository.delete(any(Customer::class))}
        }

        @Test
        fun associateUnknownContact(){

            assertThrows<EntityNotFoundException> {
                service.bindContactToCustomer(20L,"New Customer")
            }
            verify(exactly = 0) { customerRepository.save(any(Customer::class)) }
            verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
        }

        @Test
        fun associateAlreadyConnectedContact(){
            val result = service.bindContactToCustomer(mockContact.contactId,"New Customer")

            val expectedAddresses = listOf(
                EmailResponseDTO(mockMail.id,mockMail.email),
                TelephoneResponseDTO(mockTelephone.id,mockTelephone.number),
                DwellingResponseDTO(mockDwelling.id,mockDwelling.street,mockDwelling.city,mockDwelling.district, mockDwelling.country)
            )
            val expectedDTO = CustomerDTO(
                0L,
                ContactDTO(
                    mockContact.contactId,
                    mockContact.name,
                    mockContact.surname,
                    mockContact.category,
                    expectedAddresses,
                    mockContact.ssn
                ),
                "New Customer"
            )

            Assertions.assertEquals(result, expectedDTO)
            verify { customerRepository.save((any(Customer::class))) }
            verify (exactly = 0) {customerRepository.delete(any(Customer::class))}

            usedContactIds.add(mockContact.contactId)

            assertThrows<ContactAssociationException> {
                service.bindContactToCustomer(mockContact.contactId,"New Customer")
            }
            //verify(exactly = 0) { customerRepository.save(any(Customer::class)) }
            //verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
        }

        @Test
        fun associateProfessionalContact(){

            assertThrows<InvalidParamsException> {
                service.bindContactToCustomer(mockContact2.contactId,"New Customer")
            }
            verify(exactly = 0) { customerRepository.save(any(Customer::class)) }
            verify(exactly = 0) { customerRepository.delete(any(Customer::class)) }
        }
    }

    @Nested
    inner class GetCustomerTests{
        private val pageImpl = PageImpl(listOf(mockCustomer))
        private val pageReq = PageRequest.of(1, 10)

        @BeforeEach
        fun initMocks(){
            every {customerRepository.findAll(any(Pageable::class)) } returns pageImpl
            every {customerRepository.findById(any(Long::class))} returns Optional.empty()
            every { customerRepository.findById(mockCustomer.customerId) } returns Optional.of(mockCustomer)
            every { customerRepository.findByContactIds(any(), any(Pageable::class)) } returns PageImpl(listOf())
            every { customerRepository.findByContactIds(match { it.contains(mockCustomer.contactInfo.contactId) }, any(Pageable::class)) } returns PageImpl(listOf(mockCustomer))
        }

        @Test
        fun getCustomers() {

            val result = service.getCustomers(pageReq)

            val expectedResult = pageImpl.map { it.toReduceCustomerDTO() }
            verify(exactly = 1) { customerRepository.findAll( pageReq) }
            Assertions.assertEquals(result, expectedResult)
        }

        @Test
        fun getCustomersById() {

            val result = service.getCustomerById(mockCustomer.customerId)

            val expectedResult = mockCustomer.toCustomerDto()
            verify(exactly = 1) { customerRepository.findById( mockCustomer.customerId) }
            Assertions.assertEquals(result, expectedResult)
        }

        @Test
        fun getNonExistentCustomer() {
            assertThrows<EntityNotFoundException> {  service.getCustomerById(220L) }
        }

        @Test
        fun getCustomersByContactIds() {
            val result = service.getCustomersByContactIds(listOf(mockCustomer.contactInfo.contactId), PageRequest.of(0, 10))
            assert(result.contains(mockCustomer.toReduceCustomerDTO_Basic()))
        }

        @Test
        fun getCustomersByContactIds_nonexistent() {
            val result = service.getCustomersByContactIds(listOf(mockCustomer.contactInfo.contactId + 1), PageRequest.of(0, 10))
            assert(result.isEmpty)
        }
    }

    @Nested

    inner class PutCustomers{
        private val customerSlot = slot<Customer>()


        @BeforeEach
        fun initMocks(){
            every { customerRepository.save(capture(customerSlot)) } answers {firstArg<Customer>()}
            every {customerRepository.delete(any(Customer::class))} returns Unit
            every {customerRepository.findById(any(Long::class))} returns Optional.empty()
            every { customerRepository.findById(mockCustomer.customerId) } returns Optional.of(mockCustomer)
        }

        @Test
        fun updateNotes(){
            val notes = "Updated notes"

            val result = service.postCustomerNotes(mockCustomer.customerId,notes)

            val expected = CustomerDTO(
                mockCustomer.customerId,
                mockCustomer.contactInfo.toContactDto(),
                notes
            )

            Assertions.assertEquals(result,expected)
            verify (exactly = 1){ customerRepository.save(any(Customer::class)) }
        }

        @Test
        fun updateUnknownCustomer(){
            val notes = "Updated notes"

            assertThrows<EntityNotFoundException> { service.postCustomerNotes(200L,notes) }
            verify (exactly = 0){ customerRepository.save(any(Customer::class))  }
        }
    }
}