package com.royalcoffee.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_info")
data class BusinessEntity(
    @PrimaryKey
    val id: Int = 1, // Only one business profile allowed
    val businessName: String,
    val address: String,
    val phoneNumber: String,
    val brn: String,
    val vat: String,
    val country: String,
    val email: String,
    val updatedAt: Long = System.currentTimeMillis()
)
