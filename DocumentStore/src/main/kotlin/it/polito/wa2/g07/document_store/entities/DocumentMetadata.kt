package it.polito.wa2.g07.document_store.entities



import jakarta.persistence.*
import java.time.LocalDateTime

@Entity

class DocumentMetadata(
    var name: String,
    var contentType: String? = null,
    var size: Long = 0,
    var creationTimestamp: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue
    var metadataID: Long = 0

    @OneToOne
    lateinit var document: Document

    @ManyToOne
    lateinit var documentHistory: DocumentHistory
}