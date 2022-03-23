package com.bitmart.data.local.repository

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.bitmart.data.domain.model.KLineModel
import com.bitmart.data.domain.repository.IContractRepository
import java.io.IOException

class ContractLocalRepository(private val context: Context) : IContractRepository {
    override suspend fun getKLineData(symbol: String, interval: String, endTime: Long?, limit: Int): List<KLineModel> {
        val assetsManager = context.assets
        val text = try {
            assetsManager.open("line.json", AssetManager.ACCESS_STREAMING).reader().readText()
        } catch (e: IOException) {
            "[]"
        }
        val kLineData = Gson().fromJson<List<List<Any>>>(text, object : TypeToken<List<List<Any>>>() {}.type)
        return kLineData.map {
            KLineModel(
                time = it[6].toString().toLong(),
                open = it[1].toString().toDouble(),
                high = it[2].toString().toDouble(),
                low = it[3].toString().toDouble(),
                close = it[4].toString().toDouble(),
                vol = it[5].toString().toDouble(),
                amount = it[7].toString().toDouble(),
            )
        }
    }

}