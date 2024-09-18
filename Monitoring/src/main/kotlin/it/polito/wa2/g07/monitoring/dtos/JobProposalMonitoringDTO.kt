package it.polito.wa2.g07.monitoring.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class JobProposalMonitoringDTO (
    @JsonProperty("id")  val id:Long,
    @JsonProperty("status") val status :String,
    @JsonProperty("documentId")val documentId : Long?,
    @JsonProperty("professionalSignedContract")val professionalSignedContract : Long?,
    @JsonProperty("customer")val customer : String,
    @JsonProperty("professional")val professional : String,
    @JsonProperty("jobOffer")val jobOffer: String
)
