package it.polito.wa2.g07.document_store.services

import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata


class DocumentDTO(var id : Long ?, var content : ByteArray/*, var metadata: DocumentMetadata*/) {

    fun toDocumentDTO (document: Document)  : DocumentDTO {

        val d = DocumentDTO(document.documentID, document.content);
        return d;

    }
}