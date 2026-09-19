package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business_info WHERE id = 1 LIMIT 1")
    fun getBusinessInfo(): Flow<BusinessEntity?>

    @Query("SELECT * FROM business_info WHERE id = 1 LIMIT 1")
    suspend fun getBusinessInfoSync(): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBusinessInfo(business: BusinessEntity)
}
