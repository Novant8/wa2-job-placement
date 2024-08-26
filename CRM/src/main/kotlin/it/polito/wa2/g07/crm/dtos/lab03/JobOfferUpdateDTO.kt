package it.polito.wa2.g07.crm.dtos.lab03



data class JobOfferUpdateDTO (
    val description : String,
    val duration: Long,
    val notes: String?,
    val requiredSkills: MutableSet<String>,
)