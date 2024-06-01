package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.PositiveOrZero

data class PriorityDTO(
    @field:PositiveOrZero(message = "Priority must be positive or zero")
    @field:Schema(minimum = "0")
    val priority: Int
)
