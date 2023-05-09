package com.example.introduccionkotlin.repository

import androidx.room.*
import com.example.introduccionkotlin.database.CountryDao
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.services.CountriesService
import com.example.introduccionkotlin.util.NetworkRequestHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountriesRepository @Inject constructor(private val service : CountriesService,
                                                private val countryDao: CountryDao) {
    suspend fun getCountries() =
        withContext(Dispatchers.IO) {
            NetworkRequestHandler.safeServiceCall {
                service.getCountries()
            }
        }

    suspend fun getAll() =
        withContext(Dispatchers.IO){
            countryDao.getAll()
        }

    suspend fun getByName(name: String) =
        withContext(Dispatchers.IO){
            countryDao.getByName(name)
        }

    suspend fun checkCountryExists(name: String?) =
        withContext(Dispatchers.IO){
            countryDao.checkCountryExists(name)
        }

    suspend fun insert(country: Country) =
        withContext(Dispatchers.IO){
            countryDao.insert(country)
        }

    suspend fun update(country: Country) =
        withContext(Dispatchers.IO){
            countryDao.update(country)
        }

    suspend fun delete(country: Country) =
        withContext(Dispatchers.IO){
            countryDao.delete(country)
        }
}