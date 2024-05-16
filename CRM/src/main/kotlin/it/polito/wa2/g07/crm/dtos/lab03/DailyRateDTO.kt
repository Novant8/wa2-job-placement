package it.polito.wa2.g07.crm.dtos.lab03

import io.swagger.v3.oas.annotations.media.Schema

data class DailyRateDTO(
    @field:Schema(example = "150")
    val dailyRate: Double
)
