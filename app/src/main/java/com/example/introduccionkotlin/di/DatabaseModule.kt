package com.example.introduccionkotlin.di

import android.content.Context
import androidx.room.Room
import com.example.introduccionkotlin.database.AppDatabase
import com.example.introduccionkotlin.database.CountryDao
import com.example.introduccionkotlin.database.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my-app-db"
        ).build()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideCountryDao(appDatabase: AppDatabase): CountryDao {
        return appDatabase.countryDao()
    }
}