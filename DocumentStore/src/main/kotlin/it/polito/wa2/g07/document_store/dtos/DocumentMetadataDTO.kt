package it.polito.wa2.g07.document_store.dtos


import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime

data class DocumentMetadataDTO (
    var historyId:Long,
    val versionId:Long,
    val size: Long,
    val contentType: String?,
    val name: String,
    var creationTimestamp: LocalDateTime,
)

fun DocumentMetadata.toMetadataDto(): DocumentMetadataDTO =
    DocumentMetadataDTO(this.documentHistory.id,
                        this.metadataID,
                        this.size,
                        this.contentType,
                        this.name,
                        this.creationTimestamp
    )