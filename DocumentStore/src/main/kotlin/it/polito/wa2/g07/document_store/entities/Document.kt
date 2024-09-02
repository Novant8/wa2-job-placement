package it.polito.wa2.g07.document_store.entities


import jakarta.persistence.*


@Entity
class Document(
        var content: ByteArray
)
{
        @Id
        @GeneratedValue
        var documentID: Long= 0
}
