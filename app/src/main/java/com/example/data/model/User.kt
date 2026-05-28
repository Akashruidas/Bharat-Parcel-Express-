package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val name: String,
    val passwordHash: String = "",
    val isGoogleUser: Boolean = false,
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
