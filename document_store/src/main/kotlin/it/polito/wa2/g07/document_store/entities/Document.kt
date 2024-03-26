package it.polito.wa2.g07.document_store.entities


import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


@Entity
class Document
{
        @Id
        @GeneratedValue
        var documentID: Long= 0

       /* @Column(name= "content")*/
        lateinit var content: ByteArray
                /*@Column(name = "metadata_id")
       var metadataId: Long,*/

                /*@OneToOne(mappedBy = "document", cascade = [CascadeType.ALL])
                var metadata: DocumentMetadata*/
        /*fun toDocumentEntity (docDTO : DocumentDTO) : Document{
                
                return Document(docDTO.id, docDTO.content, )
        }*/
}
