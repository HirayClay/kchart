package com.bitmart.kchart.child.base

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.entity.ChartExtraInfoEntity
import com.bitmart.kchart.properties.IRendererProperties
import kotlin.math.max
import kotlin.math.min

abstract class BaseRenderer<out T : IRendererProperties> : IRenderer<T> {

    val highlightingPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().highlightingColor()
            isAntiAlias = true
        }
    }

    val downPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().downColor()
            isAntiAlias = true
        }
    }

    val risePaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().riseColor()
            isAntiAlias = true
        }
    }

    override var dataRect = RectF()

    override var rendererRect = RectF()

    abstract val rangeMaxBy: (dataEntity: ChartDataEntity) -> Double
    abstract val rangeMinBy: (dataEntity: ChartDataEntity) -> Double

    override fun draw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float) {
        canvas.save()
        var x = startX
        val dataRange = bitMartChartView.getDataInScreenRange()

        if (dataRange.first == dataRange.second) {
            canvas.restore()
            return
        }
        val min = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).minOf(rangeMinBy)
        val max = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)
        chartData.forEachIndexed { index, dataEntity ->
            if (bitMartChartView.isDataInScreen(index)) {
                drawData(dataRect, canvas, min, max, itemWidth, if (index == 0) null else (x - itemWidth - spaceWidth), if (index == 0) null else chartData[index - 1], x, dataEntity)
            }
            x += (spaceWidth + itemWidth)
        }
        canvas.restore()
    }

    override fun noCovertDraw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float) {
        canvas.save()
        drawAxis(rendererRect, canvas)
        drawExtraInfo(rendererRect,canvas,bitMartChartView.getChartExtraData())
        val highlightingPoint = bitMartChartView.getHighlightingPoint()
        if (highlightingPoint == null) {
            drawHeader(rendererRect, canvas, chartData.last())
        } else {
            val pointXDataIndex = bitMartChartView.getDataIndexByScreenPointX(highlightingPoint.x)
            if (pointXDataIndex != null) {
                val pointXData = bitMartChartView.getChartData().getOrNull(pointXDataIndex)
                if (pointXData != null) {
                    drawHeader(rendererRect, canvas, pointXData)
                    drawHighlighting(rendererRect, canvas, highlightingPoint, itemWidth, pointXData)
                } else {
                    drawHeader(rendererRect, canvas, chartData.last())
                }
            } else {
                drawHeader(rendererRect, canvas, chartData.last())
            }
        }
        canvas.restore()
    }

    fun maxOf(vararg num: Double?): Double {
        var max = 0.0
        num.forEach { n ->
            n?.let {
                max = max(max, it)
            }
        }
        return max
    }

    fun minOf(vararg num: Double?): Double {
        var min = Double.MAX_VALUE
        num.forEach { n ->
            n?.let {
                min = min(min, n)
            }
        }
        return min
    }

    protected fun Number?.priceFormat(): String {

        if (this == null) return bitMartChartView.getGlobalProperties().priceAccuracyFormat.format(0.0)
        return bitMartChartView.getGlobalProperties().priceAccuracyFormat.format(this)
    }

    protected fun Number?.countFormat(): String {
        if (this == null) return bitMartChartView.getGlobalProperties().countAccuracyFormat.format(0.0)
        return bitMartChartView.getGlobalProperties().countAccuracyFormat.format(this)
    }

    protected fun Number?.indexFormat(): String {
        if (this == null) return bitMartChartView.getGlobalProperties().indexAccuracyFormat.format(0.0)
        return bitMartChartView.getGlobalProperties().indexAccuracyFormat.format(this)
    }

    //绘制标题
    open fun drawHeader(renderRect: RectF, canvas: Canvas, dataEntity: ChartDataEntity) {

    }

    //绘制坐标轴
    open fun drawAxis(renderRect: RectF, canvas: Canvas) {

    }

    open fun drawExtraInfo(renderRect: RectF, canvas: Canvas, extraInfoEntity: ChartExtraInfoEntity?) {

    }

    open fun drawHighlighting(renderRect: RectF, canvas: Canvas, pressPoint: PointF, itemWidth: Float, dataEntity: ChartDataEntity) {
        val left = pressPoint.x - itemWidth / 2 * bitMartChartView.getTotalScale()
        val right = pressPoint.x + itemWidth / 2 * bitMartChartView.getTotalScale()
        canvas.drawRect(left, renderRect.top, right, renderRect.bottom, highlightingPaint)
    }

    abstract fun drawData(dataRect: RectF, canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity)
}