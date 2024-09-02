package it.polito.wa2.g07.document_store.dtos

import it.polito.wa2.g07.document_store.entities.DocumentHistory

data class DocumentHistoryDTO(
    val id: Long,
    val versions: List<DocumentReducedMetadataDTO>
)

fun DocumentHistory.toHistoryDto(): DocumentHistoryDTO =
    DocumentHistoryDTO(
        this.id,
        this.documentMetadata
            .sortedByDescending { it.creationTimestamp }
            .map { it.toReducedDto() }
    )