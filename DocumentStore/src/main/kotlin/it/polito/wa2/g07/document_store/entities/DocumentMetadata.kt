package it.polito.wa2.g07.document_store.entities



import jakarta.persistence.*
import java.time.LocalDateTime

@Entity

class DocumentMetadata{
        @Id
        @GeneratedValue
        var metadataID: Long = 0

        @OneToOne
       lateinit var document: Document


       lateinit var name: String


         var size: Long = 0


        var contentType: String? = null


        lateinit var creationTimestamp: LocalDateTime



}