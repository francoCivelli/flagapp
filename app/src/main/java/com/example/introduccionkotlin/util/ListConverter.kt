package com.example.introduccionkotlin.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverter {
    @TypeConverter
    fun fromList(list: List<Double>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<Double> {
        val type = object : TypeToken<List<Double>>() {}.type
        return Gson().fromJson(json, type)
    }
}