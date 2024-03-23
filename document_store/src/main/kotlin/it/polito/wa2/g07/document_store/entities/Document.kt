package it.polito.wa2.g07.document_store.entities

import it.polito.wa2.g07.document_store.services.DocumentDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class Document(
        @Id
        @GeneratedValue
        var documentID: Long?,

        @Column(name= "content")
        var content: ByteArray,

        @Column(name = "metadata_id")
        var metadataId: Long,

        @OneToOne(mappedBy = "document", cascade = [CascadeType.ALL])
        var metadata: DocumentMetadata
) {
        /*fun toDocumentEntity (docDTO : DocumentDTO) : Document{
                
                return Document(docDTO.id, docDTO.content, )
        }*/
}
