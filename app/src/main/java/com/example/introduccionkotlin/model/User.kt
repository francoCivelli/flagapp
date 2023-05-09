package com.example.introduccionkotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey var id: String,
    var name: String,
    var email: String,
    var password: String,
    var phone: String,
    var birthDate: String
)
