package it.polito.wa2.g07.document_store.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "document_metadata")
class DocumentMetadata{
        @Id
        @GeneratedValue
        var metadataID: Long = 0

        @OneToOne
       lateinit var document: Document

        @Column(name = "name")
       lateinit var name: String

        @Column(name = "size")
         var size: Long = 0

        @Column(name = "content_type")
        lateinit var contentType: String

        @Column(name = "creation_timestamp")
        lateinit var creationTimestamp: LocalDateTime
}