package com.bitmart.data.remote.repository

import com.bitmart.data.domain.model.KLineModel
import com.bitmart.data.domain.repository.IContractRepository
import com.bitmart.data.remote.api.ContractService

class ContractRemoteRepository(private val articleService: ContractService) : IContractRepository {
    override suspend fun getKLineData(symbol: String, interval: String, endTime: Long?, limit: Int): List<KLineModel> {
        val kLineData = articleService.getKLineData(symbol, interval, endTime, limit)
        return kLineData.map {
            KLineModel(
                time = it[6].toLong(),
                open = it[1].toDouble(),
                high = it[2].toDouble(),
                low = it[3].toDouble(),
                close = it[4].toDouble(),
                vol = it[5].toDouble(),
                amount = it[7].toDouble(),
            )
        }
    }
}