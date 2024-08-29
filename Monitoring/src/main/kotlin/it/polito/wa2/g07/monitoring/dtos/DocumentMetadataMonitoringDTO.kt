package it.polito.wa2.g07.monitoring.dtos


import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class DocumentMetadataMonitoringDTO (
    @JsonProperty("id") val id :Long,
    @JsonProperty("size")  val size: Long,
    @JsonProperty("contentType")  val contentType: String?,
    @JsonProperty("name")  val name: String,
    @JsonProperty("creationTimestamp")  var creationTimestamp: LocalDateTime,
    ){

}

