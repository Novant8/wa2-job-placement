package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.*
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service

class DocumentServiceImpl(private val documentRepository: DocumentRepository, private val documentMetadataRepository: DocumentMetadataRepository) : DocumentService {
    @Transactional
    override fun create(name: String, size:Long, contentType: String?, file: ByteArray): DocumentMetadataDTO{

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
        logger.info("Created Document {} - {}",savedMetadata.id, savedMetadata.name )
        return savedMetadata

    }

    override fun existsByName(name: String): Boolean {
      return  documentMetadataRepository.findByNameIgnoreCase(name) != null
    }

    override fun existsByNameExcludingMetadataID(name: String, metadataId: Long): Boolean {
        return documentMetadataRepository.findByNameIgnoreCaseAndMetadataIDNot(name, metadataId) != null
    }

    override fun getAllDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO>{
        return documentMetadataRepository.findAll(pageable).map { d-> d.toReducedDto() }
    }

    @Transactional
    override fun deleteDocument(metadataId: Long) {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DuplicateDocumentException("The document doesn't exist")
        }

        documentMetadataRepository.delete(document.get())
        logger.info("Deleted document {}", metadataId)
    }

    @Transactional
    override fun getDocumentContent(metadataId: Long): DocumentDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toDocumentDto()
    }

    @Transactional
    override fun getDocumentMetadataById(metadataId: Long): DocumentMetadataDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toMetadataDto()
    }

    @Transactional
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

        val docMetadata = documentMetadataOpt.get()
        val oldName = docMetadata.name
        docMetadata.document.content = file
        docMetadata.name = name
        docMetadata.contentType= contentType
        docMetadata.size = size
        docMetadata.creationTimestamp= LocalDateTime.now()

        val savedMetadata = documentMetadataRepository.save(docMetadata).toMetadataDto()
        logger.info("Edited Document {} - {} -> {}", savedMetadata.id, oldName, savedMetadata.name)
        return savedMetadata
    }

    companion object{
        val logger = LoggerFactory.getLogger(DocumentServiceImpl::class.java)
    }
}