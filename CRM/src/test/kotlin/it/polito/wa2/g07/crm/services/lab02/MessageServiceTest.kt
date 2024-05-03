package it.polito.wa2.g07.crm.services.lab02



import io.mockk.*
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.exceptions.MessageNotFoundException
import it.polito.wa2.g07.crm.repositories.lab02.AddressRepository
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab02.MessageRepository
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

    private val mockMessage: Message =
        Message("Titolo","Corpo del messaggio",mockMail, MessageChannel.EMAIL, 0, LocalDateTime.of(2024,1,12,5,22,3,2))
    private val mockEvent : MessageEvent = MessageEvent(mockMessage,
        MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2),"commento")
    private val mockEvent2 : MessageEvent = MessageEvent(mockMessage,
        MessageStatus.READ,LocalDateTime.of(2025,1,12,5,22,3,2),"commento")


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
        mockMessage.events= mutableSetOf(mockEvent,mockEvent2)
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
    inner class CreateMessageTests {

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
                MessageChannel.EMAIL,
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
        @Test
        fun createEmailMessage_newContact() {
            val msg = MessageCreateDTO(
                 EmailDTO("completly.new@gmail.com"), "email", "a", "b"
            )
            val result = service.createMessage(msg)
            result!!.creationTimestamp =LocalDateTime.of(2024,1,12,5,22,3,2)
            result.lastEvent.timestamp=LocalDateTime.of(2024,1,12,5,22,3,2)
            val expectedDTO = MessageDTO(
                0L,
                (msg.sender as EmailDTO),
                MessageChannel.EMAIL,
                msg.subject,
                msg.body,
                0,
                LocalDateTime.of(2024,1,12,5,22,3,2),
                MessageEventDTO(MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2))
            )

            assertEquals(result, expectedDTO)
            verify (exactly = 1){ contactRepository.save(any()) }
            verify (exactly = 1){ addressRepository.findMailAddressByMail(any()) }
            verify (exactly = 1){ messageRepository.save(any()) }
            verify (exactly = 1){ addressRepository.save(any()) }

        }
        @Test
        fun createTelephoneMessage_newContact() {
            val msg = MessageCreateDTO(
                TelephoneDTO("333"), "phone_call", "a", "b"
            )
            val result = service.createMessage(msg)
            result!!.creationTimestamp =LocalDateTime.of(2024,1,12,5,22,3,2)
            result.lastEvent.timestamp=LocalDateTime.of(2024,1,12,5,22,3,2)
            val expectedDTO = MessageDTO(
                0L,
                (msg.sender as TelephoneDTO),
                MessageChannel.PHONE_CALL,
                msg.subject,
                msg.body,
                0,
                LocalDateTime.of(2024,1,12,5,22,3,2),
                MessageEventDTO(MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2))
            )

            assertEquals(result, expectedDTO)
            verify (exactly = 1){ contactRepository.save(any()) }
            verify (exactly = 1){ addressRepository.findTelephoneAddressByTelephoneNumber(any()) }
            verify (exactly = 1){ messageRepository.save(any()) }
            verify (exactly = 1){ addressRepository.save(any()) }

        }
        @Test
        fun createDwellingMessage_newContact() {
            val msg = MessageCreateDTO(
                DwellingDTO("c","b","c","d"), "postal_mail", "a", "b"
            )
            val result = service.createMessage(msg)
            result!!.creationTimestamp =LocalDateTime.of(2024,1,12,5,22,3,2)
            result.lastEvent.timestamp=LocalDateTime.of(2024,1,12,5,22,3,2)
            val expectedDTO = MessageDTO(
                0L,
                (msg.sender as DwellingDTO),
                MessageChannel.POSTAL_MAIL,
                msg.subject,
                msg.body,
                0,
                LocalDateTime.of(2024,1,12,5,22,3,2),
                MessageEventDTO(MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2))
            )

            assertEquals(result, expectedDTO)
            verify (exactly = 1){ contactRepository.save(any()) }
            verify (exactly = 1){ addressRepository.findDwellingAddressByStreet(any(),any(),any(),any()) }
            verify (exactly = 1){ messageRepository.save(any()) }
            verify (exactly = 1){ addressRepository.save(any()) }

        }


    }

    @Nested
    inner class UpdateStatus{
        //private val mockEvent : MessageEvent = MessageEvent(mockMessage,MessageStatus.RECEIVED,LocalDateTime.of(2024,1,12,5,22,3,2),"commento")
        private val messageSlot = slot<Message>()
        @BeforeEach
        fun initMocks() {
            every { messageRepository.save(capture(messageSlot)) } answers { firstArg<Message>() }
            every { messageRepository.findById(any()) } returns Optional.of(mockMessage)
            every { messageRepository.getLastEventByMessageId(any()) } returns mockEvent2
        }

        @Test
        fun addNewState(){
            val time = LocalDateTime.of(2026,1,12,5,22,3,2)
            val newStatus = MessageEventDTO(MessageStatus.PROCESSING,time,"commento")
            val result = service.updateStatus(mockMessage.messageID,newStatus)
            assertEquals(result,newStatus)
            verify(exactly = 1) { messageRepository.findById(any()) }
            //verify(exactly = 1) { messageRepository.save(any()) }

        }
        @Test
        fun addNewStateWrong(){
            val time = LocalDateTime.of(2024,1,12,5,22,3,2)
            val newStatus = MessageEventDTO(MessageStatus.RECEIVED,time,"commento")
            assertThrows<InvalidParamsException> {
                val result = service.updateStatus(mockMessage.messageID,newStatus)
            }
            verify(exactly = 1) { messageRepository.findById(any()) }
            verify { messageRepository.save(any()) wasNot called }
        }
    }

    @Nested
    inner class History{

        private val pageImpl = PageImpl(mockMessage.events.toList())
        private val pageReq = PageRequest.of(0, 10)
        private val messageSlot = slot<Message>()
        @BeforeEach
        fun initMocks() {
            every { messageRepository.save(capture(messageSlot)) } answers { firstArg<Message>() }
            every { messageRepository.findById(any()) } returns Optional.of(mockMessage)
            every { messageRepository.getEventsByMessageID(any(),any()) } returns pageImpl
        }

        @Test
        fun getHistory(){
           val result= service.getHistory(1L,pageReq)
            val expectedResult = pageImpl.map { it.toMessageEventDTO() }
            assertEquals(result,expectedResult)
        }

    }

    @Nested
    inner class changePriority{
        private val pageReq = PageRequest.of(0, 10)
        private val messageSlot = slot<Message>()
        @BeforeEach
        fun initMocks() {
            every { messageRepository.save(capture(messageSlot)) } answers { firstArg<Message>() }
            every { messageRepository.findById(mockMessage.messageID) } returns Optional.of(mockMessage)
            every { messageRepository.findById(1000L) } returns Optional.empty()
        }

        @Test
        fun setPriority(){
            val result= service.changePriority(mockMessage.messageID,10)
            mockMessage.priority=10
            val expected = mockMessage.toMessageDTO()
            assertEquals(result,expected)
        }
        @Test
        fun setPriority_msgNotFound(){

            assertThrows<MessageNotFoundException> {
                val result= service.changePriority(1000L,100)
            }
            verify(exactly = 1) { messageRepository.findById(any()) }
            verify { messageRepository.save(any()) wasNot called }
        }

    }

    }