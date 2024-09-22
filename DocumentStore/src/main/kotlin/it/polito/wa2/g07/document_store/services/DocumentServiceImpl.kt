package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.*
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentHistory
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.repositories.DocumentHistoryRepository
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class DocumentServiceImpl(
    private val documentHistoryRepository: DocumentHistoryRepository,
    private val documentRepository: DocumentRepository,
    private val documentMetadataRepository: DocumentMetadataRepository,
    private val kafkaTemplate: KafkaTemplate<String, DocumentMetadataDTO>
) : DocumentService {



    fun createDocumentMetadata(name: String, size: Long, contentType: String?, file: ByteArray): DocumentMetadata {
        val doc = Document(file)

        val docSaved=  documentRepository.save(doc)

        val docMetadata = DocumentMetadata(
            name,
            contentType,
            size
        )
        docMetadata.document = docSaved

        return documentMetadataRepository.save(docMetadata)
    }

    override fun create(name: String, size:Long, contentType: String?, file: ByteArray, ownerUserId: String?): DocumentMetadataDTO{

        if(documentMetadataRepository.existsByNameIgnoreCase(name)) {
            throw DuplicateDocumentException("A document with the same name already exists")
        }

        val history = documentHistoryRepository.save(DocumentHistory(ownerUserId))
        val savedMetadata = createDocumentMetadata(name, size, contentType, file)

        history.addDocumentMetadata(savedMetadata)
        documentHistoryRepository.save(history)

        kafkaTemplate.send("DOCUMENT", savedMetadata.toMetadataDto())
        logger.info("Created Document {} - {}",savedMetadata.metadataID, savedMetadata.name)
        return savedMetadata.toMetadataDto()
    }

    @Transactional(readOnly = true)
    override fun getAllDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO> {
        return documentMetadataRepository.findAllMostRecentMetadataFromHistory(pageable).map { d -> d.toReducedDto() }
    }

    override fun deleteDocumentHistory(historyId: Long) {
        if (!documentHistoryRepository.existsById(historyId)) {
            throw DocumentNotFoundException("The document doesn't exist")
        }

        documentHistoryRepository.deleteById(historyId)
        logger.info("Deleted document history {}", historyId)
    }

    override fun deleteDocumentVersion(historyId: Long, metadataId: Long) {
        if (!documentMetadataRepository.existsByDocumentHistoryIdAndMetadataID(historyId, metadataId)) {
            throw DocumentNotFoundException("The document version doesn't exist or the version ID does not match")
        }

        documentMetadataRepository.deleteById(metadataId)
        logger.info("Deleted document version {}", metadataId)
    }

    @Transactional(readOnly = true)
    override fun getDocumentContent(historyId: Long) =
        documentMetadataRepository
            .findTopByDocumentHistoryIdOrderByCreationTimestampDesc(historyId)
            .orElseThrow { DocumentNotFoundException("The document doesn't exist") }
            .toDocumentDto()

    @Transactional(readOnly = true)
    override fun getDocumentVersionContent(historyId: Long, metadataId: Long) =
        documentMetadataRepository
            .findByDocumentHistoryIdAndMetadataID(historyId, metadataId)
            .orElseThrow { DocumentNotFoundException("The document doesn't exist or the version ID does not match") }
            .toDocumentDto()

    @Transactional(readOnly = true)
    override fun getDocumentMetadata(historyId: Long) =
        documentMetadataRepository
            .findTopByDocumentHistoryIdOrderByCreationTimestampDesc(historyId)
            .orElseThrow { DocumentNotFoundException("The document doesn't exist") }
            .toMetadataDto()

    @Transactional(readOnly = true)
    override fun getDocumentVersionMetadata(historyId: Long, metadataId: Long) =
        documentMetadataRepository
            .findByDocumentHistoryIdAndMetadataID(historyId, metadataId)
            .orElseThrow { DocumentNotFoundException("The document doesn't exist or the version ID does not match") }
            .toMetadataDto()

    @Transactional(readOnly = true)
    override fun getDocumentHistory(historyId: Long): DocumentHistoryDTO =
        documentHistoryRepository
            .findById(historyId)
            .orElseThrow { DocumentNotFoundException("The document doesn't exist") }
            .toHistoryDto()

    override fun editDocument(
        historyId: Long,
        name: String,
        size: Long,
        contentType: String?,
        file: ByteArray
    ): DocumentMetadataDTO {
        val history = documentHistoryRepository.findById(historyId).orElseThrow{ DocumentNotFoundException("The document doesn't exist") }
        val oldMetadata = history.documentMetadata.maxBy { it.creationTimestamp }

        if(documentMetadataRepository.existsByNameIgnoreCaseAndDocumentHistoryIdNot(name, historyId)) {
            throw DuplicateDocumentException("A document with the same name already exists")
        }

        val newMetadata = createDocumentMetadata(name, size, contentType, file)
        history.addDocumentMetadata(newMetadata)
        documentHistoryRepository.save(history)

        kafkaTemplate.send("DOCUMENT", newMetadata.toMetadataDto())
        logger.info("Edited Document {} - {} -> {}", newMetadata.metadataID, oldMetadata.name, newMetadata.name)
        return newMetadata.toMetadataDto()
    }

    override fun userIsDocumentOwner(userId: String, historyId: Long): Boolean {
        return documentHistoryRepository.findById(historyId).getOrNull()?.ownerUserId == userId
    }

    companion object{
        private val logger = LoggerFactory.getLogger(DocumentServiceImpl::class.java)
    }
}