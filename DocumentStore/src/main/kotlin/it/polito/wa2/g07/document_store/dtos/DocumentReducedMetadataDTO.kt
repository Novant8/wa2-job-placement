package it.polito.wa2.g07.document_store.dtos


import it.polito.wa2.g07.document_store.entities.DocumentMetadata


data class DocumentReducedMetadataDTO (
    val historyId:Long,
    val versionId:Long,
    val name: String
)

fun DocumentMetadata.toReducedDto(): DocumentReducedMetadataDTO =
    DocumentReducedMetadataDTO(this.documentHistory.id, this.metadataID,  this.name)