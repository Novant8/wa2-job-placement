package it.polito.wa2.g07.document_store.dtos

import it.polito.wa2.g07.document_store.entities.Document
import it.polito.wa2.g07.document_store.entities.DocumentMetadata
import java.time.LocalDateTime
import javax.print.Doc

/*
class DocumentDTO(var id : Long ?, var content : ByteArray/*, var metadata: DocumentMetadata*/) {

    fun toDocumentDTO (document: Document)  : DocumentDTO {

        val d = DocumentDTO(document.documentID, document.content);
        return d;

    }
}

*/
data class DocumentDTO (
    val id :Long?,
    val content: ByteArray,
)

fun Document.toDto(): DocumentDTO = DocumentDTO(id=this.documentID,
                                                content= this.content)
