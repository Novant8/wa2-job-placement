package it.polito.wa2.g07.crm.integrations.lab03

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.g07.crm.CrmApplicationTests
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.dtos.lab03.toProfessionalDto
import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.repositories.lab02.AddressRepository
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository

import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.repositories.lab03.ProfessionalRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //just to remove IDE error on mockMvc
@AutoConfigureMockMvc
class ProfessionalIntegrationTest: CrmApplicationTests() {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var contactRepository: ContactRepository
    @Autowired
    lateinit var customerRepository: CustomerRepository
    @Autowired
    lateinit var professionalRepository: ProfessionalRepository
    @Autowired
    lateinit var jobOfferRepository: ProfessionalRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper



    @Autowired
    lateinit var addressRepository: AddressRepository

    @Nested
    inner class PostProfessionalTest{
        @BeforeEach
        fun init(){
            professionalRepository.deleteAll()
        }

        @Test
        fun postProfessional(){
            val professional="""
                  {
                    "contactInfo": {
                        "name":"Professional1Name",
                        "surname":"Professional1Surname",
                        "category":"PROFESSIONAL",
                        "ssn":"VRDLGU70A01L219G",
                        "addresses": [{
                                "email":"company.test@example.org"
                            },
                            {
                                "phoneNumber":"34242424242"
                            },
                            {
                                "street":"Via Roma, 18",
                                "city":"Torino",
                                "district":"TO","country":"IT"
                             }
                             ]
                        },
                  "location":"Torino",
                  "skills":["PHP","Java","Angular"],
                  "dailyRate":100.0,
                  "employmentState":"UNEMPLOYED",
                  "notes":"notes test"}
            """.trimIndent()
            mockMvc.perform(post("/API/professionals").contentType(MediaType.APPLICATION_JSON).content(professional))
                .andExpect((status().isCreated))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contactInfo.name").value("Professional1Name"))
                .andExpect(jsonPath("$.contactInfo.surname").value("Professional1Surname"))
                .andExpect(jsonPath("$.contactInfo.category").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.contactInfo.ssn").value("VRDLGU70A01L219G"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].email").value("company.test@example.org"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].phoneNumber").value("34242424242"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].street").value("Via Roma, 18"))
                .andExpect( jsonPath("$.contactInfo.addresses[*].district").value("TO"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].city").value("Torino"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].country").value("IT"))
                .andExpect(jsonPath("$.location").value("Torino"))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("PHP"))
                .andExpect(jsonPath("$.skills[2]").value("Angular"))
                .andExpect(jsonPath("$.dailyRate").value(100.0))
                .andExpect(jsonPath("$.employmentState").value("UNEMPLOYED"))
                .andExpect(jsonPath("$.notes").value("notes test"))


        }

        @Test
        fun postProfessional_noNotes(){
            val professional="""
                  {
                    "contactInfo": {
                        "name":"Professional1Name",
                        "surname":"Professional1Surname",
                        "category":"PROFESSIONAL",
                        "ssn":"VRDLGU70A01L219G",
                        "addresses": [{
                                "email":"company.test@example.org"
                            },
                            {
                                "phoneNumber":"34242424242"
                            },
                            {
                                "street":"Via Roma, 18",
                                "city":"Torino",
                                "district":"TO","country":"IT"
                             }
                             ]
                        },
                  "location":"Torino",
                  "skills":["PHP","Java","Angular"],
                  "dailyRate":100.0,
                  "employmentState":"UNEMPLOYED"
                 }
            """.trimIndent()
            mockMvc.perform(post("/API/professionals").contentType(MediaType.APPLICATION_JSON).content(professional))
                .andExpect((status().isCreated))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contactInfo.name").value("Professional1Name"))
                .andExpect(jsonPath("$.contactInfo.surname").value("Professional1Surname"))
                .andExpect(jsonPath("$.contactInfo.category").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.contactInfo.ssn").value("VRDLGU70A01L219G"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].email").value("company.test@example.org"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].phoneNumber").value("34242424242"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].street").value("Via Roma, 18"))
                .andExpect( jsonPath("$.contactInfo.addresses[*].district").value("TO"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].city").value("Torino"))
                .andExpect(jsonPath("$.contactInfo.addresses[*].country").value("IT"))
                .andExpect(jsonPath("$.location").value("Torino"))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("PHP"))
                .andExpect(jsonPath("$.skills[2]").value("Angular"))
                .andExpect(jsonPath("$.dailyRate").value(100.0))
                .andExpect(jsonPath("$.employmentState").value("UNEMPLOYED"))



        }


        @Test
        fun postCustomer(){
            val professional="""
                  {
                    "contactInfo": {
                        "name":"Professional1Name",
                        "surname":"Professional1Surname",
                        "category":"CUSTOMER",
                        "ssn":"VRDLGU70A01L219G",
                        "addresses": [{
                                "email":"company.test@example.org"
                            },
                            {
                                "phoneNumber":"34242424242"
                            },
                            {
                                "street":"Via Roma, 18",
                                "city":"Torino",
                                "district":"TO","country":"IT"
                             }
                             ]
                        },
                  "location":"Torino",
                  "skills":["PHP","Java","Angular"],
                  "dailyRate":100.0,
                  "employmentState":"UNEMPLOYED",
                  "notes":"notes test"}
            """.trimIndent()
            mockMvc.perform(post("/API/professionals").contentType(MediaType.APPLICATION_JSON).content(professional))
                .andExpect((status().isBadRequest))



        }


        @Nested
        inner class PostAssociateContactToProfessional(){
            private var contactId1= 0L
            private var contactId2=0L

            @BeforeEach
            fun init(){
                professionalRepository.deleteAll()
                contactRepository.deleteAll()
                val contactDto1= CreateContactDTO(
                    "Professional",
                    "Test",
                    "professional",
                    "12345678",
                    listOf(
                        TelephoneDTO("12345667889"),
                        EmailDTO("professional.test@email.com"),
                        DwellingDTO("123 Main St", "City", "District","Country")

                    )
                )

                val contactDto2 = CreateContactDTO(
                    "Professional2",
                    "Test2",
                    "customer",
                    "EEEEA23456",
                listOf( TelephoneDTO("00223345"),
                    EmailDTO("different.professional@email.com"),
                    DwellingDTO("Street2", "City2", "District2","Country2"
                    )
                )
                )
                contactId1 = contactRepository.save(contactDto1.toEntity()).contactId
                contactId2= contactRepository.save(contactDto2.toEntity()).contactId



            }
/*
            @Test
            fun associateContact(){
                val body= """
                    {
                    "location":"Torino",
                    "skills":["PHP","Java","Angular"],
                    "dailyRate":100.0,
                    "employmentState":"UNEMPLOYED",
                    "notes":"notes test"}
                    }
                """.trimIndent()
                mockMvc.perform(post("/API/contacts/$contactId1/professionals").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            }*/

        }

        








    }

    @Nested
    inner class GetProfessionalTest {

        private var professional = Professional(
            Contact(
                "Luigi",
                "Verdi",
                ContactCategory.PROFESSIONAL
            ),
            "Torino",
            mutableSetOf("Proficient in Kotlin", "Can work well in a team"),
            0.0
        )

        @BeforeEach
        fun init() {
            jobOfferRepository.deleteAll()
            professionalRepository.deleteAll()

            professional = professionalRepository.save(professional)
        }

        @Test
        fun getProfessionals_noFilter() {
            mockMvc
                .get("/API/professionals").andExpect {
                    status { isOk() }
                    jsonPath("$.content[0].id") { value(professional.professionalId) }
                }
        }

        @Test
        fun getProfessionals_skillsFilter() {
            val validFilters = listOf(
                "kotlin",       // Only skill matches
                "kotlin,team",  // All skills match
                "java,team"     // One skill matches
            )

            for (filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("skills", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content") { isArray(); isNotEmpty() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No skill matches
            mockMvc
                .get("/API/professionals") {
                    queryParam("skills", "other")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content") { isArray(); isEmpty() }
                }
        }

        @Test
        fun getProfessionals_locationFilter() {
            val validFilters = listOf("Torino", "torino", "TORINO")

            // Match
            for(filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("location", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No match
            mockMvc
                .get("/API/professionals") {
                    queryParam("location", "Milano")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content") { isArray(); isEmpty() }
                }
        }

        @Test
        fun getProfessionals_employmentStateFilter() {
            val validFilters = listOf("unemployed", "Unemployed", "UNEMPLOYED")

            // Match
            for(filter in validFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("employmentState", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content[0].id") { value(professional.professionalId) }
                    }
            }

            // No match
            val invalidFilters = listOf("employed", "not_available")
            for (filter in invalidFilters) {
                mockMvc
                    .get("/API/professionals") {
                        queryParam("employmentState", filter)
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.content") { isArray(); isEmpty() }
                    }
            }

            // Invalid filter
            mockMvc
                .get("/API/professionals") {
                    queryParam("employmentState", "i am not valid")
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun getProfessionals_multipleFilters() {
            mockMvc
                .get("/API/professionals") {
                    queryParam("skills", "kotlin")
                    queryParam("location", "Torino")
                    queryParam("employmentState", "unemployed")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.content[0].id") { value(professional.professionalId) }
                }
        }

        @Test
        fun getProfessionalById_exists() {
            mockMvc
                .get("/API/professionals/${professional.professionalId}")
                .andExpect {
                    status { isOk() }
                    content {
                        json(objectMapper.writeValueAsString(professional.toProfessionalDto()))
                    }
                }
        }

        @Test
        fun getProfessionalById_doesNotExist() {
            mockMvc
                .get("/API/professionals/${professional.professionalId+1}")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

    @Nested
    inner class Post_AssociateContactToCustomer(){
        private var contactId1 = 0L
        private var contactId2 = 0L


        @BeforeEach
        fun init(){

            professionalRepository.deleteAll()
            customerRepository.deleteAll()
            contactRepository.deleteAll()
            val contactDto1 = CreateContactDTO(
                "Mario",
                "Rossi",
                "professional",
                "123456",
                listOf(
                    TelephoneDTO("12345667889"),
                    EmailDTO("company.test@email.com"),
                    DwellingDTO("123 Main St", "City", "District","Country")
                )
            )

            val contactDto2 = CreateContactDTO(
                "Maria",
                "Bianchi",
                "customer",
                "9988ABC",
                listOf(
                    TelephoneDTO("00223345"),
                    EmailDTO("different.company@email.com"),
                    DwellingDTO("Street2", "City2", "District2","Country2")
                )
            )
            contactId1 = contactRepository.save(contactDto1.toEntity()).contactId
            contactId2= contactRepository.save(contactDto2.toEntity()).contactId
        }

        @Test
        fun associateContact(){
            val body = """
                 {
                    "notes": "New professional associated" ,
                    "skills": ["Ita","Eng"],
                    "dailyRate": 29,
                    "location": "TO"
                 }
            """.trimIndent()

            mockMvc.perform(post("/API/contacts/$contactId1/professional").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("contactInfo.name").value("Mario"))
                .andExpect(jsonPath("contactInfo.surname").value("Rossi"))
                .andExpect(jsonPath("contactInfo.category").value("PROFESSIONAL"))
                .andExpect(jsonPath("contactInfo.ssn").value("123456"))
                .andExpect(jsonPath("contactInfo.addresses[*].email").value("company.test@email.com"))
                .andExpect(jsonPath("contactInfo.addresses[*].phoneNumber").value("12345667889"))
                .andExpect(jsonPath("contactInfo.addresses[*].street").value("123 Main St"))
                .andExpect(jsonPath("contactInfo.addresses[*].city").value("City"))
                .andExpect(jsonPath("contactInfo.addresses[*].district").value("District"))
                .andExpect(jsonPath("contactInfo.addresses[*].country").value("Country"))
                .andExpect(jsonPath("notes").value("New professional associated"))
                .andExpect(jsonPath("skills").isArray())
                .andExpect(jsonPath("skills[0]").value("Ita"))
                .andExpect(jsonPath("employmentState").value("UNEMPLOYED"))
                .andExpect(jsonPath("dailyRate").value(29.0))
        }

        @Test
        fun associateContactWithInvalidId(){
            val body = """
                  {
                    "notes": "New professional associated" ,
                    "skills": ["Ita","Eng"],
                    "dailyRate": 29,
                    "location": "TO"
                 }
            """.trimIndent()

            mockMvc.perform(post("/API/contacts/20/professional").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound)
        }
        @Test
        fun associateAlreadyConnectedContact(){
            val body = """
                    {
                    "notes": "New professional associated" ,
                    "skills": ["Ita","Eng"],
                    "dailyRate": 29,
                    "location": "TO"
                 }
            """.trimIndent()

            val body2 = """
                     {
                    "notes": "New professional associated" ,
                    "skills": ["Ita","Eng"],
                    "dailyRate": 29,
                    "location": "TO"
                 }
            """.trimIndent()

            mockMvc.perform(post("/API/contacts/$contactId1/professional").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated)

            mockMvc.perform(post("/API/contacts/$contactId1/professional").contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isConflict)
        }
        @Test
        fun associateProfessionalContact(){
            val body = """
                    {
                    "notes": "New professional associated" ,
                    "skills": ["Ita","Eng"],
                    "dailyRate": 29,
                    "location": "TO"
                 }
            """.trimIndent()


            mockMvc.perform(post("/API/contacts/$contactId2/professional").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest)
        }

    }

}