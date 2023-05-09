package com.example.introduccionkotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "countries")
data class Country (
    @PrimaryKey var id: String,
    @SerializedName("name")
    val countryName: String?,
    @SerializedName("capital")
    val capital: String?,
    @SerializedName("flagPNG")
    val flag: String?,
    @SerializedName("population")
    val population: Int,
    @SerializedName("region")
    val region: String,

    var descripcion: String?
) : Serializable