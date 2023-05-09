package com.example.introduccionkotlin.repository

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.introduccionkotlin.database.UserDao
import com.example.introduccionkotlin.model.User
import com.example.introduccionkotlin.util.NetworkRequestHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UsersRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun getById(id: String) =
        withContext(Dispatchers.IO) {
            userDao.getById(id)
        }

    suspend fun getByEmail(email: String) =
        withContext(Dispatchers.IO) {
            userDao.getByEmail(email)
        }

    suspend fun checkUserExists(email: String) =
        withContext(Dispatchers.IO) {
            userDao.checkUserExists(email)
        }

    suspend fun insert(user: User) =
        withContext(Dispatchers.IO) {
            userDao.insert(user)
        }

    suspend fun update(user: User) =
        withContext(Dispatchers.IO) {
            userDao.update(user)
        }
}