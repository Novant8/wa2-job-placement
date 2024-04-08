package it.polito.wa2.g07.document_store


import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.matchers.ArrayEquals
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.annotation.DirtiesContext

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.lang.reflect.Array
import java.time.LocalDateTime

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
    companion object {
        // Members of the companion object
        val d1 = DocumentMetadata()
        init {
            d1.contentType="text/plain"
            d1.document=Document()
            d1.document.content="text".toByteArray()
            d1.name="fileDiTesto.txt"
            d1.creationTimestamp= LocalDateTime.now()
            d1.size= d1.document.content.size.toLong()
        }
    }
    @BeforeEach
    fun initDb() {
        docMetadataRepo.deleteAll()
        docRepo.deleteAll()
        docRepo.save(d1.document)
        docMetadataRepo.save(d1);
    }

    @Test
    fun test_get_endpoints() {

        var response1= mvc.perform(get("/API/documents/")).andExpect(status().isOk()).andReturn().response.contentAsString
        var response2= mvc.perform(get("/API/documents")).andExpect(status().isOk()).andReturn().response.contentAsString
        assertEquals(response1, response2)

        val id=JSONObject(response1).getJSONArray("content").getJSONObject(0).getString("id")
        assertEquals(d1.metadataID, id.toLong())

        response1= mvc.perform(get("/API/documents/"+id)).andExpect(status().isOk()).andReturn().response.contentAsString
        response2= mvc.perform(get("/API/documents/"+id+"/")).andExpect(status().isOk()).andReturn().response.contentAsString
        assertEquals(response1, response2)
        var obj_res = JSONObject(response1)
        assertEquals(d1.name,obj_res.getString("name"))
        /*assertEquals(
            d1.creationTimestamp.truncatedTo(ChronoUnit.SECONDS).toString(),
            LocalDateTime.parse(obj_res.getString("creation_timestamp")).truncatedTo(ChronoUnit.SECONDS).toString()
        )*/
        assertEquals(d1.contentType,obj_res.getString("contentType"))
        assertEquals(d1.size,obj_res.getLong("size"))
        var responseContent1=mvc.perform(get("/API/documents/"+id+"/data")).andExpect(status().isOk()).andReturn().response.contentAsByteArray
        var responseContent2= mvc.perform(get("/API/documents/"+id+"/data/")).andExpect(status().isOk()).andReturn().response.contentAsByteArray
        ArrayEquals(responseContent1).equals(responseContent2)
        ArrayEquals(responseContent1).equals(d1.document.content)
    }

    @Test
    fun test_post_documents(){

        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        val secondFile = MockMultipartFile("document", "filename2.txt", "text/plain", "some xml".toByteArray())

        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents/").file(firstFile))
            .andExpect(status().isCreated)
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(secondFile))
            .andExpect(status().isCreated)
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(secondFile))
            .andExpect(status().isConflict)
    }
    @Test
    fun test_put_documents(){
        val file = MockMultipartFile("document", "fileCheSostituisce.txt", "text/plain", "Lorem ipsum".toByteArray())
         mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents/"+d1.metadataID).file(file).with { it.method = "PUT"; it })
            .andExpect(status().isNoContent)

    }
    @Test
    fun test_post_documents_duplicate(){
        val firstFile = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andExpect(status().isCreated)
        mvc.perform(
            MockMvcRequestBuilders.multipart("/API/documents").file(firstFile))
            .andExpect(status().isConflict)
    }
    @Test
    fun test_double_delete(){
        mvc.perform(delete("/API/documents/"+d1.metadataID)).andExpect(status().isNoContent)
        mvc.perform(delete("/API/documents/"+d1.metadataID)).andExpect(status().isNotFound)
    }
    @Test
    fun test_chain(){
        val documentStoreEndpoint = "/API/documents/"

        val response1=mvc.perform(get(documentStoreEndpoint))
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsString

        val testFile = MockMultipartFile("document", "testchain.txt", "text/plain", "some html".toByteArray())

        val postResponse = mvc.perform(
            MockMvcRequestBuilders.multipart(documentStoreEndpoint)
                .file(testFile)
        )
            .andExpect(status().isCreated())
            .andReturn()
            .response
            .contentAsString

        val newId = JSONObject(postResponse).getLong("id")

        assertNotNull(newId)

        val response2=mvc.perform(get(documentStoreEndpoint))
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsString

        assertNotEquals(response1,response2)
        
        val contentArray = JSONObject(response2).getJSONArray("content")
        var isIdPresent = false
        for (i in 0 until contentArray.length()) {
            val jsonObject = contentArray.getJSONObject(i)
            val id = jsonObject.getLong("id")
            if (id == newId) {
                isIdPresent = true
                break
            }
        }


        assertTrue(isIdPresent)


        val downloadFile=mvc.perform(get("$documentStoreEndpoint$newId/data"))
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsByteArray

        assertNotNull(downloadFile)

        mvc.perform(delete("$documentStoreEndpoint$newId"))
            .andExpect(status().isNoContent)
    }


}
