package it.polito.wa2.g07.document_store.controllers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.DocumentHistoryDTO
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.services.DocumentService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest(@Autowired var mockMvc: MockMvc) {

    @MockkBean
    private lateinit var documentService: DocumentService

    private val mockDocumentDTO = DocumentDTO(1L, 1L,42L, "text/plain", "fileDiTesto.txt", LocalDateTime.now(), "sono un file di testo".toByteArray())
    private val mockMetadataDTO = DocumentMetadataDTO(mockDocumentDTO.historyId, mockDocumentDTO.versionId!!, mockDocumentDTO.size, mockDocumentDTO.contentType, mockDocumentDTO.name, mockDocumentDTO.creationTimestamp)
    private val mockReducedMetadataDTO = DocumentReducedMetadataDTO(mockMetadataDTO.historyId, mockMetadataDTO.versionId, mockMetadataDTO.name)
    private val mockDocumentHistoryDTO = DocumentHistoryDTO(1L, listOf(mockReducedMetadataDTO))

    @Nested
    inner class GetDocumentTest {

        private val pageImpl = PageImpl(listOf(mockReducedMetadataDTO))

        @BeforeEach
        fun initMocks() {
            every { documentService.getAllDocuments(any(Pageable::class)) } returns pageImpl
            every { documentService.getDocumentMetadata(any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentMetadata(mockMetadataDTO.historyId) } returns mockMetadataDTO
            every { documentService.getDocumentContent(any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentContent(mockMetadataDTO.historyId) } returns mockDocumentDTO
            every { documentService.getDocumentVersionMetadata(any(Long::class), any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentVersionMetadata(mockMetadataDTO.historyId, mockMetadataDTO.versionId) } returns mockMetadataDTO
            every { documentService.getDocumentVersionContent(any(Long::class), any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentVersionContent(mockMetadataDTO.historyId, mockMetadataDTO.versionId) } returns mockDocumentDTO
            every { documentService.getDocumentHistory(any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentHistory(mockMetadataDTO.historyId) } returns mockDocumentHistoryDTO
        }

        @Test
        fun getDocuments_returnsRestrictedInfo() {
                mockMvc
                    .perform(get("/API/documents/"))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.content").isArray)
                    .andExpect(jsonPath("$.content").isNotEmpty)
                    .andExpect(jsonPath("$.content[0].versionId").value(mockMetadataDTO.versionId))
                    .andExpect(jsonPath("$.content[0].name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentMetadata_returnsDocumentMetadata() {
            val historyId = mockMetadataDTO.historyId
            mockMvc
                .perform(get("/API/documents/$historyId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.historyId").value(mockDocumentDTO.historyId))
                .andExpect(jsonPath("$.versionId").value(mockMetadataDTO.versionId))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
                .andExpect(jsonPath("$.contentType").value(mockMetadataDTO.contentType))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentMetadata_documentNotFound() {
            val historyId = mockMetadataDTO.historyId + 1
            mockMvc
                .perform(get("/API/documents/$historyId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun getDocumentContent_returnsBinaryContent() {
            val historyId = mockMetadataDTO.historyId
            mockMvc
                .perform(get("/API/documents/$historyId/data"))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mockMetadataDTO.contentType!!))
                .andExpect(content().bytes(mockDocumentDTO.content))
        }

        @Test
        fun getDocumentContent_documentNotfound() {
            val historyId = mockMetadataDTO.historyId + 1
            mockMvc
                .perform(get("/API/documents/$historyId/data"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun getDocumentVersionMetadata_returnsDocumentMetadata() {
            val historyId = mockMetadataDTO.historyId
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(get("/API/documents/$historyId/version/$versionId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.historyId").value(mockDocumentDTO.historyId))
                .andExpect(jsonPath("$.versionId").value(mockMetadataDTO.versionId))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
                .andExpect(jsonPath("$.contentType").value(mockMetadataDTO.contentType))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentVersionMetadata_documentNotFound() {
            val historyId = mockMetadataDTO.historyId + 1
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(get("/API/documents/$historyId/version/$versionId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun getDocumentVersionContent_returnsBinaryContent() {
            val historyId = mockMetadataDTO.historyId
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(get("/API/documents/$historyId/version/$versionId/data"))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mockMetadataDTO.contentType!!))
                .andExpect(content().bytes(mockDocumentDTO.content))
        }

        @Test
        fun getDocumentVersionContent_documentNotfound() {
            val historyId = mockMetadataDTO.historyId + 1
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(get("/API/documents/$historyId/version/$versionId/data"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun getDocumentHistory_success() {
            val historyId = mockMetadataDTO.historyId
            mockMvc
                .perform(get("/API/documents/$historyId/history"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(historyId))
                .andExpect(jsonPath("$.versions").isArray)
                .andExpect(jsonPath("$.versions").isNotEmpty)
                .andExpect(jsonPath("$.versions[0].versionId").value(mockMetadataDTO.versionId))
                .andExpect(jsonPath("$.versions[0].name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentHistory_documentNotFound() {
            val historyId = mockMetadataDTO.historyId + 1
            mockMvc
                .perform(get("/API/documents/$historyId/history"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PostDocumentTest {

        private val newDocument = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        private val newId = 2L

        @BeforeEach
        fun initMocks() {
            every { documentService.create(any(String::class), any(Long::class), any(String::class), any(ByteArray::class), any(String::class)) } answers {
                val name = firstArg<String>()
                val size = secondArg<Long>()
                val contentType = thirdArg<String>()
                DocumentMetadataDTO(mockDocumentDTO.historyId, newId, size, contentType, name, LocalDateTime.now())
            }
            every { documentService.create(mockDocumentDTO.name, any(Long::class), any(String::class), any(ByteArray::class), any(String::class)) } throws DuplicateDocumentException("Document with the same name already exists")
        }

        @Test
        fun saveDocument_success() {
            mockMvc
                .perform(multipart("/API/documents/").file(newDocument))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.historyId").value(mockDocumentDTO.historyId))
                .andExpect(jsonPath("$.versionId").value(newId))
                .andExpect(jsonPath("$.name").value(newDocument.originalFilename))
                .andExpect(jsonPath("$.size").value(newDocument.size))
                .andExpect(jsonPath("$.contentType").value(newDocument.contentType))
        }

        @Test
        fun saveDocument_missingName() {
            val badDocument = MockMultipartFile(newDocument.name, null, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/").file(badDocument))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun saveDocument_duplicateName() {
            val badDocument = MockMultipartFile(newDocument.name, mockDocumentDTO.name, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/").file(badDocument))
                .andExpect(status().isConflict)
        }
    }

    @Nested
    @WithMockUser(roles = [ "operator" ])
    inner class PutDocumentTest {

        private val newDocument = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        private val existingName = "existing_document.txt"

        @BeforeEach
        fun initMocks() {
            every { documentService.editDocument(any(Long::class), any(String::class), any(Long::class), any(String::class), any(ByteArray::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.editDocument(mockDocumentDTO.versionId!!, any(String::class), any(Long::class), any(String::class), any(ByteArray::class)) } answers {
                val id = arg<Long>(0)
                val name = arg<String>(1)
                val size = arg<Long>(2)
                val contentType = arg<String>(3)
                DocumentMetadataDTO(mockDocumentDTO.historyId, id, size, contentType, name, LocalDateTime.now())
            }
            every { documentService.editDocument(any(Long::class), existingName, any(Long::class), any(String::class), any(ByteArray::class)) } throws DuplicateDocumentException("Document with the same name already exists")
            every { documentService.editDocument(mockDocumentDTO.versionId!!, mockDocumentDTO.name, any(Long::class), any(String::class), any(ByteArray::class)) } answers {
                val id = arg<Long>(0)
                val name = arg<String>(1)
                val size = arg<Long>(2)
                val contentType = arg<String>(3)
                DocumentMetadataDTO(mockDocumentDTO.historyId, id, size, contentType, name, LocalDateTime.now())
            }
        }

        @Test
        fun editDocument_success_diffName() {
            val historyId = mockDocumentDTO.historyId
            mockMvc
                .perform(multipart("/API/documents/$historyId").file(newDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.historyId").value(mockDocumentDTO.historyId))
                .andExpect(jsonPath("$.versionId").value(historyId))
                .andExpect(jsonPath("$.name").value(newDocument.originalFilename))
                .andExpect(jsonPath("$.size").value(newDocument.size))
                .andExpect(jsonPath("$.contentType").value(newDocument.contentType))
        }

        @Test
        fun editDocument_success_sameName() {
            val historyId = mockDocumentDTO.historyId
            val badDocument = MockMultipartFile(newDocument.name, mockDocumentDTO.name, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$historyId").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.historyId").value(mockDocumentDTO.historyId))
                .andExpect(jsonPath("$.versionId").value(historyId))
                .andExpect(jsonPath("$.name").value(mockDocumentDTO.name))
                .andExpect(jsonPath("$.size").value(newDocument.size))
                .andExpect(jsonPath("$.contentType").value(newDocument.contentType))
        }

        @Test
        fun editDocument_missingName() {
            val historyId = mockDocumentDTO.historyId
            val badDocument = MockMultipartFile(newDocument.name, null, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$historyId").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun editDocument_documentNotFound() {
            val historyId = mockDocumentDTO.historyId + 1
            mockMvc
                .perform(multipart("/API/documents/$historyId").file(newDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isNotFound)
        }

        @Test
        fun editDocument_duplicateName() {
            val historyId = mockDocumentDTO.historyId
            val badDocument = MockMultipartFile(newDocument.name, existingName, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$historyId").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isConflict)
        }
    }

    @Nested
    @WithMockUser(roles = [ "manager" ])
    inner class DeleteDocumentTest {
        @BeforeEach
        fun initMocks() {
            every { documentService.deleteDocumentHistory(any(Long::class)) } throws DocumentNotFoundException("The document does not found")
            every { documentService.deleteDocumentHistory(mockMetadataDTO.historyId) } returns Unit
            every { documentService.deleteDocumentVersion(any(Long::class), any(Long::class)) } throws DocumentNotFoundException("The document does not found")
            every { documentService.deleteDocumentVersion(mockMetadataDTO.historyId, mockMetadataDTO.versionId) } returns Unit
        }

        @Test
        fun deleteDocument_success() {
            val historyId = mockMetadataDTO.historyId
            mockMvc
                .perform(delete("/API/documents/$historyId"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun deleteDocument_notFound() {
            val historyId = mockMetadataDTO.historyId + 1
            mockMvc
                .perform(delete("/API/documents/$historyId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun deleteVersion_success() {
            val historyId = mockMetadataDTO.historyId
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(delete("/API/documents/$historyId/version/$versionId"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun deleteVersion_historyNotFound() {
            val historyId = mockMetadataDTO.historyId + 1
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(delete("/API/documents/$historyId/version/$versionId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun deleteVersion_versionNotFound() {
            val historyId = mockMetadataDTO.historyId + 1
            val versionId = mockMetadataDTO.versionId
            mockMvc
                .perform(delete("/API/documents/$historyId/version/$versionId"))
                .andExpect(status().isNotFound)
        }
    }

}