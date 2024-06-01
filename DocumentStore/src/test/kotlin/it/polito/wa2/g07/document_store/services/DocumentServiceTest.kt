package it.polito.wa2.g07.document_store.services

import io.mockk.*
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.toDocumentDto
import it.polito.wa2.g07.document_store.dtos.toMetadataDto
import it.polito.wa2.g07.document_store.dtos.toReducedDto
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

class DocumentServiceTest {

    private val mockDocumentMetadata = DocumentMetadata()
    init {
        mockDocumentMetadata.metadataID = 1
        mockDocumentMetadata.contentType="text/plain"
        mockDocumentMetadata.document=Document()
        mockDocumentMetadata.document.content="text".toByteArray()
        mockDocumentMetadata.name="fileDiTesto.txt"
        mockDocumentMetadata.creationTimestamp= LocalDateTime.now()
        mockDocumentMetadata.size= mockDocumentMetadata.document.content.size.toLong()
    }

    private val documentRepository = mockk<DocumentRepository>()
    private val documentMetadataRepository = mockk<DocumentMetadataRepository>()
    private val service = DocumentServiceImpl(documentRepository, documentMetadataRepository)

    @Nested
    inner class GetDocumentsTests {

        @BeforeEach
        fun initMocks() {
            every { documentMetadataRepository.findById(any(Long::class)) } returns Optional.empty()
            every { documentMetadataRepository.findById(mockDocumentMetadata.metadataID) } returns Optional.of(mockDocumentMetadata)
        }

        @Test
        fun getAllDocuments_returnsAllDocuments() {
            val pageReq = PageRequest.of(1, 10)
            val documentMetadataPage = PageImpl(listOf(mockDocumentMetadata), pageReq, 1)
            every { documentMetadataRepository.findAll(pageReq) } returns documentMetadataPage

            val dto = service.getAllDocuments(pageReq)

            val expectedDTO = mockDocumentMetadata.toReducedDto()
            val expectedDTOPage = PageImpl(listOf(expectedDTO), pageReq, 1)
            assertEquals(dto, expectedDTOPage)
        }

        @Test
        fun getDocumentMetadataById_returnsDocumentMetadata() {
            val id = mockDocumentMetadata.metadataID
            val dto = service.getDocumentMetadataById(id)

            assertEquals(dto, mockDocumentMetadata.toMetadataDto())
        }

        @Test
        fun getDocumentMetadataById_documentNotFound() {
            val id = mockDocumentMetadata.metadataID + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentMetadataById(id)
            }
        }

        @Test
        fun getDocumentContent_returnsDocumentContent() {
            val id = mockDocumentMetadata.metadataID
            val content = service.getDocumentContent(id)

            assertEquals(content, mockDocumentMetadata.toDocumentDto())
        }

