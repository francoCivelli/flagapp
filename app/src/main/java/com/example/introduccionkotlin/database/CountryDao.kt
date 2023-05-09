package com.example.introduccionkotlin.database

import androidx.room.*
import com.example.introduccionkotlin.model.Country

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries")
    fun getAll(): List<Country>

    @Query("SELECT * FROM countries WHERE countryName = :name")
    fun getByName(name: String): Country

    @Query("SELECT COUNT(*) FROM countries WHERE countryName = :name")
    fun checkCountryExists(name: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(country: Country)

    @Update
    fun update(country: Country)

    @Delete
    fun delete(country: Country)
}