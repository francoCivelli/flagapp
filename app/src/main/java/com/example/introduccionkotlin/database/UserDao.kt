package com.example.introduccionkotlin.database

import androidx.room.*
import com.example.introduccionkotlin.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getByEmail(email: String): User

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun checkUserExists(email: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

}