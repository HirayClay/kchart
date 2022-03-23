package com.bitmart.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ContractService {
    @GET("dapi/v1/klines")
    suspend fun getKLineData(@Query("symbol") symbol: String, @Query("interval") interval: String, @Query("endTime") endTime: Long? = null, @Query("limit") limit: Int = 500): List<List<String>>
}