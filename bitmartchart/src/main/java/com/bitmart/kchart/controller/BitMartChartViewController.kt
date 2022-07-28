@file:Suppress("unused")

package com.bitmart.kchart.controller

import androidx.annotation.WorkerThread
import com.bitmart.kchart.entity.Calculator
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.BitMartChartProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class BitMartChartViewController {
    private var call: BitMartChildViewBridge? = null
    internal var loadMoreListener: (() -> Unit)? = null
    internal var chartChangeListener: ChartChangeListener? = null
    private val chartDataEntities = CopyOnWriteArrayList<ChartDataEntity>()

    private val mutex = Mutex()

    fun getChartData(): List<ChartDataEntity> {
        return chartDataEntities
    }

    //更新最新一条数据
    suspend fun updateNewerData(entity: ChartDataEntity) {
        withContext(Dispatchers.IO) {
            if (chartDataEntities.isEmpty()) return@withContext
            mutex.withLock {
                chartDataEntities.removeLast()
                chartDataEntities.add(entity)
                Calculator.calc(chartDataEntities)
            }
        }
        call?.onDataSetAdd(0)
    }

    //添加一条新数据
    suspend fun addNewData(entity: ChartDataEntity) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                chartDataEntities.add(entity)
                Calculator.calc(chartDataEntities)
            }
        }
        call?.onDataSetAdd(1)
    }

    //设置数据
    suspend fun setChartData(entities: List<ChartDataEntity>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                chartDataEntities.clear()
                chartDataEntities.addAll(entities)
                Calculator.calc(chartDataEntities)
            }
        }
        call?.onDataSetChanged()
    }

    //添加新数据
    suspend fun addNewChartData(entities: List<ChartDataEntity>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (chartDataEntities.isEmpty()) {
                    setChartData(entities)
                    return@withContext
                }
                chartDataEntities.addAll(entities)
                Calculator.calc(chartDataEntities)

            }
        }
        call?.onDataSetAdd(if (chartDataEntities.size == 0) 0 else entities.size)
    }

    //添加老数据
    suspend fun addOldChartData(entities: List<ChartDataEntity>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (chartDataEntities.isEmpty()) {
                    setChartData(entities)
                    return@withContext
                }
                chartDataEntities.addAll(0, entities)
                Calculator.calc(chartDataEntities)
            }
        }
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