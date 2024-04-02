package it.polito.wa2.g07.document_store.dtos

import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime

data class DocumentReducedMetadataDTO (
    val id :Long,
    val name: String
)

fun DocumentMetadata.toReducedDto(): DocumentReducedMetadataDTO =
    DocumentReducedMetadataDTO(this.metadataID,  this.name)