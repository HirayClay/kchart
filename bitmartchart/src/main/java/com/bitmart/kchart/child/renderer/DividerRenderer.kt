package com.bitmart.kchart.child.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.bitmart.kchart.base.IBitMartChartView
import com.bitmart.kchart.child.base.IRenderer
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.IRendererProperties

class DividerRenderer(override val bitMartChartView: IBitMartChartView) : IRenderer<DividerRendererProperties> {

    private val highlightingPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().highlightingColor()
        }
    }

    override fun draw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float) {

    }

    override fun noCovertDraw(canvas: Canvas, chartData: List<ChartDataEntity>, startX: Float, spaceWidth: Float, itemWidth: Float) {
        canvas.save()
        val highlightingPoint = bitMartChartView.getHighlightingPoint()
        if (highlightingPoint != null) {
            val pointXDataIndex = bitMartChartView.getDataIndexByScreenPointX(highlightingPoint.x)
            if (pointXDataIndex != null) {
                val pointXData = bitMartChartView.getChartData().getOrNull(pointXDataIndex)
                if (pointXData != null) {
                    drawHighlighting(rendererRect, canvas, highlightingPoint, itemWidth, pointXData)
                }
            }
        }
        canvas.restore()
    }

    private fun drawHighlighting(renderRect: RectF, canvas: Canvas, pressPoint: PointF, itemWidth: Float, pointXData: ChartDataEntity) {
        val left = pressPoint.x - itemWidth / 2 * bitMartChartView.getTotalScale()
        val right = pressPoint.x + itemWidth / 2 * bitMartChartView.getTotalScale()
        canvas.drawRect(left, renderRect.top, right, renderRect.bottom, highlightingPaint)
    }

    override var dataRect: RectF = RectF()
    override var rendererRect: RectF = RectF()
    override val properties by lazy { DividerRendererProperties() }
}

data class DividerRendererProperties(override var heightRatio: Float = 0.08f) : IRendererProperties