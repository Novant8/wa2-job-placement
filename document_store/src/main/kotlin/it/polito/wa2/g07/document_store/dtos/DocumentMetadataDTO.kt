package it.polito.wa2.g07.document_store.dtos

import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime

data class DocumentMetadataDTO (
    val id :Long,
    val size: Long,
    val contentType: String?,
    val name: String,
    var creationTimestamp: LocalDateTime,
    //val document : DocumentDTO
    )

fun DocumentMetadata.toMetadataDto(): DocumentMetadataDTO =
    DocumentMetadataDTO(this.metadataID,
                        this.size,
                        this.contentType,
                        this.name,
                        this.creationTimestamp )