        @Test
        fun getDocumentContent_documentNotFound() {
            val id = mockDocumentMetadata.metadataID + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentContent(id)
            }
        }
    }

    @Nested
    inner class CreateDocumentTests {
        private val newMetadataId = mockDocumentMetadata.metadataID + 1
        private val newDocumentId = mockDocumentMetadata.document.documentID + 1
        private val creationTimestamp = LocalDateTime.now()

        @BeforeEach
        fun initMocks() {
            every { documentMetadataRepository.existsByNameIgnoreCase(any(String::class)) } returns false
            every { documentMetadataRepository.existsByNameIgnoreCase(mockDocumentMetadata.name) } returns true
            every { documentRepository.save(any(Document::class)) } answers {
                val document = firstArg<Document>()
                document.documentID = newDocumentId
                document
            }
            every { documentMetadataRepository.save(any(DocumentMetadata::class)) } answers {
                val metadata = firstArg<DocumentMetadata>()
                metadata.metadataID = newMetadataId
                metadata.creationTimestamp = creationTimestamp
                metadata
            }
        }

        @Test
        fun create_success() {
            val name = "new_document"
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()
            val result = service.create(name, size, contentType, content)

            val expectedDTO = DocumentMetadataDTO(newMetadataId, size, contentType, name, creationTimestamp)
            assertEquals(result, expectedDTO)
        }

        @Test
        fun create_duplicateName() {
            val name = mockDocumentMetadata.name
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()

            assertThrows<DuplicateDocumentException> {
                service.create(name, size, contentType, content)
            }
        }
    }

    @Nested
    inner class EditDocumentTests {
        @BeforeEach
        fun initMocks() {
            every { documentMetadataRepository.findById(any(Long::class)) } returns Optional.empty()
            every { documentMetadataRepository.findById(mockDocumentMetadata.metadataID) } returns Optional.of(mockDocumentMetadata)
            every { documentMetadataRepository.existsByNameIgnoreCaseAndMetadataIDNot(any(String::class), any(Long::class)) } returns false
            every { documentMetadataRepository.existsByNameIgnoreCaseAndMetadataIDNot(mockDocumentMetadata.name, any(Long::class)) } returns true
            every { documentMetadataRepository.existsByNameIgnoreCaseAndMetadataIDNot(mockDocumentMetadata.name, mockDocumentMetadata.metadataID) } returns false
            every { documentRepository.save(any(Document::class)) } answers { firstArg() }
            every { documentMetadataRepository.save(any(DocumentMetadata::class)) } answers { firstArg() }
        }

        @Test
        fun editDocument_success_diffName() {
            val id = mockDocumentMetadata.metadataID
            val name = mockDocumentMetadata.name + "_v2"
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()
            val result = service.editDocument(id, name, size, contentType, content)

            val expectedDTO = DocumentMetadataDTO(id, size, contentType, name, mockDocumentMetadata.creationTimestamp)
            assertEquals(result, expectedDTO)
        }

        @Test
        fun editDocument_success_sameName() {
            val id = mockDocumentMetadata.metadataID
            val name = mockDocumentMetadata.name
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()
            val result = service.editDocument(id, name, size, contentType, content)

            val expectedDTO = DocumentMetadataDTO(id, size, contentType, name, mockDocumentMetadata.creationTimestamp)
            assertEquals(result, expectedDTO)
        }

        @Test
        fun editDocument_documentNotFound() {
            val id = mockDocumentMetadata.metadataID + 1
            val name = mockDocumentMetadata.name + "_v2"
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()

            assertThrows<DocumentNotFoundException> {
                service.editDocument(id, name, size, contentType, content)
            }
        }

        @Test
        fun editDocument_duplicateName() {
            val existingName = "existing_document"
            every { documentMetadataRepository.existsByNameIgnoreCaseAndMetadataIDNot(existingName, any(Long::class)) } returns true

            val id = mockDocumentMetadata.metadataID
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()

            assertThrows<DuplicateDocumentException> {
                service.editDocument(id, existingName, size, contentType, content)
            }
        }
    }

    @Nested
    inner class DeleteDocumentTests {
        @BeforeEach
        fun initMocks() {
            every { documentMetadataRepository.existsById(any(Long::class)) } returns false
            every { documentMetadataRepository.existsById(mockDocumentMetadata.metadataID) } returns true
            every { documentMetadataRepository.deleteById(any(Long::class)) } answers {}
        }

        @Test
        fun deleteDocument_success() {
            val id = mockDocumentMetadata.metadataID
            service.deleteDocument(id)

            verify {
                documentMetadataRepository.deleteById(mockDocumentMetadata.metadataID)
            }
        }

        @Test
        fun deleteDocument_documentNotFound() {
            val id = mockDocumentMetadata.metadataID + 1
            assertThrows<DocumentNotFoundException> {
                service.deleteDocument(id)
            }

            verify(exactly = 0) {
                documentMetadataRepository.deleteById(mockDocumentMetadata.metadataID)
            }
        }
    }

}