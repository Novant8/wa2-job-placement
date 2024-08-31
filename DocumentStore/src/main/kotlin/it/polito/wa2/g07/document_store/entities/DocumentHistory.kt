package it.polito.wa2.g07.document_store.entities

import jakarta.persistence.*

@Entity
class DocumentHistory(
    var ownerUserId: String? = null
) {
    @Id
    @GeneratedValue
    var id: Long = 0

    @OneToMany(mappedBy = "documentHistory", cascade = [CascadeType.REMOVE])
    var documentMetadata: MutableSet<DocumentMetadata> = mutableSetOf()

    fun addDocumentMetadata(documentMetadata: DocumentMetadata) {
        this.documentMetadata.add(documentMetadata)
        documentMetadata.documentHistory = this
    }
}