package com.bitmart.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.bitmart.data.local.entity.ContractEntity

@Dao
interface ContractDao {
    @Query("SELECT * FROM contract LIMIT :limitStart,:limitEnd")
    suspend fun getContractList(limitStart: Int, limitEnd: Int): List<ContractEntity>
}