package it.polito.wa2.g07.document_store.services

import io.mockk.*
import it.polito.wa2.g07.document_store.dtos.*
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentHistory
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.repositories.DocumentHistoryRepository
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

    private val mockDocumentHistory = DocumentHistory()
    private val mockDocumentMetadata = DocumentMetadata(
        "fileDiTesto.txt",
        "text/plain"
    )
    init {
        mockDocumentHistory.id = 1
        mockDocumentMetadata.metadataID = 1
        mockDocumentMetadata.document=Document("text".toByteArray())
        mockDocumentMetadata.size= mockDocumentMetadata.document.content.size.toLong()
        mockDocumentHistory.addDocumentMetadata(mockDocumentMetadata)
    }

    private val documentRepository = mockk<DocumentRepository>()
    private val documentMetadataRepository = mockk<DocumentMetadataRepository>()
    private val documentHistoryRepository = mockk<DocumentHistoryRepository>()
    private val service = DocumentServiceImpl(documentHistoryRepository, documentRepository, documentMetadataRepository)

    @Nested
    inner class GetDocumentsTests {

        @BeforeEach
        fun initMocks() {
            every { documentMetadataRepository.findTopByDocumentHistoryIdOrderByCreationTimestampDesc(any(Long::class)) } returns Optional.empty()
            every { documentMetadataRepository.findTopByDocumentHistoryIdOrderByCreationTimestampDesc(mockDocumentHistory.id) } returns Optional.of(mockDocumentMetadata)
            every { documentMetadataRepository.findByDocumentHistoryIdAndMetadataID(any(Long::class), any(Long::class)) } returns Optional.empty()
            every { documentMetadataRepository.findByDocumentHistoryIdAndMetadataID(mockDocumentHistory.id, mockDocumentMetadata.metadataID) } returns Optional.of(mockDocumentMetadata)
            every { documentHistoryRepository.findById(any(Long::class)) } returns Optional.empty()
            every { documentHistoryRepository.findById(mockDocumentHistory.id) } returns Optional.of(mockDocumentHistory)
        }

        @Test
        fun getAllDocuments_returnsAllDocuments() {
            val pageReq = PageRequest.of(1, 10)
            val documentMetadataPage = PageImpl(listOf(mockDocumentMetadata), pageReq, 1)
            every { documentMetadataRepository.findAllMostRecentMetadataFromHistory(pageReq) } returns documentMetadataPage

            val dto = service.getAllDocuments(pageReq)

            val expectedDTO = mockDocumentMetadata.toReducedDto()
            val expectedDTOPage = PageImpl(listOf(expectedDTO), pageReq, 1)
            assertEquals(dto, expectedDTOPage)
        }

        @Test
        fun getDocumentMetadata_success() {
            val id = mockDocumentHistory.id
            val dto = service.getDocumentMetadata(id)

            assertEquals(dto, mockDocumentMetadata.toMetadataDto())
        }

        @Test
        fun getDocumentMetadata_documentNotFound() {
            val id = mockDocumentHistory.id + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentMetadata(id)
            }
        }

        @Test
        fun getDocumentContent_returnsDocumentContent() {
            val id = mockDocumentHistory.id
            val content = service.getDocumentContent(id)

            assertEquals(content, mockDocumentMetadata.toDocumentDto())
        }

        @Test
        fun getDocumentContent_documentNotFound() {
            val id = mockDocumentHistory.id + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentContent(id)
            }
        }

        @Test
        fun getDocumentVersionMetadata_returnsDocumentMetadata() {
            val dto = service.getDocumentVersionMetadata(mockDocumentHistory.id, mockDocumentMetadata.metadataID)

            assertEquals(dto, mockDocumentMetadata.toMetadataDto())
        }

        @Test
        fun getDocumentVersionMetadata_historyNotFound() {
            assertThrows<DocumentNotFoundException> {
                service.getDocumentVersionMetadata(mockDocumentHistory.id + 1, mockDocumentMetadata.metadataID)
            }
        }

        @Test
        fun getDocumentVersionMetadata_versionNotFound() {
            val id = mockDocumentMetadata.metadataID + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentVersionMetadata(id, mockDocumentMetadata.metadataID + 1)
            }
        }

        @Test
        fun getDocumentVersionContent_returnsDocumentContent() {
            val content = service.getDocumentVersionContent(mockDocumentHistory.id, mockDocumentMetadata.metadataID)

            assertEquals(content, mockDocumentMetadata.toDocumentDto())
        }

        @Test
        fun getDocumentVersionContent_historyNotFound() {
            assertThrows<DocumentNotFoundException> {
                service.getDocumentVersionContent(mockDocumentHistory.id + 1, mockDocumentMetadata.metadataID)
            }
        }

        @Test
        fun getDocumentVersionContent_versionNotFound() {
            assertThrows<DocumentNotFoundException> {
                service.getDocumentVersionContent(mockDocumentHistory.id, mockDocumentMetadata.metadataID + 1)
            }
        }

        @Test
        fun getDocumentHistory_success() {
            val id = mockDocumentHistory.id
            val dto = service.getDocumentHistory(id)

            assertEquals(dto, mockDocumentHistory.toHistoryDto())
        }

        @Test
        fun getDocumentHistory_documentNotFound() {
            val id = mockDocumentHistory.id + 1
            assertThrows<DocumentNotFoundException> {
                service.getDocumentHistory(id)
            }
        }
    }

    @Nested
    inner class CreateDocumentTests {
        private var newHistoryId = mockDocumentHistory.id + 1
        private var newMetadataId = mockDocumentMetadata.metadataID + 1
        private var newDocumentId = mockDocumentMetadata.document.documentID + 1
        private val creationTimestamp = LocalDateTime.now()

        @BeforeEach
        fun initMocks() {
            every { documentHistoryRepository.save(any(DocumentHistory::class)) } answers {
                val documentHistory = firstArg<DocumentHistory>()
                documentHistory.id = newHistoryId
                documentHistory
            }
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

            val expectedDTO = DocumentMetadataDTO(newHistoryId, newMetadataId, size, contentType, name, creationTimestamp)
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
        private var newMetadataId = mockDocumentMetadata.metadataID + 1
        private var newDocumentId = mockDocumentMetadata.document.documentID + 1
        private val creationTimestamp = LocalDateTime.now()

        @BeforeEach
        fun initMocks() {
            every { documentHistoryRepository.findById(any(Long::class)) } returns Optional.empty()
            every { documentHistoryRepository.findById(mockDocumentHistory.id) } returns Optional.of(mockDocumentHistory)
            every { documentMetadataRepository.findById(any(Long::class)) } returns Optional.empty()
            every { documentMetadataRepository.findById(mockDocumentMetadata.metadataID) } returns Optional.of(mockDocumentMetadata)
            every { documentMetadataRepository.existsByNameIgnoreCaseAndDocumentHistoryIdNot(any(String::class), any(Long::class)) } returns false
            every { documentMetadataRepository.existsByNameIgnoreCaseAndDocumentHistoryIdNot(mockDocumentMetadata.name, any(Long::class)) } returns true
            every { documentMetadataRepository.existsByNameIgnoreCaseAndDocumentHistoryIdNot(mockDocumentMetadata.name, mockDocumentMetadata.metadataID) } returns false
            every { documentHistoryRepository.save(any(DocumentHistory::class)) } answers { firstArg() }
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
        fun editDocument_success_diffName() {
            val historyId = mockDocumentHistory.id
            val versionId = mockDocumentMetadata.metadataID
            val name = mockDocumentMetadata.name + "_v2"
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()
            val result = service.editDocument(versionId, name, size, contentType, content)

            val expectedDTO = DocumentMetadataDTO(historyId, newMetadataId, size, contentType, name, creationTimestamp)
            assertEquals(result, expectedDTO)
        }

        @Test
        fun editDocument_success_sameName() {
            val historyId = mockDocumentHistory.id
            val versionId = mockDocumentMetadata.metadataID
            val name = mockDocumentMetadata.name
            val size = 42L
            val contentType = "application/pdf"
            val content = "hello i'm a PDF file".toByteArray()
            val result = service.editDocument(versionId, name, size, contentType, content)

            val expectedDTO = DocumentMetadataDTO(historyId, newMetadataId, size, contentType, name, creationTimestamp)
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
            every { documentMetadataRepository.existsByNameIgnoreCaseAndDocumentHistoryIdNot(existingName, any(Long::class)) } returns true

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
            every { documentHistoryRepository.existsById(any(Long::class)) } returns false
            every { documentHistoryRepository.existsById(mockDocumentHistory.id) } returns true
            every { documentHistoryRepository.deleteById(any(Long::class)) } returns Unit
            every { documentMetadataRepository.existsByDocumentHistoryIdAndMetadataID(any(Long::class), any(Long::class)) } returns false
            every { documentMetadataRepository.existsByDocumentHistoryIdAndMetadataID(mockDocumentMetadata.documentHistory.id, mockDocumentMetadata.metadataID) } returns true
            every { documentMetadataRepository.deleteById(any(Long::class)) } returns Unit
        }

        @Test
        fun deleteDocumentHistory_success() {
            val id = mockDocumentHistory.id
            service.deleteDocumentHistory(id)

            verify {
                documentHistoryRepository.deleteById(mockDocumentMetadata.documentHistory.id)
            }
        }

        @Test
        fun deleteDocumentHistory_notFound() {
            val id = mockDocumentHistory.id + 1
            assertThrows<DocumentNotFoundException> {
                service.deleteDocumentHistory(id)
            }

            verify(exactly = 0) {
                documentHistoryRepository.deleteById(mockDocumentMetadata.documentHistory.id)
            }
        }

        @Test
        fun deleteDocumentVersion_success() {
            val historyId = mockDocumentMetadata.documentHistory.id
            val versionId = mockDocumentMetadata.metadataID
            service.deleteDocumentVersion(historyId, versionId)

            verify {
                documentMetadataRepository.deleteById(mockDocumentMetadata.metadataID)
            }
        }

        @Test
        fun deleteDocumentHistory_historyNotFound() {
            val historyId = mockDocumentMetadata.documentHistory.id + 1
            val versionId = mockDocumentMetadata.metadataID
            assertThrows<DocumentNotFoundException> {
                service.deleteDocumentVersion(historyId, versionId)
            }

            verify(exactly = 0) {
                documentMetadataRepository.deleteById(mockDocumentMetadata.metadataID)
            }
        }

        @Test
        fun deleteDocumentHistory_versionNotFound() {
            val historyId = mockDocumentMetadata.documentHistory.id
            val versionId = mockDocumentMetadata.metadataID + 1
            assertThrows<DocumentNotFoundException> {
                service.deleteDocumentVersion(historyId, versionId)
            }

            verify(exactly = 0) {
                documentMetadataRepository.deleteById(mockDocumentMetadata.metadataID)
            }
        }
    }

}