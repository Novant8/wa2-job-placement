package it.polito.wa2.g07.document_store.integrations


import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.toDocumentDto
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentHistory
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.repositories.DocumentHistoryRepository
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.*

import org.testcontainers.junit.jupiter.Container

import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@WithMockUser(roles = [ "manager" ])
@Testcontainers
class DocumentStoreApplicationTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mvc: MockMvc
    @Autowired
    lateinit var docRepo: DocumentRepository
    @Autowired
    lateinit var docMetadataRepo: DocumentMetadataRepository
    @Autowired
    lateinit var docHistoryRepo: DocumentHistoryRepository

    companion object {
        @Container
        val db = PostgreSQLContainer("postgres:latest")
        val documentStoreEndpoint = "/API/documents"
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", db::getJdbcUrl)
            registry.add("spring.datasource.username", db::getUsername)
            registry.add("spring.datasource.password", db::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
        }
    }

    private lateinit var d1: DocumentDTO
    private lateinit var d2: DocumentDTO
    private lateinit var d3: DocumentDTO

    @BeforeEach
    fun initDb() {
        docMetadataRepo.deleteAll()
        docHistoryRepo.deleteAll()
        docRepo.deleteAll()

        val h1 = DocumentHistory()
        val h2 = DocumentHistory()

        val d1 = DocumentMetadata(
            "fileDiTesto.txt",
            "text/plain",
        )
        d1.document = docRepo.save(Document("text".toByteArray()))
        d1.size = d1.document.content.size.toLong()

        val d2 = DocumentMetadata(
            "fileDiTesto_v2.txt",
            "text/plain",
        )
        d2.document = docRepo.save(Document("more text".toByteArray()))
        d2.size = d2.document.content.size.toLong()

        val d3 = DocumentMetadata(
            "altroFile.txt",
            "text/plain",
        )
        d3.document = docRepo.save(Document("other text".toByteArray()))
        d3.size= d3.document.content.size.toLong()

        h1.addDocumentMetadata(d1)
        h1.addDocumentMetadata(d2)
        h2.addDocumentMetadata(d3)

        docHistoryRepo.save(h1)
        docHistoryRepo.save(h2)

        this.d1 = docMetadataRepo.save(d1).toDocumentDto()
        this.d2 = docMetadataRepo.save(d2).toDocumentDto()
        this.d3 = docMetadataRepo.save(d3).toDocumentDto()
    }

    @Nested
    inner class GetDocumentTests {

        @Test
        fun getDocuments_showMostRecent() {
            mvc
                .get("/API/documents")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.content") { isArray() }
                        jsonPath("$.content.length()") { value(2) }
                        jsonPath("$.content[*].historyId") { value(containsInAnyOrder(d1.historyId.toInt(), d3.historyId.toInt())) }
                        jsonPath("$.content[*].versionId") { value(containsInAnyOrder(d2.versionId!!.toInt(), d3.versionId!!.toInt())) }
                    }
                }
        }

        @Test
        fun getDocument_showMostRecent() {
            mvc
                .get("/API/documents/${d1.historyId}")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.historyId") { value(d1.historyId) }
                        jsonPath("$.versionId") { value(d2.versionId) }
                    }
                }
        }

        @Test
        fun getDocument_notFound() {
            mvc
                .get("/API/documents/${d1.historyId + 1000}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersion() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d1.versionId}")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.historyId") { value(d1.historyId) }
                        jsonPath("$.versionId") { value(d1.versionId) }
                    }
                }
        }

        @Test
        fun getDocumentVersion_historyNotFound() {
            mvc
                .get("/API/documents/${d1.historyId+1000}/version/${d1.versionId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersion_versionNotFound() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d1.versionId!!+1000}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersion_versionNotBelongingToHistory() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d3.versionId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentContent() {
            mvc
                .get("/API/documents/${d1.historyId}/data")
                .andExpect {
                    status { isOk() }
                    header {
                        string(HttpHeaders.CONTENT_TYPE, d2.contentType!!)
                        string(HttpHeaders.CONTENT_DISPOSITION, stringContainsInOrder(d2.name))
                    }
                    content { bytes(d2.content) }
                }
        }

        @Test
        fun getDocumentContent_notFound() {
            mvc
                .get("/API/documents/${d1.historyId + 1000}/data")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersionContent() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d1.versionId}/data")
                .andExpect {
                    status { isOk() }
                    content { bytes(d1.content) }
                }
        }

        @Test
        fun getDocumentVersionContent_historyNotFound() {
            mvc
                .get("/API/documents/${d1.historyId+1000}/version/${d1.versionId}/data")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersionContent_versionNotFound() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d1.versionId!!+1000}/data")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentVersionContent_versionNotBelongingToHistory() {
            mvc
                .get("/API/documents/${d1.historyId}/version/${d3.versionId}/data")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun getDocumentHistory_success() {
            mvc
                .get("/API/documents/${d1.historyId}/history")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(d1.historyId) }
                    jsonPath("$.versions") { isArray() }
                    jsonPath("$.versions.length()") { value(2) }
                    jsonPath("$.versions[0].versionId") { value(d2.versionId) }
                    jsonPath("$.versions[1].versionId") { value(d1.versionId) }
                }
        }

        @Test
        fun getDocumentHistory_notFound() {
            mvc
                .get("/API/documents/${d1.historyId+1000}/history")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

    @Nested
    inner class PostTests {

        private val newFile = MockMultipartFile("document", "newFile.txt", "text/plain", "some content".toByteArray())

        @Test
        fun createDocument_success() {
            val response =
                mvc
                    .multipart("/API/documents") {
                        file(newFile)
                    }
                    .andExpect {
                        status { isCreated() }
                    }
                    .andReturn().response
            val historyId = JSONObject(response.contentAsString).getLong("historyId")
            mvc
                .get("/API/documents/${historyId}")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun createDocument_duplicateName() {
            mvc
                .multipart("/API/documents") {
                    file(newFile)
                }
                .andExpect {
                    status { isCreated() }
                }
            mvc
                .multipart("/API/documents") {
                    file(newFile)
                }
                .andExpect {
                    status { isConflict() }
                }
        }

    }

    @Nested
    inner class PutTests {

        @Test
        fun editDocument_success() {
            val newFile = MockMultipartFile("document", "newFile.txt", "text/plain", "some content".toByteArray())
            mvc
                .multipart(HttpMethod.PUT,"/API/documents/${d3.historyId}") {
                    file(newFile)
                }
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.historyId") { value(d3.historyId) }
                        jsonPath("$.versionId") { value(not(d3.versionId!!.toInt())) }
                    }
                }
        }

        @Test
        fun editDocument_duplicateName_conflict() {
            val newFile = MockMultipartFile("document", d2.name, "text/plain", "some content".toByteArray())
            mvc
                .multipart(HttpMethod.PUT,"/API/documents/${d3.historyId}") {
                    file(newFile)
                }
                .andExpect {
                    status { isConflict() }
                }
        }

        @Test
        fun editDocument_duplicateName_allow() {
            val newFile = MockMultipartFile("document", d1.name, "text/plain", "some content".toByteArray())
            mvc
                .multipart(HttpMethod.PUT,"/API/documents/${d3.historyId}") {
                    file(newFile)
                }
                .andExpect {
                    status { isOk() }
                }
        }

    }

    @Nested
    inner class DeleteTests {

        @Test
        fun deleteHistory_success() {
            mvc
                .delete("/API/documents/${d1.historyId}")
                .andExpect {
                    status { isNoContent() }
                }
            mvc
                .get("/API/documents/${d1.historyId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteHistory_notFound() {
            mvc
                .delete("/API/documents/${d1.historyId + 1000}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteVersion_success() {
            mvc
                .delete("/API/documents/${d2.historyId}/version/${d2.versionId}")
                .andExpect {
                    status { isNoContent() }
                }
            mvc
                .get("/API/documents/${d2.historyId}")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.historyId") { value(d2.historyId) }
                        jsonPath("$.versionId") { value(d1.versionId) }
                    }
                }
        }

        @Test
        fun deleteVersion_historyNotFound() {
            mvc
                .delete("/API/documents/${d1.historyId + 1000}/version/${d1.versionId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteVersion_versionNotFound() {
            mvc
                .delete("/API/documents/${d1.historyId}/version/${d1.versionId!! + 1000}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun deleteVersion_versionNotMatchingHistory() {
            mvc
                .delete("/API/documents/${d1.historyId}/version/${d3.versionId}")
                .andExpect {
                    status { isNotFound() }
                }
        }

    }

    @Test
    fun completeChain() {

        // Get documents
        val get1Response =
            mvc
                .get("/API/documents")
                .andExpect { status { isOk() } }
                .andReturn().response

        // Add new document
        val testFile = MockMultipartFile("document", "testchain.txt", "text/html", "some html".toByteArray())
        val postResponse =
            mvc
                .multipart("/API/documents") {
                    file(testFile)
                }
                .andExpect { status { isCreated() } }
                .andReturn().response
        val historyId = JSONObject(postResponse.contentAsString).getLong("historyId")
        assertNotNull(historyId)

        // Check that added document is present in search
        val get2Response =
            mvc
                .get("/API/documents")
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.content[*].historyId") { value(hasItem(historyId.toInt())) }
                    }
                }
                .andReturn().response
        assertNotEquals(get1Response.contentAsString, get2Response.contentAsString)

        // Edit document
        val newTestFile = MockMultipartFile("document", "testchain.txt", "text/html", "some edited html".toByteArray())
        mvc
            .multipart(HttpMethod.PUT, "/API/documents/${historyId}") {
                file(newTestFile)
            }
            .andExpect { status { isOk() } }

        // Check that the document is edited
        mvc
            .get("/API/documents/${historyId}/data")
            .andExpect {
                status { isOk() }
                content { bytes(newTestFile.bytes) }
            }

        // Delete document history
        mvc
            .delete("/API/documents/${historyId}")
            .andExpect { status { isNoContent() } }

        // Check that the document does not exist
        mvc
            .get("/API/documents/${historyId}")
            .andExpect { status { isNotFound() } }
    }
}
