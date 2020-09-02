package ru.loginov.chemistryapplication.api.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChemistryMetric @JsonCreator constructor(
        @JsonProperty("tag") val tag: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("collector") val isCollector: Boolean
)