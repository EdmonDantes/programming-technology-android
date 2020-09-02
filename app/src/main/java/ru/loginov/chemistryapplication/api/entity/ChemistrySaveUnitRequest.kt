package ru.loginov.chemistryapplication.api.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class ChemistryScaleUnit(@JsonProperty("id") val id: Long, @JsonProperty("scale") val scale: Double)

data class ChemistrySaveUnitRequest(@JsonProperty("name") val name: String, @JsonProperty("less") val less: ChemistryScaleUnit?, @JsonProperty("more") val more: ChemistryScaleUnit?)