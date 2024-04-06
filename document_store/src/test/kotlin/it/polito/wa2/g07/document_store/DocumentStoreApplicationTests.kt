package it.polito.wa2.g07.document_store


import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
    fun test_API_endpoints() {

        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some random text".toByteArray())
        var res= mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andReturn()

        val content: String = res.getResponse().getContentAsString()
        val id=JSONObject(content)["id"]
        println(content+ "id =>"+id)

        mvc.perform(get("/API/documents/")).andExpect(status().isOk())
        mvc.perform(get("/API/documents")).andExpect(status().isOk())
            //.andExpect(status().isOk) //DEVE RITORNARE 201 IS CREATED NON 200 OK !!!

        //TEST GET
        mvc.perform(get("/API/documents/"+id)).andExpect(status().isOk())
        mvc.perform(get("/API/documents/"+id+"/")).andExpect(status().isOk())

        mvc.perform(get("/API/documents/"+id+"/data")).andExpect(status().isOk())
        mvc.perform(get("/API/documents/"+id+"/data/")).andExpect(status().isOk())

        //TEST PUT
        val secondFile = MockMultipartFile("document", "file2.txt", "text/plain", "Lorem ipsum".toByteArray())
        res= mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents/"+id).file(secondFile).with(RequestPostProcessor { it.method = "PUT"; it}))
            .andExpect(status().is2xxSuccessful)
            .andReturn()
        //TEST DELETE
        mvc.perform(delete("/API/documents/"+id)).andExpect(status().is2xxSuccessful)

    }
    @Test
    fun test_post_documents(){
        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents/").file(firstFile))
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
