package it.polito.wa2.g07.document_store.entities


import jakarta.persistence.*


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

       /* @OneToOne(mappedBy = "document", cascade = [CascadeType.ALL])
        lateinit var metadata: DocumentMetadata*/
        /*fun toDocumentEntity (docDTO : DocumentDTO) : Document{
                
                return Document(docDTO.id, docDTO.content, )
        }*/
}
