package it.polito.wa2.g07.document_store.dtos


import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime


data class DocumentDTO (
    val id :Long?,
    val size: Long,
    val contentType: String?,
    val name: String,
    var creationTimestamp: LocalDateTime,
    val content:ByteArray
)

fun DocumentMetadata.toDocumentDto(): DocumentDTO = DocumentDTO(id=this.metadataID,
                                                                contentType = this.contentType,
                                                                size = this.size,
                                                                name = this.name,
                                                                creationTimestamp = this.creationTimestamp,
                                                                content= this.document.content)
