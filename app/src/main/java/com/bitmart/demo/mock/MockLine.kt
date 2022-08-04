package com.bitmart.demo.mock

import com.bitmart.data.domain.model.KLineModel
import com.bitmart.demo.MyApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MockLine{
    suspend fun getMockKlineModel(): List<KLineModel> {
        return withContext(Dispatchers.IO) {
            val assetManager = MyApplication.instance.assets
            val json = assetManager.open("mock_line.json").bufferedReader().readText()
            val list = Gson().fromJson<List<MockKlineModel>>(json, object : TypeToken<List<MockKlineModel>>() {}.type)
            list.map { it.covertModel() }
        }
    }
}

data class MockKlineModel(
    val avg_price: String,
    val base_coin_volume: String,
    val close: String,
    val contract_name: String,
    val high: String,
    val last_price: String,
    val low: String,
    val `open`: String,
    val quote_coin_volume: String,
    val rise_fall_rate: String,
    val rise_fall_value: String,
    val timestamp: Int,
    val volume: String
) {
    fun covertModel(): KLineModel {
        return KLineModel(time = timestamp * 1000L, open = open.toDouble(), high = high.toDouble(), low = low.toDouble(), close = close.toDouble(), vol = volume.toDouble(), amount = quote_coin_volume.toDouble())
    }
}