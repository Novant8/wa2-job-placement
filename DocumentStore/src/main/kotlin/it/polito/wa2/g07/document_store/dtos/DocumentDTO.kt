package it.polito.wa2.g07.document_store.dtos


import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime


data class DocumentDTO (
    val historyId: Long,
    val versionId:Long?,
    val size: Long,
    val contentType: String?,
    val name: String,
    val creationTimestamp: LocalDateTime,
    val content:ByteArray
)

fun DocumentMetadata.toDocumentDto(): DocumentDTO = DocumentDTO(historyId=this.documentHistory.id,
                                                                versionId=this.metadataID,
                                                                contentType = this.contentType,
                                                                size = this.size,
                                                                name = this.name,
                                                                creationTimestamp = this.creationTimestamp,
                                                                content= this.document.content)
