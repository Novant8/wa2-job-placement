package it.polito.wa2.g07.monitoring.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class MessageMonitoringDTO(
    @JsonProperty("id") var id: String,
    @JsonProperty("channel") var channel: String?,
    @JsonProperty("priority") var priority: Int?,
    @JsonProperty("creationTimestamp") var creationTimestamp: LocalDateTime?,
    @JsonProperty("status") var status: String,
    @JsonProperty("statusTimestamp") var statusTimestamp: LocalDateTime?,
)
