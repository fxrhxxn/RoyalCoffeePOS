package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.CategoryDao
import com.royalcoffee.pos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for category-related business logic.
 *
 * The UI/ViewModel should use this repository instead of
 * calling CategoryDao directly.
 */
class CategoryRepository(
    private val categoryDao: CategoryDao
) {

    /**
     * Reactive category list used by:
     *
     * - Inventory filters
     * - Product form dropdown
     * - Manage Categories screen
     */
    val allCategories: Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()


    /**
     * Adds a new category.
     *
     * Returns Result.success when successful.
     *
     * Returns Result.failure when:
     * - Name is empty
     * - Category already exists
     * - Database insert fails
     */
    suspend fun addCategory(
        categoryName: String
    ): Result<Unit> {

        val cleanedName =
            categoryName.trim()

        if (cleanedName.isBlank()) {

            return Result.failure(
                IllegalArgumentException(
                    "Category name cannot be empty."
                )
            )
        }

        /*
         * Case-insensitive duplicate check.
         */
        if (
            categoryDao.categoryExists(
                cleanedName
            ) > 0
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "This category already exists."
                )
            )
        }

        return try {

            categoryDao.insertCategory(
                CategoryEntity(
                    categoryName = cleanedName
                )
            )

            Result.success(Unit)

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }


    /**
     * Attempts to remove a category.
     *
     * Categories currently used by products cannot
     * be removed.
     */
    suspend fun removeCategory(
        category: CategoryEntity
    ): Result<Unit> {

        val productCount =
            categoryDao.getProductCountForCategory(
                category.categoryName
            )

        if (productCount > 0) {

            return Result.failure(
                IllegalStateException(
                    "This category is currently used by " +
                            "$productCount product(s). " +
                            "Reassign those products before removing it."
                )
            )
        }

        return try {

            categoryDao.deleteCategory(
                category
            )

            Result.success(Unit)

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }
}