package it.polito.wa2.g07.document_store.controllers


import it.polito.wa2.g07.document_store.dtos.DocumentHistoryDTO
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
import org.springframework.security.core.Authentication
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

    @GetMapping("/{historyId}/data","/{historyId}/data/")
    fun getDocumentContent(@PathVariable("historyId") historyId: Long): ResponseEntity<ByteArray> {
        val document = documentService.getDocumentContent(historyId)
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, document.contentType)
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"${document.name}\"")

        return ResponseEntity<ByteArray>(document.content, headers, HttpStatus.OK)
    }

    @GetMapping("/{historyId}","/{historyId}/")
    fun getDocumentMetadata(@PathVariable("historyId") historyId: Long): DocumentMetadataDTO  {
        return documentService.getDocumentMetadata(historyId)
    }

    @GetMapping("/{historyId}/history","/{historyId}/history")
    fun getDocumentHistory(@PathVariable("historyId") historyId: Long): DocumentHistoryDTO {
        return documentService.getDocumentHistory(historyId)
    }

    @GetMapping("/{historyId}/version/{metadataId}/data")
    fun getDocumentVersionContent(
        @PathVariable("historyId") historyId: Long,
        @PathVariable("metadataId") metadataId: Long
    ): ResponseEntity<ByteArray> {
        val document = documentService.getDocumentVersionContent(historyId, metadataId)
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, document.contentType)
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"${document.name}\"")

        return ResponseEntity<ByteArray>(document.content, headers, HttpStatus.OK)
    }

    @GetMapping("/{historyId}/version/{metadataId}")
    fun getDocumentVersionMetadata(
        @PathVariable("historyId") historyId: Long,
        @PathVariable("metadataId") metadataId: Long
    ): DocumentMetadataDTO  {
        return documentService.getDocumentVersionMetadata(historyId, metadataId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/","",consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('operator', 'manager', 'professional','customer')")
    fun saveDocument(
        @RequestPart("document") document: MultipartFile,
        authentication: Authentication?
    ): DocumentMetadataDTO {

        if(document.originalFilename.isNullOrEmpty()) {
            throw InvalidBodyException("The document does not have a name")
        }

        return  documentService.create(document.originalFilename!!, document.size, document.contentType, document.bytes, authentication?.name)
    }

    @PutMapping("/{historyId}","/{historyId}/")
    // Authorize only if the authenticated user is an operator/manager, or if the user is trying to modify their own document.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @documentHistoryRepository.findById(#historyId).orElse(null)?.ownerUserId == authentication.name")
    fun putDocuments(@PathVariable("historyId") historyId: Long,
                     @RequestPart("document") document: MultipartFile) : DocumentMetadataDTO {

        if(document.originalFilename === null || document.originalFilename!!.isEmpty()) {
            throw InvalidBodyException("The document does not have a name")
        }

        return  documentService.editDocument(
            historyId,
            document.originalFilename!!,
            document.size,
            document.contentType,
            document.bytes
        )
    }

    @DeleteMapping("/{historyId}","/{historyId}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Authorize only if the authenticated user is an operator/manager, or if the user is trying to modify their own document.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @documentHistoryRepository.findById(#historyId).orElse(null)?.ownerUserId == authentication.name")
    fun deleteHistory(@PathVariable("historyId") historyId: Long){
       documentService.deleteDocumentHistory(historyId)
    }

    @DeleteMapping("/{historyId}/version/{metadataId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Authorize only if the authenticated user is an operator/manager, or if the user is trying to modify their own document.
    @PreAuthorize("hasAnyRole('operator', 'manager') or @documentHistoryRepository.findById(#historyId).orElse(null)?.ownerUserId == authentication.name")
    fun deleteVersion(
        @PathVariable("historyId") historyId: Long,
        @PathVariable("metadataId") metadataId: Long
    ){
        documentService.deleteDocumentVersion(historyId, metadataId)
    }

}

