package it.polito.wa2.g07.document_store.dtos


import org.springframework.web.multipart.MultipartFile


data class CreateDocumentMetadataDTO (
    val size: Long,
    val contentType: String,
    val document : MultipartFile,
    val name: String,
)

