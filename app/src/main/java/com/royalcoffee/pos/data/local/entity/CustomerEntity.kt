package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(
            value = ["phone_number"],
            unique = true
        ),
        Index(
            value = ["customer_name"]
        )
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "customer_id")
    val customerId: Long = 0,

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
