package it.polito.wa2.g07.document_store.dtos

import it.polito.wa2.g07.document_store.entities.Document


class DocumentDTO(var id : Long ?, var content : ByteArray/*, var metadata: DocumentMetadata*/) {

    fun toDocumentDTO (document: Document)  : DocumentDTO {

        val d = DocumentDTO(document.documentID, document.content);
        return d;

    }
}