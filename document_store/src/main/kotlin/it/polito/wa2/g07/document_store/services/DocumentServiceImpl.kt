package it.polito.wa2.g07.document_store.services

import it.polito.wa2.g07.document_store.controllers.ProblemDetailsHandler
import it.polito.wa2.g07.document_store.dtos.*
import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import it.polito.wa2.g07.document_store.exceptions.DocumentNotFoundException
import it.polito.wa2.g07.document_store.repositories.DocumentMetadataRepository
import it.polito.wa2.g07.document_store.repositories.DocumentRepository
import jakarta.transaction.Transactional
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

        return documentMetadataRepository.save(docMetadata).toMetadataDto()
    }

    override fun existsByName(name: String): Boolean {
      return  documentMetadataRepository.findByNameIgnoreCase(name) != null
    }
    override fun getAllDocuments(): List<DocumentReducedMetadataDTO>{
       return  documentMetadataRepository.findAll().map { d -> d.toReducedDto() }
    }

    override fun getDocumentContent(metadataId: Long): DocumentDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toDocumentDto();
    }

    override fun getDocumentMetadataById(metadataId: Long): DocumentMetadataDTO {
        val document = documentMetadataRepository.findById(metadataId)
        if (!document.isPresent()) {
            throw DocumentNotFoundException("The document doesn't exist")
        }
        return document.get().toMetadataDto();
    }
}