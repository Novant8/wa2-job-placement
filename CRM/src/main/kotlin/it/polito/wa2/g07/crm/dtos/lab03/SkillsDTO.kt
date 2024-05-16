package it.polito.wa2.g07.crm.dtos.lab03

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class SkillsDTO(
    @field:ArraySchema(arraySchema = Schema(example = "[ \"Proficient in Kotlin\", \"Can work autonomously\" ]"))
    val skills: Set<String>,
)
