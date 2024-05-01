package it.polito.wa2.g07.crm.services



import io.mockk.*
import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.exceptions.DuplicateAddressException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.AddressRepository
import it.polito.wa2.g07.crm.repositories.ContactRepository
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

class MessageServiceTest {

    private val mockContact: Contact = Contact(
        "Mario",
        "Rossi",
        ContactCategory.CUSTOMER,
        "RSSMRA70A01L219K"
    )
    private val mockMail = Email("mario.rossi@example.org")
    private val mockTelephone = Telephone("34242424242")
    private val mockDwelling = Dwelling("Via Roma, 18", "Torino", "TO", "IT")

    private val mockMessage: Message =Message("Titolo","Corpo del messaggio",mockMail,0, LocalDateTime.of(2024,1,12,5,22,3,2))
    private val mockEvent : MessageEvent = MessageEvent(mockMessage,MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2),"commento")
    fun initMockMessage(){
        mockMail.id = 1L
        mockMail.email = "mario.rossi@example.org"
        mockMail.contacts.add(mockContact)
        mockContact.addresses = mutableSetOf(
            mockMail
        )
        mockMessage.messageID=1L
        mockMessage.subject="Titolo"
        mockMessage.body="Corpo del messaggio"
        mockMessage.creationTimestamp=LocalDateTime.of(2024,1,12,5,22,3,2)
        mockMessage.events= mutableSetOf(mockEvent)
    }


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
    }

    init {
       // initMockContact()
        initMockMessage()
    }

    private val contactRepository = mockk<ContactRepository>()
    private val addressRepository = mockk<AddressRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val service = MessageServiceImpl(messageRepository, addressRepository,contactRepository)

    @Nested
    inner class GetMessageTests {

        private val pageImpl = PageImpl(listOf(mockMessage))
        private val pageReq = PageRequest.of(0, 10)

        @BeforeEach
        fun initMocks() {

            every { messageRepository.findById(mockMessage.messageID) } returns Optional.of(mockMessage)
            every { messageRepository.findAll(any(Pageable::class)) } returns pageImpl
            every { messageRepository.findAllByStatus(any(),any(Pageable::class)) } returns pageImpl
        }


        @Test
        fun getMessage_success() {
            val result = service.getMessages(filterBy=null, pageable = pageReq)
            val expectedResult = pageImpl.map { it.toReducedDTO() }
            verify(exactly = 1) { messageRepository.findAll(pageReq) }
            assertEquals(result, expectedResult)
        }


        @Test
        fun getMessageFiltered_success() {
            val result = service.getMessages(filterBy= listOf(MessageStatus.RECEIVED), pageable = pageReq)
            val expectedResult = pageImpl.map { it.toReducedDTO() }
            verify(exactly = 1) { messageRepository.findAllByStatus(any(),any()) }
            assertEquals(result, expectedResult)
        }

        @Test
        fun getMessageFiltered_empty(){
            fun getMessageFiltered_success() {
                val result = service.getMessages(filterBy= listOf(MessageStatus.DISCARDED), pageable = pageReq)
                verify(exactly = 1) { messageRepository.findAllByStatus(any(),any()) }
                assertEquals(result.totalElements, 0)
            }

        }



    }

    @Nested
    inner class CreateContactTests {

        private val contactSlot = slot<Contact>()
        private val addressSlot = slot<Address>()
        private val messageSlot = slot<Message>()

        @BeforeEach
        fun initMocks() {

            every { messageRepository.save(capture(messageSlot)) } answers { firstArg<Message>() }
            every { contactRepository.save(capture(contactSlot)) } answers { firstArg<Contact>() }
            every { addressRepository.save(capture(addressSlot)) } answers { firstArg<Address>() }
            every { addressRepository.findMailAddressByMail(mockMail.email) } returns Optional.of(mockMail)
            every {
                addressRepository.findDwellingAddressByStreet(
                    mockDwelling.street,
                    mockDwelling.city,
                    mockDwelling.district,
                    mockDwelling.country
                )
            } returns Optional.of(mockDwelling)

            every { addressRepository.findMailAddressByMail(any(String::class)) } returns Optional.empty()
            every { addressRepository.findMailAddressByMail(mockMail.email) } returns Optional.of(mockMail)
            every { addressRepository.findTelephoneAddressByTelephoneNumber(any(String::class)) } returns Optional.empty()
            every { addressRepository.findTelephoneAddressByTelephoneNumber(mockTelephone.number) } returns Optional.of(mockTelephone)
            every {
                addressRepository.findDwellingAddressByStreet(
                    any(String::class),
                    any(String::class),
                    any(String::class),
                    any(String::class)
                )
            } returns Optional.empty()



        }

        @Test
        fun createEmailMessage() {
            val msg = MessageCreateDTO(
                mockMail.toAddressDTO(), "email", "a", "b"
            )
            val result = service.createMessage(msg)
            result!!.creationTimestamp =LocalDateTime.of(2024,1,12,5,22,3,2)
            result.lastEvent.timestamp=LocalDateTime.of(2024,1,12,5,22,3,2)
            val expectedDTO = MessageDTO(
                0L,
                (msg.sender as EmailDTO),
                AddressType.EMAIL,
                msg.subject,
                msg.body,
                0,
                LocalDateTime.of(2024,1,12,5,22,3,2),
                MessageEventDTO(MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2))
            )

            assertEquals(result, expectedDTO)
            verify (exactly = 0){ contactRepository.save(any(Contact::class)) }
            verify (exactly = 1){ addressRepository.findMailAddressByMail(any()) }
            verify (exactly = 1){ messageRepository.save(any()) }
            verify (exactly = 0){ addressRepository.save(any()) }

        }


    }


    }