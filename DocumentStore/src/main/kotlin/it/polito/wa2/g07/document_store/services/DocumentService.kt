package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.DocumentHistoryDTO
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO


import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

interface DocumentService {

 fun create(name: String, size:Long, contentType: String?, file: ByteArray, ownerUserId: String?): DocumentMetadataDTO

 fun getAllDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO>
 fun getDocumentContent(historyId:Long): DocumentDTO
 fun getDocumentMetadata(historyId:Long): DocumentMetadataDTO
 fun getDocumentVersionContent(historyId: Long, metadataId: Long): DocumentDTO
 fun getDocumentVersionMetadata(historyId:Long, metadataId: Long): DocumentMetadataDTO
 fun getDocumentHistory(historyId:Long): DocumentHistoryDTO

 fun editDocument(historyId:Long, name: String, size:Long, contentType: String?, file: ByteArray) : DocumentMetadataDTO

 fun deleteDocumentHistory(historyId: Long)
 fun deleteDocumentVersion(historyId:Long, metadataId:Long)
}