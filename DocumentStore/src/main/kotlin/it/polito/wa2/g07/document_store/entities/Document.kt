package it.polito.wa2.g07.document_store.entities


import jakarta.persistence.*


@Entity
class Document
{
        @Id
        @GeneratedValue
        var documentID: Long= 0


        lateinit var content: ByteArray

}
