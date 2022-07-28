@file:Suppress("unused")

package com.bitmart.kchart.controller

import androidx.annotation.WorkerThread
import com.bitmart.kchart.entity.Calculator
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.BitMartChartProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BitMartChartViewController {
    private var call: BitMartChildViewBridge? = null
    internal var loadMoreListener: (() -> Unit)? = null
    internal var chartChangeListener: ChartChangeListener? = null
    private var chartDataEntities = mutableListOf<ChartDataEntity>()

    fun getChartData(): List<ChartDataEntity> {
        return chartDataEntities
    }

    //更新最新一条数据
    suspend fun updateNewerData(entity: ChartDataEntity) {
        chartDataEntities.removeLast()
        chartDataEntities.add(entity)
        if (chartDataEntities.isEmpty()) return
        chartDataEntities = withContext(Dispatchers.IO) { Calculator.calc(chartDataEntities) }
        call?.onDataSetAdd(0)
    }

    //添加一条新数据
    suspend fun addNewData(entity: ChartDataEntity) {
        chartDataEntities.add(entity)
        chartDataEntities = withContext(Dispatchers.IO) { Calculator.calc(chartDataEntities) }
        call?.onDataSetAdd(1)
    }

    //设置数据
    suspend fun setChartData(entities: List<ChartDataEntity>) {
        chartDataEntities.clear()
        chartDataEntities.addAll(entities)
        chartDataEntities = withContext(Dispatchers.IO) { Calculator.calc(chartDataEntities) }
        call?.onDataSetChanged()
    }

    //添加新数据
    suspend fun addNewChartData(entities: List<ChartDataEntity>) {
        if (chartDataEntities.isEmpty()) {
            setChartData(entities)
            return
        }
        chartDataEntities.addAll(entities)
        chartDataEntities = withContext(Dispatchers.IO) { Calculator.calc(chartDataEntities) }
        call?.onDataSetAdd(if (chartDataEntities.size == 0) 0 else entities.size)
    }

    //添加老数据
    suspend fun addOldChartData(entities: List<ChartDataEntity>) {

        if (chartDataEntities.isEmpty()) {
            setChartData(entities)
            return
        }
        chartDataEntities.addAll(0, entities)
        chartDataEntities = withContext(Dispatchers.IO) { Calculator.calc(chartDataEntities) }
        call?.onDataSetAdd(if (chartDataEntities.size == 0) 0 else entities.size)
    }

    internal fun setListener(call: BitMartChildViewBridge) {
        this.call = null
        this.call = call
    }

    fun setLoadMoreListener(loadMoreListener: () -> Unit) {
        this.loadMoreListener = loadMoreListener
    }

    fun setChartChangeListener(listener: ChartChangeListener) {
        this.chartChangeListener = listener
    }

    //设置更新完成
    fun setLoadMoreFinish(noMore: Boolean) {
        call?.finishLoadMore(noMore)
    }

    //获取当前屏幕可显示的条目数
    fun getCurrentPageSize(): Int {
        return call?.getCurrentPageSize() ?: 40
    }
}

interface ChartChangeListener {

    @WorkerThread
    fun onPageShowNumChange(pageShowNum: Int)

    @WorkerThread
    fun onChartPropertiesChange(properties: BitMartChartProperties)
}