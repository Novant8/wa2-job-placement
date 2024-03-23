package it.polito.wa2.g07.document_store.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.stereotype.Controller


@RestController
class DocumentController {

    @GetMapping("/API/documents/")
    fun index() {
        print("Test");
        //return "test"  //this is the name of the view
    }
    //@GetMapping("")
    //GET /API/documents/{metadatatId}/ -- details of docu {documentId} or fail if it does not exist

    //GET /API/documents/{metadatatId}/data/ -- byte content of document {metatadataId} or fail if it does not exist

    //POST /API/documents/ -- convert the request param into  DocumentMetadataDTO and store it in the DB, provided that a file with that name doesn’t already exist.
}

