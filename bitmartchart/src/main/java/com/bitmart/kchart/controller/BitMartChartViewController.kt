package com.bitmart.kchart.controller

import com.bitmart.kchart.entity.Calculator
import com.bitmart.kchart.entity.ChartDataEntity

class BitMartChartViewController {
    private var call: BitMartChildViewBridge? = null
    internal var loadMoreListener: (() -> Unit)? = null
    private val chartDataEntities = mutableListOf<ChartDataEntity>()

    fun getChartData(): List<ChartDataEntity> {
        return chartDataEntities
    }

    //更新最新一条数据
    suspend fun updateNewerData(entity: ChartDataEntity) {
        if (chartDataEntities.isEmpty()) return
        chartDataEntities.removeLast()
        chartDataEntities.add(entity)
        Calculator.calc(chartDataEntities)
        call?.onDataSetAdd(0)
    }

    //添加一条新数据
    suspend fun addNewData(entity: ChartDataEntity) {
        chartDataEntities.add(entity)
        Calculator.calc(chartDataEntities)
        call?.onDataSetAdd(1)
    }

    //设置数据
    suspend fun setChartData(entities: List<ChartDataEntity>) {
        chartDataEntities.clear()
        chartDataEntities.addAll(entities)
        Calculator.calc(chartDataEntities)
        setLoadMOreFinish()
        call?.onDataSetChanged()
    }

    //添加新数据
    suspend fun addNewChartData(entities: List<ChartDataEntity>) {
        if (chartDataEntities.isEmpty()) {
            setChartData(entities)
            return
        }
        chartDataEntities.addAll(entities)
        Calculator.calc(chartDataEntities)
        setLoadMOreFinish()
        call?.onDataSetAdd(if (chartDataEntities.size == 0) 0 else entities.size)
    }

    //添加老数据
    suspend fun addOldChartData(entities: List<ChartDataEntity>) {
        if (chartDataEntities.isEmpty()) {
            setChartData(entities)
            return
        }
        chartDataEntities.addAll(0, entities)
        Calculator.calc(chartDataEntities)
        setLoadMOreFinish()
        call?.onDataSetAdd(if (chartDataEntities.size == 0) 0 else entities.size)
    }

    internal fun setListener(call: BitMartChildViewBridge) {
        this.call = null
        this.call = call
    }

    fun setLoadMoreListener(loadMoreListener: () -> Unit) {
        this.loadMoreListener = loadMoreListener
    }

    //设置更新完成
    fun setLoadMOreFinish() {
        call?.finishLoadMore()
    }
}