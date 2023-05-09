package com.example.introduccionkotlin.util

import com.example.introduccionkotlin.model.Country

object SerializableCountryFactory {
    fun createCountry(): Country = Country("", "", "", "", 0, "", "")
}
