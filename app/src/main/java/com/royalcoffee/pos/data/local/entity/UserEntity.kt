package com.royalcoffee.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.royalcoffee.pos.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val username: String,
    val hashedPin: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)
