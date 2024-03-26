package it.polito.wa2.g07.document_store.services


import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO



interface DocumentService {
 /*fun getDocuments(): DocumentDTO?*/
 fun create(name: String, size:Long, contentType : String, file: ByteArray  ): DocumentMetadataDTO

 fun existsByName(name: String): Boolean
}