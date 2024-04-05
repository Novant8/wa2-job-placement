package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.DocumentDTO
import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO



interface DocumentService {
 /*fun getDocuments(): DocumentDTO?*/
 fun create(name: String, size:Long, contentType: String?, file: ByteArray): DocumentMetadataDTO

 fun existsByName(name: String): Boolean

 fun getAllDocuments():  List<DocumentReducedMetadataDTO>
 fun getDocumentContent(metadataId:Long): DocumentDTO
 fun getDocumentMetadataById(metadataId:Long): DocumentMetadataDTO

 fun deleteDocument(metadataId: Long)


}