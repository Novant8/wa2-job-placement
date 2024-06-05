package it.polito.wa2.g07.document_store.controllers


import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO
import it.polito.wa2.g07.document_store.exceptions.InvalidBodyException
import it.polito.wa2.g07.document_store.services.DocumentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/API/documents")
@EnableMethodSecurity(prePostEnabled = true)
class DocumentController(private val documentService: DocumentService) {

    @GetMapping("/", "")
    fun getDocuments(pageable: Pageable): Page<DocumentReducedMetadataDTO> {
       return  documentService.getAllDocuments(pageable)
    }

    @GetMapping("/{metadataId}/data","/{metadataId}/data/")
    fun getDocumentContent(@PathVariable("metadataId") metadataId: Long): ResponseEntity<ByteArray> {
        val document = documentService.getDocumentContent(metadataId)
        val documentMetadata = documentService.getDocumentMetadataById(metadataId)
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, documentMetadata.contentType)
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"${documentMetadata.name}\"")

        return ResponseEntity<ByteArray>(document.content, headers, HttpStatus.OK)
    }

    @GetMapping("/{metadataId}","/{metadataId}/")
    fun getDocumentMetadataById(@PathVariable("metadataId") metadataId: Long,): DocumentMetadataDTO  {

        return documentService.getDocumentMetadataById(metadataId)
    }
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/","",consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    fun saveDocument(@RequestParam("document") document: MultipartFile): DocumentMetadataDTO {

        if(document.originalFilename === null || document.originalFilename!!.isEmpty()) {
            throw InvalidBodyException("The document does not have a name")
        }

        return  documentService.create(document.originalFilename!!, document.size, document.contentType, document.bytes)
    }

    @PutMapping("/{metadataId}","/{metadataId}/")
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    fun putDocuments(@PathVariable("metadataId") metadataId: Long,
                     @RequestParam("document") document: MultipartFile) : DocumentMetadataDTO {

        if(document.originalFilename === null || document.originalFilename!!.isEmpty()) {
            throw InvalidBodyException("The document does not have a name")
        }

        return  documentService.editDocument(
            metadataId,
            document.originalFilename!!,
            document.size,
            document.contentType,
            document.bytes
        )
    }

    @DeleteMapping("/{metadataId}","/{metadataId}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('operator', 'manager')")
    fun deleteDocument(@PathVariable("metadataId") metadataId: Long){
       documentService.deleteDocument(metadataId)

    }

}

