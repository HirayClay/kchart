package com.bitmart.kchart.base

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.GlobalProperties

interface IBitMartChartView {

    //获取上下文
    fun getContext(): Context

    //获取触摸区域
    fun getTouchArea(): Rect

    //获取数据
    fun getChartData(): List<ChartDataEntity>

    //获取总缩放比
    fun getTotalScale(): Float

    //获取总滑动距离
    fun getTotalTranslate(): Float

    //是否到左边界
    fun isReachLeftBorder(): Boolean

    //是否到右边界
    fun isReachRightBorder(): Boolean

    //获取全局配置
    fun getGlobalProperties(): GlobalProperties

    //重绘制
    fun invalidate()

    //判断数据是否在屏幕中
    fun isDataInScreen(index: Int): Boolean

    //获取屏幕显示的数据范围
    fun getDataInScreenRange(): Pair<Int, Int>

    //获取高亮触摸手指位置null表示没有高亮
    fun getHighlightingPoint(): PointF?

    // 获取屏幕点对应的数据下标
    fun getDataIndexByScreenPointX(x: Float): Int?

    // 获取数据下标对应的屏幕点
    fun getDataScreenPointXbyIndex(index: Int): Float?
}
