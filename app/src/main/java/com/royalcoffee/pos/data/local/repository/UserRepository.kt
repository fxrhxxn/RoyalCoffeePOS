package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.UserDao
import com.royalcoffee.pos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    
    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)
    
    suspend fun getUserCount(): Int = userDao.getUserCount()
    
    suspend fun getFirstAdmin(): UserEntity? = userDao.getFirstAdmin()
    
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}
