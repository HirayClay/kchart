package com.bitmart.data.domain.usecase

import com.bitmart.data.domain.model.KLineModel
import com.bitmart.data.domain.repository.IContractRepository

class GetKLineListUseCase( private val remoteRepository: IContractRepository) {

    suspend operator fun invoke(symbol: String, interval: String, endTime: Long? = null, limit: Int = 500): List<KLineModel> {
        return remoteRepository.getKLineData(symbol, interval, endTime, limit);
    }
}