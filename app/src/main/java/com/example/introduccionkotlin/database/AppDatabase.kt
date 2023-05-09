package com.example.introduccionkotlin.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.model.User

@Database(entities = [User::class, Country::class],  version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun countryDao(): CountryDao
}