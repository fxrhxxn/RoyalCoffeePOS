package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.royalcoffee.pos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for product categories.
 */
@Dao
interface CategoryDao {
    /**
     * Returns all categories alphabetically.
     * Flow means screens observing this list automatically.
     * update whenever categories are added or removed.
     */
    @Query(
        """
        SELECT *
        FROM categories
        ORDER BY category_name COLLATE NOCASE ASC
        """
    )
    fun getAllCategories(): Flow<List<CategoryEntity>>
    /**
     * Insert a category.
     *
     * ABORT prevents an exact duplicate from replacing
     * an existing category.
     */
    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insertCategory(
        category: CategoryEntity
    ): Long


    /**
     * Remove an existing category.
     */
    @Delete
    suspend fun deleteCategory(
        category: CategoryEntity
    )


    /**
     * Check whether a category already exists.
     *
     * COLLATE NOCASE means:
     *
     * Coffee
     * coffee
     * COFFEE
     *
     * are treated as the same category for validation.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM categories
        WHERE category_name = :categoryName COLLATE NOCASE
        """
    )
    suspend fun categoryExists(
        categoryName: String
    ): Int


    /**
     * Determines how many products currently use a category.
     *
     * We use this before deleting a category.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM products
        WHERE category = :categoryName COLLATE NOCASE
        """
    )
    suspend fun getProductCountForCategory(
        categoryName: String
    ): Int
}