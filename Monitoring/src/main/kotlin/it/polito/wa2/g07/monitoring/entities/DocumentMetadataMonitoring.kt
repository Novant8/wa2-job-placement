package it.polito.wa2.g07.monitoring.entities



import jakarta.persistence.*
import java.time.LocalDateTime

@Entity

class DocumentMetadataMonitoring(

    val metadataID: Long = 0,
    val versionId: Long = 0,
    var name: String?= null,
    var size: Long = 0,
    var contentType: String? = null,
    var creationTimestamp: LocalDateTime? = null
){
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long =0
}