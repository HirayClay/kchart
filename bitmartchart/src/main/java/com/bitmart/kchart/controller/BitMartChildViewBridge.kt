package com.bitmart.kchart.controller

import com.bitmart.kchart.entity.ChartExtraInfoEntity

interface BitMartChildViewBridge {

    fun onDataSetChanged()

    fun onDataSetAdd(newDataSize: Int)

    fun finishLoadMore(noMore: Boolean)

    fun getCurrentPageSize(): Int

    fun onSetChartExtraInfo(entity: ChartExtraInfoEntity?)
}