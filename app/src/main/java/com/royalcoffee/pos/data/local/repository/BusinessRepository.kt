package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.BusinessDao
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val businessDao: BusinessDao) {
    fun getBusinessInfo(): Flow<BusinessEntity?> = businessDao.getBusinessInfo()
    
    suspend fun getBusinessInfoSync(): BusinessEntity? = businessDao.getBusinessInfoSync()
    
    suspend fun updateBusinessInfo(business: BusinessEntity) = businessDao.updateBusinessInfo(business)
}
