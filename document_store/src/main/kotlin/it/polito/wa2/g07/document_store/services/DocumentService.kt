package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO


import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

interface DocumentService {

 fun create(name: String, size:Long, contentType: String?, file: ByteArray): DocumentMetadataDTO

 fun getAllDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO>
 fun getDocumentContent(metadataId:Long): DocumentDTO
 fun getDocumentMetadataById(metadataId:Long): DocumentMetadataDTO

 fun editDocument(metadataId:Long, name: String, size:Long, contentType: String?, file: ByteArray) : DocumentMetadataDTO

 fun deleteDocument(metadataId: Long)
}