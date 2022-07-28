package com.bitmart.demo.util

import com.bitmart.demo.MyApplication
import com.bitmart.kchart.properties.BitMartChartProperties
import com.bitmart.kchart.properties.KLineRendererProperties
import com.bitmart.kchart.properties.VolRendererProperties
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileCache(private val name: String, private val default: String? = null) {

    private val cacheDir = MyApplication.instance.cacheDir

    suspend fun setValue(value: String) {
        withContext(Dispatchers.IO) {
            val file = File(cacheDir, name)
            file.writeText(value, Charsets.UTF_8)
        }
    }

    suspend fun getValue(): String? {
        return withContext(Dispatchers.IO) {
            val file = File(cacheDir, name)
            if (!file.exists()) return@withContext default
            return@withContext file.readText(Charsets.UTF_8)
        }
    }
}

class ChartConfigCacheManager {

    private val gson = Gson()

    private val fileCache = FileCache("cache_bitmart_chart_config")

    var properties = BitMartChartProperties(KLineRendererProperties(), VolRendererProperties())
        private set

    fun cachePageShowNum(pageShowNum: Int) {
        properties.pageShowNum = pageShowNum
    }

    fun cacheProperties(properties: BitMartChartProperties) {
        val pageShowNum = this.properties.pageShowNum
        this.properties = properties
        this.properties.pageShowNum = pageShowNum
    }

    suspend fun setUp(): BitMartChartProperties {
        val propertiesJson = fileCache.getValue()
        //缓存中没找到用默认设置的
        if (propertiesJson.isNullOrEmpty()) {
            return this.properties
        }
        try {
            this@ChartConfigCacheManager.properties = gson.fromJson(propertiesJson, BitMartChartProperties::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this.properties
    }

    //缓存数据 请在onPause中调用
    suspend fun storageData() {
        fileCache.setValue(gson.toJson(properties))
    }
}
