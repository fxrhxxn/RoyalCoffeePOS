package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: InventoryHistoryEntity)

    /**
     * Returns all history records, newest first.
     */
    @Query("SELECT * FROM inventory_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<InventoryHistoryEntity>>

    /**
     * Returns history for a specific product.
     */
    @Query("SELECT * FROM inventory_history WHERE product_id = :productId ORDER BY timestamp DESC")
    fun getHistoryByProduct(productId: Int): Flow<List<InventoryHistoryEntity>>

    /**
     * Deletes all history. Mainly for testing.
     */
    @Query("DELETE FROM inventory_history")
    suspend fun deleteAllHistory()
}
