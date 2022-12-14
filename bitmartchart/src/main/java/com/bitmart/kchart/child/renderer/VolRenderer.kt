package com.bitmart.kchart.child.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.bitmart.kchart.base.IBitMartChartView
import com.bitmart.kchart.child.base.BaseRenderer
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.VolRendererProperties

private const val FIXED_HEIGHT_RATIO = 0.02f

class VolRenderer(override val properties: VolRendererProperties, override val bitMartChartView: IBitMartChartView) : BaseRenderer<VolRendererProperties>() {

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

    override val rangeMaxBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity -> maxOf(dataEntity.vol, dataEntity.volMa[0], dataEntity.volMa[1]) }

    override val rangeMinBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity -> minOf(dataEntity.vol, dataEntity.volMa[0], dataEntity.volMa[1]) }


    private fun getMainRect(): RectF {
        val fixedHeight = dataRect.height() * FIXED_HEIGHT_RATIO
        return RectF(rendererRect.left, getHeaderRect().bottom, rendererRect.right, rendererRect.bottom - fixedHeight)
    }

    private fun getMainFixedRect(): RectF {
        val fixedHeight = dataRect.height() * FIXED_HEIGHT_RATIO
        return RectF(rendererRect.left, rendererRect.bottom - fixedHeight, rendererRect.right, rendererRect.bottom)
    }

    private fun getHeaderRect(): RectF {
        val headerHeight = dataRect.height() * bitMartChartView.getGlobalProperties().headerRatio
        return RectF(rendererRect.left, rendererRect.top, rendererRect.right, rendererRect.top + headerHeight)
    }

    override fun drawAxis(renderRect: RectF, canvas: Canvas) {
        drawAxisY(canvas)
    }

    private fun drawAxisY(canvas: Canvas) {
        val dataRange = bitMartChartView.getDataInScreenRange()

        if (dataRange.first == dataRange.second) {
            return
        }
        val max = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)
        textPaint.textSize = getFontSize()
        textPaint.color = bitMartChartView.getGlobalProperties().textColorSecondary()
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(max.countFormat(), rendererRect.right, getMainRect().top + getFontSize(), textPaint)
    }

    override fun drawHeader(renderRect: RectF, canvas: Canvas, dataEntity: ChartDataEntity) {
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.LEFT
        val fixTextHeight = textPaint.fontMetrics.descent
        val data1 = "VOL:${dataEntity.vol.countFormat()}"
        val data2 = "MA10:${dataEntity.volMa[0].indexFormat()}"
        val data3 = "MA20:${dataEntity.volMa[1].indexFormat()}"

        val width1 = textPaint.measureText(data1)
        val width2 = textPaint.measureText(data2)

        textPaint.color = getIndexColor()[0]
        canvas.drawText(data1, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
        textPaint.color = getIndexColor()[1]
        canvas.drawText(data2, getHeaderRect().left + width1 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
        textPaint.color = getIndexColor()[2]
        canvas.drawText(data3, getHeaderRect().left + width1 + 20 + width2 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
    }

    override fun drawData(dataRect: RectF, canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity) {
        drawVol(canvas, min, max, itemWidth, curStartX, curData)
        drawMa(canvas, min, max, itemWidth, preStartX, preData, curStartX, curData)
    }

    //???????????????
    private fun drawVol(canvas: Canvas, min: Double, max: Double, itemWidth: Float, curStartX: Float, curData: ChartDataEntity) {
        //??????????????????
        canvas.drawRect(
            curStartX,
            getMainFixedRect().top - 1,
            curStartX + itemWidth,
            getMainFixedRect().bottom,
            if (curData.isRise) risePaint else downPaint
        )

        if (max - min <= 0.0) {
            return
        }

        val fixHeight = (max - curData.vol) / (max - min) * (getMainRect().height())
        canvas.drawRect(
            curStartX,
            getMainRect().top + fixHeight.toFloat(),
            curStartX + itemWidth,
            getMainRect().bottom,
            if (curData.isRise) risePaint else downPaint
        )
    }

    //???????????????
    private fun drawMa(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity) {
        linePaint.color = getIndexColor()[1]
        drawCandleLine(canvas, min, max, itemWidth, preStartX, preData?.volMa?.get(0), curStartX, curData.volMa[0])
        linePaint.color = getIndexColor()[2]
        drawCandleLine(canvas, min, max, itemWidth, preStartX, preData?.volMa?.get(1), curStartX, curData.volMa[1])
    }

    private fun getIndexColor(): List<Int> {
        return if (bitMartChartView.getGlobalProperties().isDarkMode) properties.indexColor.dark else properties.indexColor.light
    }

    private fun drawCandleLine(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: Double?, curStartX: Float, curData: Double?) {

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