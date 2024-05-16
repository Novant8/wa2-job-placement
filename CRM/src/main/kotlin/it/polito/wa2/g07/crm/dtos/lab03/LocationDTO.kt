package it.polito.wa2.g07.crm.dtos.lab03

import io.swagger.v3.oas.annotations.media.Schema

data class LocationDTO(
    @field:Schema(example = "New York")
    val location: String
)
