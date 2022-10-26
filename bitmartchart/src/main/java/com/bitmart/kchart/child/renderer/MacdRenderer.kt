package com.bitmart.kchart.child.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.bitmart.kchart.base.IBitMartChartView
import com.bitmart.kchart.child.base.BaseRenderer
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.MacdRendererProperties


class MacdRenderer(override val properties: MacdRendererProperties, override val bitMartChartView: IBitMartChartView) : BaseRenderer<MacdRendererProperties>() {

    private val textPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().textColor()
            isAntiAlias = true
        }
    }

    private val linePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            isDither = true
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    private fun getFontSize(): Float {
        return dataRect.height() * bitMartChartView.getGlobalProperties().headerRatio
    }

    override val rangeMaxBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity -> maxOf(dataEntity.macd[0], dataEntity.macd[1], dataEntity.macd[2]) }

    override val rangeMinBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity -> minOf(dataEntity.macd[0], dataEntity.macd[1], dataEntity.macd[2]) }

    private fun getMainRect(): RectF {
        val headerHeight = dataRect.height() * bitMartChartView.getGlobalProperties().headerRatio
        return RectF(rendererRect.left, rendererRect.top + headerHeight, rendererRect.right, rendererRect.bottom)
    }

    private fun getHeaderRect(): RectF {
        val headerHeight = dataRect.height() * bitMartChartView.getGlobalProperties().headerRatio
        return RectF(rendererRect.left, rendererRect.top, rendererRect.right, rendererRect.top + headerHeight)
    }


    override fun drawHeader(renderRect: RectF, canvas: Canvas, dataEntity: ChartDataEntity) {
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.LEFT
        val fixTextHeight = textPaint.fontMetrics.descent
        val data0 = "MACD(12,26,9)"
        val data1 = "MACD:${dataEntity.macd[0].indexFormat()}"
        val data2 = "DIF:${dataEntity.macd[1].indexFormat()}"
        val data3 = "DEA:${dataEntity.macd[2].indexFormat()}"

        val width0 = textPaint.measureText(data0)
        val width1 = textPaint.measureText(data1)
        val width2 = textPaint.measureText(data2)

        textPaint.color = bitMartChartView.getGlobalProperties().textColor()
        canvas.drawText(data0, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
        textPaint.color = getIndexColor()[0]
        canvas.drawText(data1, getHeaderRect().left + width0 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
        textPaint.color = getIndexColor()[1]
        canvas.drawText(data2, getHeaderRect().left + width0 + 20 + width1 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
        textPaint.color = getIndexColor()[2]
        canvas.drawText(data3, getHeaderRect().left + width0 + 20 + width1 + 20 + width2 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
    }


    override fun drawAxis(renderRect: RectF, canvas: Canvas) {
        drawAxisY(canvas)
    }

    private fun drawAxisY(canvas: Canvas) {
        val dataRange = bitMartChartView.getDataInScreenRange()

        if (dataRange.first == dataRange.second) {
            return
        }
        val min = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).minOf(rangeMinBy)
        val max = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)

        textPaint.textSize = getFontSize()
        textPaint.color = bitMartChartView.getGlobalProperties().textColorSecondary()
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(max.indexFormat(), rendererRect.right, getMainRect().top + getFontSize(), textPaint)
        canvas.drawText(min.indexFormat(), rendererRect.right, getMainRect().bottom, textPaint)
    }

    override fun drawData(dataRect: RectF, canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity) {
        drawMacd(canvas, min, max, itemWidth, curStartX, curData)
        linePaint.color = getIndexColor()[1]
        drawLine(canvas, min, max, itemWidth, preStartX, preData?.macd?.get(1), curStartX, curData.macd[1])
        linePaint.color = getIndexColor()[2]
        drawLine(canvas, min, max, itemWidth, preStartX, preData?.macd?.get(2), curStartX, curData.macd[2])
    }

    //绘制柱状图
    private fun drawMacd(canvas: Canvas, min: Double, max: Double, itemWidth: Float, curStartX: Float, curData: ChartDataEntity) {
        val macd = curData.macd[0] ?: return
        val height = getMainRect().height()
        val fixHeight = (max - macd) / (max - min) * height
        val macdZeroHeight = (max - 0.0) / (max - min) * height
        canvas.drawRect(
            curStartX,
            getMainRect().top + fixHeight.toFloat(),
            curStartX + itemWidth,
            getMainRect().top + macdZeroHeight.toFloat(),
            if (macd > 0) risePaint else downPaint
        )
    }


    private fun getIndexColor(): List<Int> {
        return if (bitMartChartView.getGlobalProperties().isDarkMode) properties.indexColor.dark else properties.indexColor.light
    }

    //绘制折线图
    private fun drawLine(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: Double?, curStartX: Float, curData: Double?) {

        if (preData == null || preStartX == null || curData == null) {
            return
        }
        val difHeight = (max - min)
        val preDataHeight = max - preData
        val curDataHeight = max - curData

        canvas.drawLine(
            preStartX + itemWidth / 2,
            (getMainRect().top + (preDataHeight.toFloat() / difHeight.toFloat() * getMainRect().height())),
            curStartX + itemWidth / 2,
            (getMainRect().top + (curDataHeight.toFloat() / difHeight.toFloat() * getMainRect().height())),
            linePaint
        )
    }
}