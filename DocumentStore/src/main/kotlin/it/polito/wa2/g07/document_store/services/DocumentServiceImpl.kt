package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.*
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class DocumentServiceImpl(private val documentRepository: DocumentRepository,
                          private val documentMetadataRepository: DocumentMetadataRepository,
                          private val kafkaTemplate: KafkaTemplate<String, DocumentMetadataDTO>
) : DocumentService {

    override fun create(name: String, size:Long, contentType: String?, file: ByteArray): DocumentMetadataDTO{

        if(documentMetadataRepository.existsByNameIgnoreCase(name)) {
            throw DuplicateDocumentException("A document with the same name already exists")
        }

        val doc = Document()
        doc.content= file

        val docSaved=  documentRepository.save(doc)
        val docMetadata = DocumentMetadata()

        docMetadata.name = name
        docMetadata.contentType= contentType
        docMetadata.document = docSaved
        docMetadata.size = size
        docMetadata.creationTimestamp= LocalDateTime.now()

        val savedMetadata = documentMetadataRepository.save(docMetadata).toMetadataDto()
        kafkaTemplate.send("DOCUMENT", savedMetadata)
        logger.info("Created Document {} - {}",savedMetadata.id, savedMetadata.name )
        return savedMetadata

    }

    @Transactional(readOnly = true)
    override fun getAllDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO>{
        return documentMetadataRepository.findAll(pageable).map { d-> d.toReducedDto() }
    }

    override fun deleteDocument(metadataId: Long) {
        if (!documentMetadataRepository.existsById(metadataId)) {
            throw DocumentNotFoundException("The document doesn't exist")
        }

        documentMetadataRepository.deleteById(metadataId)
        logger.info("Deleted document {}", metadataId)
    }

    @Transactional(readOnly = true)
    override fun getDocumentContent(metadataId: Long): DocumentDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toDocumentDto()
    }

    @Transactional(readOnly = true)
    override fun getDocumentMetadataById(metadataId: Long): DocumentMetadataDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toMetadataDto()
    }

    override fun editDocument(
        metadataId: Long,
        name: String,
        size: Long,
        contentType: String?,
        file: ByteArray
    ): DocumentMetadataDTO {
        val documentMetadataOpt = documentMetadataRepository.findById(metadataId)
        if (!documentMetadataOpt.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }

        if(documentMetadataRepository.existsByNameIgnoreCaseAndMetadataIDNot(name, metadataId)) {
            throw DuplicateDocumentException("A document with the same name already exists")
        }

        val docMetadata = documentMetadataOpt.get()
        val oldName = docMetadata.name
        docMetadata.document.content = file
        docMetadata.name = name
        docMetadata.contentType= contentType
        docMetadata.size = size
        docMetadata.creationTimestamp= LocalDateTime.now()

        val savedMetadata = documentMetadataRepository.save(docMetadata).toMetadataDto()
        kafkaTemplate.send("DOCUMENT", savedMetadata)
        logger.info("Edited Document {} - {} -> {}", savedMetadata.id, oldName, savedMetadata.name)
        return savedMetadata
    }

    companion object{
        val logger = LoggerFactory.getLogger(DocumentServiceImpl::class.java)
    }
}