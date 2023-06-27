package com.example.introduccionkotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.introduccionkotlin.util.ListConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "countries")
@TypeConverters(ListConverter::class)
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
    @SerializedName("latlng")
    val latlng: List<Double>,

    var descripcion: String?,
    var selectedMap: Boolean
) : Serializable