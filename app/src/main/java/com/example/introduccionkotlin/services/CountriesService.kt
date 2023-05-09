package com.example.introduccionkotlin.services

import com.example.introduccionkotlin.model.Country
import retrofit2.Response
import retrofit2.http.GET

interface CountriesService {
    @GET("DevTides/countries/master/countriesV2.json")
    suspend fun getCountries() : Response<List<Country>>
}