package it.polito.wa2.g07.document_store.controllers


import it.polito.wa2.g07.document_store.dtos.DocumentMetadataDTO
import it.polito.wa2.g07.document_store.dtos.DocumentReducedMetadataDTO
import it.polito.wa2.g07.document_store.exceptions.DuplicateDocumentException
import it.polito.wa2.g07.document_store.exceptions.InvalidBodyException
import it.polito.wa2.g07.document_store.services.DocumentService


import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/API/documents")
class DocumentController(private val documentService: DocumentService) {

    @GetMapping("/", "")
    fun getDocuments(): List<DocumentReducedMetadataDTO> {
       return  documentService.getAllDocuments()
    }
   // GET /API/documents/{metadatatId}/data/ -- byte content of document {metatadataId} or fail if it does not exist
    @GetMapping("/{metadataId}/data")
    fun getDocumentContent(@PathVariable("metadataId") metadataId: Long): ResponseEntity<ByteArray> {
        // throw  handleDocumentNotFound()
        val document = documentService.getDocumentContent(metadataId)
        val documentMetadata = documentService.getDocumentMetadataById(metadataId)
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, documentMetadata.contentType)

        return ResponseEntity<ByteArray>(document.content, headers, HttpStatus.OK)
    }
    //GET /API/documents/{metadatatId}/ -- details of docu {documentId} or fail if it does not exist
    @GetMapping("/{metadataId}")
    fun getDocumentMetadataById(@PathVariable("metadataId") metadataId: Long,): DocumentMetadataDTO  {
        // throw  handleDocumentNotFound()
        return documentService.getDocumentMetadataById(metadataId)
    }
    @PostMapping("/","",consumes = ["multipart/form-data"])
    fun saveDocument(@RequestParam("document") document: MultipartFile): DocumentMetadataDTO {

        if(document.originalFilename === null) {
            throw InvalidBodyException("The document does not have a name")
        }

        if (documentService.existsByName(document.originalFilename!!) ){
            throw DuplicateDocumentException("A document with the same name already exists")
        }

        return  documentService.create(document.originalFilename!!, document.size, document.contentType, document.bytes)
    }

    @DeleteMapping("/{metadataId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDocument(@PathVariable("metadataId") metadataId: Long){
       documentService.deleteDocument(metadataId)

    }
    //@GetMapping("")
    //GET /API/documents/{metadatatId}/ -- details of docu {documentId} or fail if it does not exist

    //GET /API/documents/{metadatatId}/data/ -- byte content of document {metatadataId} or fail if it does not exist

    //POST /API/documents/ -- convert the request param into  DocumentMetadataDTO and store it in the DB, provided that a file with that name doesn’t already exist.
}

