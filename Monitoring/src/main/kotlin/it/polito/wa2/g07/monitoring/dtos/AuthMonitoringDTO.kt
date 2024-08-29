package it.polito.wa2.g07.monitoring.dtos

import com.fasterxml.jackson.annotation.JsonProperty



data class AuthMonitoringDTO(
    @JsonProperty("userId") val userId: String,
    @JsonProperty("username") val name: String?,
)