package it.polito.wa2.g07.monitoring.dtos

import com.fasterxml.jackson.annotation.JsonProperty


data class JobOfferMonitoringDTO (
    @JsonProperty("id")  val id:Long,
    @JsonProperty("description")  val description : String,
    @JsonProperty("customer") val customer: String,
    @JsonProperty("requiredSkills") val requiredSkills: MutableSet<String>,
    @JsonProperty("duration") val duration: Long,
    @JsonProperty("offerStatus") val offerStatus: String,
    @JsonProperty("notes") val notes: String?,
    @JsonProperty("professional") val professional: String?,    //during the lifecycle of a joboffer professional can be empty
    @JsonProperty("value")  val value:Double?                   //when a professional is not present value cannot be computed
)

