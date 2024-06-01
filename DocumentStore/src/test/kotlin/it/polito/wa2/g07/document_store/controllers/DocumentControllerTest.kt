package it.polito.wa2.g07.document_store.controllers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.services.DocumentService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest
class DocumentControllerTest(@Autowired var mockMvc: MockMvc) {

    @MockkBean
    private lateinit var documentService: DocumentService

    private val mockDocumentDTO = DocumentDTO(1L,42L, "text/plain", "fileDiTesto.txt", LocalDateTime.now(), "sono un file di testo".toByteArray())
    private val mockMetadataDTO = DocumentMetadataDTO(mockDocumentDTO.id!!, mockDocumentDTO.size, mockDocumentDTO.contentType, mockDocumentDTO.name, mockDocumentDTO.creationTimestamp)
    private val mockReducedMetadataDTO = DocumentReducedMetadataDTO(mockMetadataDTO.id, mockMetadataDTO.name)

    @Nested
    inner class GetDocumentTest {

        private val pageImpl = PageImpl(listOf(mockReducedMetadataDTO))

        @BeforeEach
        fun initMocks() {
            every { documentService.getAllDocuments(any(Pageable::class)) } returns pageImpl
            every { documentService.getDocumentMetadataById(any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentMetadataById(mockMetadataDTO.id) } returns mockMetadataDTO
            every { documentService.getDocumentContent(any(Long::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.getDocumentContent(mockMetadataDTO.id) } returns mockDocumentDTO
        }

        @Test
        fun getDocuments_returnsRestrictedInfo() {
                mockMvc
                    .perform(get("/API/documents/"))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.content").isArray)
                    .andExpect(jsonPath("$.content").isNotEmpty)
                    .andExpect(jsonPath("$.content[0].id").value(mockMetadataDTO.id))
                    .andExpect(jsonPath("$.content[0].name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentMetadataById_returnsDocumentMetadata() {
            val id = mockMetadataDTO.id
            mockMvc
                .perform(get("/API/documents/$id"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(mockMetadataDTO.id))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
                .andExpect(jsonPath("$.contentType").value(mockMetadataDTO.contentType))
                .andExpect(jsonPath("$.name").value(mockMetadataDTO.name))
        }

        @Test
        fun getDocumentMetadataById_documentNotFound() {
            val id = mockMetadataDTO.id + 1
            mockMvc
                .perform(get("/API/documents/$id"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun getDocumentContent_returnsBinaryContent() {
            val id = mockMetadataDTO.id
            mockMvc
                .perform(get("/API/documents/$id/data"))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mockMetadataDTO.contentType!!))
                .andExpect(content().bytes(mockDocumentDTO.content))
        }

        @Test
        fun getDocumentContent_documentNotfound() {
            val id = mockMetadataDTO.id + 1
            mockMvc
                .perform(get("/API/documents/$id/data"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class PostDocumentTest {

        private val newDocument = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        private val newId = 2L

        @BeforeEach
        fun initMocks() {
            every { documentService.create(any(String::class), any(Long::class), any(String::class), any(ByteArray::class)) } answers {
                val name = firstArg<String>()
                val size = secondArg<Long>()
                val contentType = thirdArg<String>()
                DocumentMetadataDTO(newId, size, contentType, name, LocalDateTime.now())
            }
            every { documentService.create(mockDocumentDTO.name, any(Long::class), any(String::class), any(ByteArray::class)) } throws DuplicateDocumentException("Document with the same name already exists")
        }

        @Test
        fun saveDocument_success() {
            mockMvc
                .perform(multipart("/API/documents/").file(newDocument))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(newId))
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
    inner class PutDocumentTest {

        private val newDocument = MockMultipartFile("document", "filename.txt", "text/plain", "some xml".toByteArray())
        private val newId = 2L
        private val existingName = "existing_document.txt"

        @BeforeEach
        fun initMocks() {
            every { documentService.editDocument(any(Long::class), any(String::class), any(Long::class), any(String::class), any(ByteArray::class)) } throws DocumentNotFoundException("The document does not exist")
            every { documentService.editDocument(mockDocumentDTO.id!!, any(String::class), any(Long::class), any(String::class), any(ByteArray::class)) } answers {
                val id = arg<Long>(0)
                val name = arg<String>(1)
                val size = arg<Long>(2)
                val contentType = arg<String>(3)
                DocumentMetadataDTO(id, size, contentType, name, LocalDateTime.now())
            }
            every { documentService.editDocument(any(Long::class), existingName, any(Long::class), any(String::class), any(ByteArray::class)) } throws DuplicateDocumentException("Document with the same name already exists")
            every { documentService.editDocument(mockDocumentDTO.id!!, mockDocumentDTO.name, any(Long::class), any(String::class), any(ByteArray::class)) } answers {
                val id = arg<Long>(0)
                val name = arg<String>(1)
                val size = arg<Long>(2)
                val contentType = arg<String>(3)
                DocumentMetadataDTO(id, size, contentType, name, LocalDateTime.now())
            }
        }

        @Test
        fun editDocument_success_diffName() {
            val id = mockDocumentDTO.id!!
            mockMvc
                .perform(multipart("/API/documents/$id").file(newDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(newDocument.originalFilename))
                .andExpect(jsonPath("$.size").value(newDocument.size))
                .andExpect(jsonPath("$.contentType").value(newDocument.contentType))
        }

        @Test
        fun editDocument_success_sameName() {
            val id = mockDocumentDTO.id!!
            val badDocument = MockMultipartFile(newDocument.name, mockDocumentDTO.name, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$id").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(mockDocumentDTO.name))
                .andExpect(jsonPath("$.size").value(newDocument.size))
                .andExpect(jsonPath("$.contentType").value(newDocument.contentType))
        }

        @Test
        fun editDocument_missingName() {
            val id = mockDocumentDTO.id!!
            val badDocument = MockMultipartFile(newDocument.name, null, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$id").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun editDocument_documentNotFound() {
            val id = mockDocumentDTO.id!! + 1
            mockMvc
                .perform(multipart("/API/documents/$id").file(newDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isNotFound)
        }

        @Test
        fun editDocument_duplicateName() {
            val id = mockDocumentDTO.id!!
            val badDocument = MockMultipartFile(newDocument.name, existingName, newDocument.contentType, newDocument.bytes)
            mockMvc
                .perform(multipart("/API/documents/$id").file(badDocument).with{ it.method = "PUT"; it })
                .andExpect(status().isConflict)
        }
    }

    @Nested
    inner class DeleteDocumentTest {
        @BeforeEach
        fun initMocks() {
            every { documentService.deleteDocument(any(Long::class)) } throws DocumentNotFoundException("The document does not found")
            every { documentService.deleteDocument(mockMetadataDTO.id) } returns Unit
        }

        @Test
        fun deleteDocument_success() {
            val id = mockMetadataDTO.id
            mockMvc
                .perform(delete("/API/documents/$id"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun deleteDocument_notFound() {
            val id = mockMetadataDTO.id + 1
            mockMvc
                .perform(delete("/API/documents/$id"))
                .andExpect(status().isNotFound)
        }
    }

}