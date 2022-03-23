package com.bitmart.kchart.child.base

import android.graphics.Canvas
import android.graphics.RectF
import com.bitmart.kchart.base.IBitMartChartView
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.IRendererProperties

interface IRenderer<out P : IRendererProperties> {

    val properties: P

    val bitMartChartView: IBitMartChartView

    var dataRect:RectF

    var rendererRect: RectF

    fun draw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float)

    fun noCovertDraw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float)
}