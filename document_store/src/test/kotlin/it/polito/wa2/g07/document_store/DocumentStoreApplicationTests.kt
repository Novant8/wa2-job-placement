package it.polito.wa2.g07.document_store


import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import net.bytebuddy.utility.dispatcher.JavaDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DocumentStoreApplicationTests {
   // @Autowired
   // lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var mvc: MockMvc
    @Autowired
    lateinit var docRepo: DocumentRepository
    @Autowired
    lateinit var docMetadataRepo: DocumentMetadataRepository

    @BeforeEach
    fun initDb() {
        docMetadataRepo.deleteAll()
        docRepo.deleteAll()

    }

    @Test
    fun test_get_documents() {
        mvc.perform(get("/API/documents/")).andExpect(status().isOk())

    }
    @Test
    fun test_post_documents(){
        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andExpect(status().isOk) //DEVE RITORNARE 201 IS CREATED NON 200 OK !!!
    }
    @Test
    fun test_post_documents_duplicate(){
        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andExpect(status().isOk) //DEVE RITORNARE 201 IS CREATED NON 200 OK !!!
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andExpect(status().isConflict)
    }
}
