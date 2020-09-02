package ru.loginov.chemistryapplication.api.entity

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChemistryUnit @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("moreChemistryUnit") val moreChemistryUnit: Int? = null,
        @JsonProperty("lessChemistryUnit") val lessChemistryUnit: Int? = null,
        @JsonProperty("id") val id: Long? = null,) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Long::class.java.classLoader) as? Long) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeValue(moreChemistryUnit)
        parcel.writeValue(lessChemistryUnit)
        parcel.writeValue(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChemistryUnit> {
        override fun createFromParcel(parcel: Parcel): ChemistryUnit {
            return ChemistryUnit(parcel)
        }

        override fun newArray(size: Int): Array<ChemistryUnit?> {
            return arrayOfNulls(size)
        }
    }

}