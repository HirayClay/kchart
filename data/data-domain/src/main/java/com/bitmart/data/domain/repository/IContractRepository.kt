package com.bitmart.data.domain.repository

import com.bitmart.data.domain.model.KLineModel

interface IContractRepository {
    suspend fun getKLineData(symbol: String,  interval: String,  endTime: Long?, limit: Int = 500): List<KLineModel>
}