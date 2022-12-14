package com.bitmart.kchart.child.renderer

import android.graphics.*
import android.text.format.DateFormat
import androidx.core.graphics.contains
import com.bitmart.kchart.base.IBitMartChartView
import com.bitmart.kchart.child.base.BaseRenderer
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.entity.ChartExtraInfoEntity
import com.bitmart.kchart.entity.PositionInfo
import com.bitmart.kchart.entity.SarEntity
import com.bitmart.kchart.properties.KLineRendererProperties
import com.bitmart.kchart.properties.KLineShowType
import com.bitmart.kchart.util.addPlusSign
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val CANDLE_CENTER_WIDTH_RATIO = 0.1f
private const val CONTENT_PADDING_TOP_RATIO = 0.04f
private const val CANDLE_MIN_HEIGHT = 1f

class KLineRenderer(override val properties: KLineRendererProperties, override val bitMartChartView: IBitMartChartView) : BaseRenderer<KLineRendererProperties>() {


    private val textPadding = 10

    private val textPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().textColor()
            isAntiAlias = true
        }
    }

    private val coverPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().backgroundColor
            isAntiAlias = true
        }
    }

    private val infoWindowPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().backgroundColor
            isAntiAlias = true
        }
    }

    private val linePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = bitMartChartView.getGlobalProperties().highlightingColor()
            isDither = true
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    private val dashLinePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            isDither = true
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(6f, 5f), 0f)
        }
    }

    private val axisPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = bitMartChartView.getGlobalProperties().highlightingColor()
            isAntiAlias = true
        }
    }

    private fun getFontSize(): Float {
        return dataRect.height() * bitMartChartView.getGlobalProperties().headerRatio / properties.heightRatio
    }

    override val rangeMaxBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity ->
        when (properties.showType) {
            KLineShowType.TIME_LINE -> maxOf(dataEntity.high, dataEntity.high, dataEntity.close, dataEntity.open)
            KLineShowType.CANDLE_WITH_MA -> maxOf(dataEntity.high, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.ma[0], dataEntity.ma[1], dataEntity.ma[2])
            KLineShowType.CANDLE_WITH_EMA -> maxOf(dataEntity.high, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.ema[0], dataEntity.ema[1], dataEntity.ema[2])
            KLineShowType.CANDLE_WITH_BOLL -> maxOf(dataEntity.high, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.boll[0], dataEntity.boll[1], dataEntity.boll[2])
            KLineShowType.CANDLE_WITH_SAR -> maxOf(dataEntity.high, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.sar.sar)
        }
    }

    override val rangeMinBy: (dataEntity: ChartDataEntity) -> Double = { dataEntity ->
        when (properties.showType) {
            KLineShowType.TIME_LINE -> minOf(dataEntity.low, dataEntity.high, dataEntity.close, dataEntity.open)
            KLineShowType.CANDLE_WITH_MA -> minOf(dataEntity.low, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.ma[0], dataEntity.ma[1], dataEntity.ma[2])
            KLineShowType.CANDLE_WITH_EMA -> minOf(dataEntity.low, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.ema[0], dataEntity.ema[1], dataEntity.ema[2])
            KLineShowType.CANDLE_WITH_BOLL -> minOf(dataEntity.low, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.boll[0], dataEntity.boll[1], dataEntity.boll[2])
            KLineShowType.CANDLE_WITH_SAR -> minOf(dataEntity.low, dataEntity.high, dataEntity.close, dataEntity.open, dataEntity.sar.sar)
        }
    }

    private fun getHeaderRect(): RectF {
        return RectF(rendererRect.left, rendererRect.top, rendererRect.right, rendererRect.top + rendererRect.height() * bitMartChartView.getGlobalProperties().headerRatio / properties.heightRatio)
    }

    private fun getDateRect(): RectF {
        return RectF(rendererRect.left, rendererRect.bottom - rendererRect.height() * bitMartChartView.getGlobalProperties().headerRatio / properties.heightRatio, rendererRect.right, rendererRect.bottom)
    }

    private fun getDrawDataRect(): RectF {
        return RectF(
            dataRect.left,
            rendererRect.top + rendererRect.height() * bitMartChartView.getGlobalProperties().headerRatio / properties.heightRatio + rendererRect.height() * CONTENT_PADDING_TOP_RATIO,
            dataRect.right,
            rendererRect.bottom - rendererRect.height() * bitMartChartView.getGlobalProperties().headerRatio / properties.heightRatio - rendererRect.height() * CONTENT_PADDING_TOP_RATIO
        )
    }

    override fun drawHighlighting(renderRect: RectF, canvas: Canvas, pressPoint: PointF, itemWidth: Float, dataEntity: ChartDataEntity) {
        super.drawHighlighting(renderRect, canvas, pressPoint, itemWidth, dataEntity)
        if (getDrawDataRect().contains(pressPoint)) {
            drawHighlightingPrice(renderRect, canvas, pressPoint)
        }
        drawHighlightingDate(canvas, pressPoint, dataEntity)
        drawHighlightingInfo(renderRect, canvas, pressPoint, dataEntity)
    }

    //??????????????????
    private fun drawHighlightingPrice(renderRect: RectF, canvas: Canvas, pressPoint: PointF) {

        val dataRange = bitMartChartView.getDataInScreenRange()
        if (dataRange.first == dataRange.second) {
            return
        }
        val min = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).minOf(rangeMinBy)
        val max = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)
        val currentPrice = (max - (max - min) * (pressPoint.y - getDrawDataRect().top) / (getDrawDataRect().height())).priceFormat()

        textPaint.color = bitMartChartView.getGlobalProperties().highlightingColor()
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.RIGHT
        val textWidth = textPaint.measureText(currentPrice)
        if ((pressPoint.x - getDrawDataRect().left) > (renderRect.width() / 2)) {
            canvas.drawLine(renderRect.left, pressPoint.y, renderRect.right - textWidth, pressPoint.y, highlightingPaint)
            textPaint.color = bitMartChartView.getGlobalProperties().textColor()
            canvas.drawText(currentPrice, renderRect.right, pressPoint.y + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
        } else {
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawLine(renderRect.left + textWidth, pressPoint.y, renderRect.right, pressPoint.y, highlightingPaint)
            textPaint.color = bitMartChartView.getGlobalProperties().textColor()
            canvas.drawText(currentPrice, renderRect.left, pressPoint.y + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
        }

    }

    //??????????????????
    private fun drawHighlightingDate(canvas: Canvas, pressPoint: PointF, dataEntity: ChartDataEntity) {
        textPaint.color = bitMartChartView.getGlobalProperties().textColor()
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.CENTER
        val time = DateFormat.format(properties.dataFormat, dataEntity.time).toString()

        val timeWidth = textPaint.measureText(time)
        canvas.drawRect(pressPoint.x - timeWidth / 2, getDateRect().top, pressPoint.x + timeWidth / 2, getDateRect().bottom, coverPaint)
        canvas.drawText(time, pressPoint.x, getDateRect().bottom, textPaint)
    }

    //??????????????????
    private fun drawHighlightingInfo(renderRect: RectF, canvas: Canvas, pressPoint: PointF, dataEntity: ChartDataEntity) {
        val fontSize = getFontSize()
        textPaint.textSize = fontSize

        val fontHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top
        val fontPadding = textPaint.fontMetrics.descent

        val info = listOf(
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.date}:", DateFormat.format(properties.dataFormat, dataEntity.time).toString(), false),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.open}:", dataEntity.open.priceFormat(), false),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.high}:", dataEntity.high.priceFormat(), false),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.low}:", dataEntity.low.priceFormat(), false),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.close}:", dataEntity.close.priceFormat(), false),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.change}:", dataEntity.change.priceFormat(), true),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.changeRatio}:", "${dataEntity.ratio.priceFormat()}%", true),
            Triple("${bitMartChartView.getGlobalProperties().chartLanguage.vol}:", dataEntity.vol.countFormat(), false),
        )
        val infoAreaMinWidth = info.maxOf { textPaint.measureText(it.first + it.second) }
        val infoAreaHeight = fontHeight * 8f
        val margin = infoAreaMinWidth / 8
        val infoAreaWidth = if (infoAreaMinWidth + infoAreaMinWidth / 3 < infoAreaHeight) infoAreaHeight else infoAreaMinWidth + infoAreaMinWidth / 3

        //???????????????????????????
        val area = if ((pressPoint.x - getDrawDataRect().left) > (renderRect.width() / 2)) {
            RectF(renderRect.left + margin, getDrawDataRect().top + margin, infoAreaWidth + margin, getDrawDataRect().top + infoAreaHeight + margin)
        } else {
            RectF(renderRect.right - infoAreaWidth - margin, getDrawDataRect().top + margin, renderRect.right - margin, getDrawDataRect().top + infoAreaHeight + margin)
        }

        infoWindowPaint.color = bitMartChartView.getGlobalProperties().backgroundColor
        infoWindowPaint.style = Paint.Style.FILL
        canvas.drawRect(area, infoWindowPaint)

        infoWindowPaint.color = bitMartChartView.getGlobalProperties().textColor()
        infoWindowPaint.style = Paint.Style.STROKE
        canvas.drawRect(area, infoWindowPaint)

        var top = area.top
        info.forEach {
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = bitMartChartView.getGlobalProperties().textColorSecondary()
            canvas.drawText(it.first, area.left + fontPadding, top + fontSize, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            if (it.third) {
                textPaint.color = if (dataEntity.isRise) bitMartChartView.getGlobalProperties().riseColor() else bitMartChartView.getGlobalProperties().downColor()
            } else {
                textPaint.color = bitMartChartView.getGlobalProperties().textColor()
            }
            canvas.drawText(it.second, area.right - fontPadding, top + fontSize, textPaint)
            top += fontHeight
        }
    }

    override fun drawHeader(renderRect: RectF, canvas: Canvas, dataEntity: ChartDataEntity) {

        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.LEFT
        val fixTextHeight = textPaint.fontMetrics.descent
        when (properties.showType) {
            KLineShowType.TIME_LINE -> {

            }
            KLineShowType.CANDLE_WITH_MA -> {
                val data1 = "MA5:${dataEntity.ma[0].indexFormat()}"
                val data2 = "MA10:${dataEntity.ma[1].indexFormat()}"
                val data3 = "MA20:${dataEntity.ma[2].indexFormat()}"

                val width1 = textPaint.measureText(data1)
                val width2 = textPaint.measureText(data2)

                textPaint.color = getIndexColor()[0]
                canvas.drawText(data1, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[1]
                canvas.drawText(data2, getHeaderRect().left + width1 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[2]
                canvas.drawText(data3, getHeaderRect().left + width1 + 20 + width2 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
            }
            KLineShowType.CANDLE_WITH_EMA -> {
                val data1 = "EMA5:${dataEntity.ema[0].indexFormat()}"
                val data2 = "EMA10:${dataEntity.ema[1].indexFormat()}"
                val data3 = "EMA20:${dataEntity.ema[2].indexFormat()}"

                val width1 = textPaint.measureText(data1)
                val width2 = textPaint.measureText(data2)

                textPaint.color = getIndexColor()[0]
                canvas.drawText(data1, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[1]
                canvas.drawText(data2, getHeaderRect().left + width1 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[2]
                canvas.drawText(data3, getHeaderRect().left + width1 + width2 + 20 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
            }
            KLineShowType.CANDLE_WITH_BOLL -> {
                val data1 = "BOLL:${dataEntity.boll[0].indexFormat()}"
                val data2 = "UB:${dataEntity.boll[1].indexFormat()}"
                val data3 = "LB:${dataEntity.boll[2].indexFormat()}"

                val width1 = textPaint.measureText(data1)
                val width2 = textPaint.measureText(data2)

                textPaint.color = getIndexColor()[0]
                canvas.drawText(data1, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[1]
                canvas.drawText(data2, getHeaderRect().left + width1 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
                textPaint.color = getIndexColor()[2]
                canvas.drawText(data3, getHeaderRect().left + width1 + width2 + 20 + 20, getHeaderRect().bottom - fixTextHeight, textPaint)
            }
            KLineShowType.CANDLE_WITH_SAR -> {
                val data1 = "SAR:${dataEntity.sar.sar.indexFormat()}"
                textPaint.color = if (dataEntity.sar.rise) bitMartChartView.getGlobalProperties().riseColor() else bitMartChartView.getGlobalProperties().downColor()
                canvas.drawText(data1, getHeaderRect().left, getHeaderRect().bottom - fixTextHeight, textPaint)
            }
        }
    }


    override fun drawExtraInfo(renderRect: RectF, canvas: Canvas, extraInfoEntity: ChartExtraInfoEntity?) {
        drawUserPositions(renderRect, canvas, extraInfoEntity?.positions)
    }

    private fun drawUserPositions(renderRect: RectF, canvas: Canvas, positions: List<PositionInfo>?) {

        if (!properties.showExtraInfo) {
            return
        }

        if (positions == null || positions.isEmpty()) return

        val dataRange = bitMartChartView.getDataInScreenRange()
        if (dataRange.first == dataRange.second) {
            return
        }

        val chartData = bitMartChartView.getChartData()
        val min = chartData.subList(dataRange.first, dataRange.second + 1).minOf(rangeMinBy)
        val max = chartData.subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)

        positions.forEach {

            val pointY = (getDrawDataRect().top + (max - it.price) / (max - min) * getDrawDataRect().height()).toFloat()
            if (!getDrawDataRect().contains(PointF(getDrawDataRect().height() / 2 + getDrawDataRect().top, pointY))) {
                return
            }

            val price = it.price.priceFormat()
            val pnl = it.pnl.addPlusSign()

            linePaint.color = when (it.way) {
                PositionInfo.Way.LONG -> bitMartChartView.getGlobalProperties().riseColor()
                PositionInfo.Way.SHORT -> bitMartChartView.getGlobalProperties().downColor()
            }

            val pnlWidth = textPaint.measureText(pnl)
            val priceWidth = textPaint.measureText(price)
            val holdingWidth = textPaint.measureText(it.holding)

            /*??????????????????*/
            textPaint.color = bitMartChartView.getGlobalProperties().backgroundColor
            canvas.drawRoundRect(
                renderRect.left + 1,
                pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding,
                renderRect.left + 1 + pnlWidth + holdingWidth + textPadding * 4,
                pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding,
                4f,
                4f,
                textPaint,
            )
            /*???????????????*/
            canvas.drawRoundRect(
                renderRect.left + 1,
                pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding,
                renderRect.left + 1 + pnlWidth + holdingWidth + textPadding * 4,
                pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding,
                4f,
                4f,
                linePaint,
            )
            /*?????????????????????*/
            textPaint.color = if (pnl.startsWith("-")) bitMartChartView.getGlobalProperties().downColor() else bitMartChartView.getGlobalProperties().riseColor()
            textPaint.textSize = getFontSize()
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(pnl, rendererRect.left + 1 + textPadding, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)

            textPaint.color = when (it.way) {
                PositionInfo.Way.LONG -> bitMartChartView.getGlobalProperties().riseColor()
                PositionInfo.Way.SHORT -> bitMartChartView.getGlobalProperties().downColor()
            }

            /*????????????????????????*/
            canvas.drawRoundRect(
                rendererRect.left + 1 + textPadding * 2 + pnlWidth,
                pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding,
                renderRect.left + 1 + pnlWidth + holdingWidth + textPadding * 4,
                pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding,
                4f,
                4f,
                textPaint
            )

            /*????????????????????????*/
            canvas.drawRect(
                renderRect.right - priceWidth,
                pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding,
                renderRect.right,
                pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding,
                textPaint
            )
            /*??????????????????????????????*/
            val path = Path()
            path.moveTo(renderRect.right - textPadding * 1.5f - priceWidth, pointY)
            path.lineTo(renderRect.right - priceWidth, pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding)
            path.lineTo(renderRect.right - priceWidth, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding)
            path.close()
            canvas.drawPath(path, textPaint)

            textPaint.color = Color.WHITE
            /*??????????????????*/
            canvas.drawText(it.holding, rendererRect.left + 1 + textPadding * 3 + pnlWidth, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
            /*??????????????????*/
            canvas.drawText(price, rendererRect.right - priceWidth, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
            /*???????????????*/
            dashLinePaint.color = when (it.way) {
                PositionInfo.Way.LONG -> bitMartChartView.getGlobalProperties().riseColor()
                PositionInfo.Way.SHORT -> bitMartChartView.getGlobalProperties().downColor()
            }
            canvas.drawLine(renderRect.left + 1 + pnlWidth + holdingWidth + textPadding * 4, pointY, renderRect.right - textPadding * 1.5f - priceWidth, pointY, dashLinePaint)

        }
    }

    override fun drawAxis(renderRect: RectF, canvas: Canvas) {
        super.drawAxis(renderRect, canvas)
        //????????????
        drawAxisX(canvas, bitMartChartView.getChartData())
        //????????????
        drawAxisY(canvas)
        //??????????????????
        drawNowPrice(renderRect, canvas)
    }

    private fun drawNowPrice(renderRect: RectF, canvas: Canvas) {

        if (!properties.showNowPrice) {
            return
        }

        val dataRange = bitMartChartView.getDataInScreenRange()
        if (dataRange.first == dataRange.second) {
            return
        }
        val chartData = bitMartChartView.getChartData()

        val min = chartData.subList(dataRange.first, dataRange.second + 1).minOf(rangeMinBy)
        val max = chartData.subList(dataRange.first, dataRange.second + 1).maxOf(rangeMaxBy)
        val pointY = (getDrawDataRect().top + (max - chartData.last().close) / (max - min) * getDrawDataRect().height()).toFloat()

        if (!getDrawDataRect().contains(PointF(getDrawDataRect().height() / 2 + getDrawDataRect().top, pointY))) {
            return
        }
        linePaint.color = if (chartData.last().isRise) bitMartChartView.getGlobalProperties().riseColor() else bitMartChartView.getGlobalProperties().downColor()
        textPaint.color = if (chartData.last().isRise) bitMartChartView.getGlobalProperties().riseColor() else bitMartChartView.getGlobalProperties().downColor()
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.LEFT
        val nowPrice = chartData.last().close.priceFormat()
        val textWidth = textPaint.measureText(nowPrice)

        /*????????????????????????*/
        canvas.drawRect(
            renderRect.right - textWidth,
            pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding,
            renderRect.right,
            pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding,
            textPaint
        )
        /*??????????????????????????????*/
        val path = Path()
        path.moveTo(renderRect.right - textPadding * 1.5f - textWidth, pointY)
        path.lineTo(renderRect.right - textWidth, pointY - (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 - textPadding)
        path.lineTo(renderRect.right - textWidth, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4 + textPadding)
        path.close()
        canvas.drawPath(path, textPaint)

        textPaint.color = Color.WHITE
        canvas.drawText(nowPrice, rendererRect.right - textWidth, pointY + (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
        canvas.drawLine(renderRect.left, pointY, renderRect.right - textPadding * 1.5f - textWidth, pointY, linePaint)
    }

    private fun drawAxisY(canvas: Canvas) {
        val dataRange = bitMartChartView.getDataInScreenRange()

        if (dataRange.first == dataRange.second) {
            return
        }
        val subList = bitMartChartView.getChartData().subList(dataRange.first, dataRange.second + 1)
        val min = subList.minOf(rangeMinBy)
        val max = subList.maxOf(rangeMaxBy)

        val eachHeight = ((getDrawDataRect().height()) / (properties.showAxisYNum - 1))
        var startY = getDrawDataRect().top

        val eachDataHeight = (max - min) / (properties.showAxisYNum - 1)
        var dataStartY = max

        textPaint.color = bitMartChartView.getGlobalProperties().textColorSecondary()
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.RIGHT

        for (index in 1..properties.showAxisYNum) {
            val data = dataStartY.priceFormat()
            canvas.drawLine(rendererRect.left, startY, rendererRect.right - textPaint.measureText(data), startY, axisPaint)
            canvas.drawText(data, rendererRect.right, startY + abs(textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4, textPaint)
            startY += eachHeight
            dataStartY -= eachDataHeight
        }
        if (properties.showType != KLineShowType.TIME_LINE) {
            drawMaxAndMin(canvas, min, max, subList)
        }
    }

    private fun drawMaxAndMin(canvas: Canvas, min: Double, max: Double, dataList: List<ChartDataEntity>) {

        textPaint.color = bitMartChartView.getGlobalProperties().textColor()
        textPaint.textSize = getFontSize()
        val fixTextHeight = (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top) / 4

        var maxEntity: ChartDataEntity? = null
        var minEntity: ChartDataEntity? = null
        dataList.forEachIndexed { index, chartDataEntity ->
            if (index == 0) {
                maxEntity = chartDataEntity
                minEntity = chartDataEntity
            }
            if (chartDataEntity.low < minEntity!!.low) {
                minEntity = chartDataEntity
            }
            minEntity?.let {
                minEntity = if (chartDataEntity.low < it.low) chartDataEntity else it
            }
            maxEntity?.let {
                maxEntity = if (chartDataEntity.high > it.high) chartDataEntity else it
            }
        }

        if (properties.showMaxPrice) {
            //???????????????
            maxEntity?.also {
                var maxDataScreenPointX = bitMartChartView.getDataScreenPointXbyIndex(bitMartChartView.getChartData().indexOf(maxEntity))
                if (maxDataScreenPointX != null) {
                    maxDataScreenPointX += bitMartChartView.getGlobalProperties().eachWidth / 2 * bitMartChartView.getTotalScale()
                    val pointY = (getDrawDataRect().height() * (max - it.high) / (max - min)).toFloat() + getDrawDataRect().top

                    val lowPrice = it.high.priceFormat()
                    val textWidth = textPaint.measureText(lowPrice)
                    val lineWidth = textWidth / 1.6f * bitMartChartView.getTotalScale()

                    //????????????????????????
                    if ((maxDataScreenPointX - getDrawDataRect().left) < rendererRect.width() / 2) {
                        textPaint.textAlign = Paint.Align.LEFT
                        canvas.drawLine(maxDataScreenPointX, pointY, maxDataScreenPointX + lineWidth, pointY, textPaint)
                        canvas.drawText(lowPrice, maxDataScreenPointX + lineWidth, pointY + fixTextHeight, textPaint)
                    } else {
                        textPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawLine(maxDataScreenPointX, pointY, maxDataScreenPointX - lineWidth, pointY, textPaint)
                        canvas.drawText(lowPrice, maxDataScreenPointX - lineWidth, pointY + fixTextHeight, textPaint)
                    }
                }
            }
        }

        if (properties.showMinPrice) {
            //???????????????
            minEntity?.also {
                var minDataScreenPointX = bitMartChartView.getDataScreenPointXbyIndex(bitMartChartView.getChartData().indexOf(minEntity))
                if (minDataScreenPointX != null) {
                    minDataScreenPointX += bitMartChartView.getGlobalProperties().eachWidth / 2 * bitMartChartView.getTotalScale()
                    val pointY = (getDrawDataRect().height() * (max - it.low) / (max - min)).toFloat() + getDrawDataRect().top

                    val lowPrice = it.low.priceFormat()
                    val textWidth = textPaint.measureText(lowPrice)
                    val lineWidth = textWidth / 1.6f * bitMartChartView.getTotalScale()

                    //????????????????????????
                    if ((minDataScreenPointX - getDrawDataRect().left) < rendererRect.width() / 2) {
                        textPaint.textAlign = Paint.Align.LEFT
                        canvas.drawLine(minDataScreenPointX, pointY, minDataScreenPointX + lineWidth, pointY, textPaint)
                        canvas.drawText(lowPrice, minDataScreenPointX + lineWidth, pointY + fixTextHeight, textPaint)
                    } else {
                        textPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawLine(minDataScreenPointX, pointY, minDataScreenPointX - lineWidth, pointY, textPaint)
                        canvas.drawText(lowPrice, minDataScreenPointX - lineWidth, pointY + fixTextHeight, textPaint)
                    }
                }
            }
        }
    }

    private fun drawAxisX(canvas: Canvas, chartData: List<ChartDataEntity>) {

        textPaint.color = bitMartChartView.getGlobalProperties().textColorSecondary()
        textPaint.textSize = getFontSize()
        textPaint.textAlign = Paint.Align.CENTER

        val tempTime = DateFormat.format(properties.dataFormat, System.currentTimeMillis()).toString()
        val tempTimeWidth = textPaint.measureText(tempTime)
        //?????????????????????????????????
        val maxTimeCount = (rendererRect.width() / tempTimeWidth).toInt()
        //????????????????????????
        val timeCount = min(maxTimeCount, properties.showAxisXNum)
        //
        val eachWidth = (rendererRect.width() - tempTimeWidth) / (timeCount - 1)
        var timeStartX = tempTimeWidth / 2
        for (index in 1..timeCount) {
            val dataIndex = bitMartChartView.getDataIndexByScreenPointX(timeStartX)
            val currentData = chartData.getOrNull(dataIndex ?: -1)
            if (dataIndex != null && currentData != null) {
                canvas.drawText(DateFormat.format(properties.dataFormat, currentData.time).toString(), timeStartX, getDateRect().bottom, textPaint)
                timeStartX += eachWidth
            }
        }
    }

    override fun drawData(dataRect: RectF, canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity) {
        when (properties.showType) {
            KLineShowType.TIME_LINE -> {
                linePaint.color = if (bitMartChartView.getGlobalProperties().isDarkMode) properties.timeLineColor.dark else properties.timeLineColor.light
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.close, curStartX, curData.close)
            }
            KLineShowType.CANDLE_WITH_MA, KLineShowType.CANDLE_WITH_EMA, KLineShowType.CANDLE_WITH_BOLL, KLineShowType.CANDLE_WITH_SAR -> drawCandle(canvas, min, max, itemWidth, preStartX, preData, curStartX, curData)
        }
    }


    private fun getIndexColor(): List<Int> {
        return if (bitMartChartView.getGlobalProperties().isDarkMode) properties.indexColor.dark else properties.indexColor.light
    }

    private fun drawCandle(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: ChartDataEntity?, curStartX: Float, curData: ChartDataEntity) {

        //???????????????
        val curCloseHeight = ((max - curData.close) / (max - min) * getDrawDataRect().height()).toFloat()
        val curOpenHeight = ((max - curData.open) / (max - min) * getDrawDataRect().height()).toFloat()
        val candleCloseTop = if (curData.isRise) getDrawDataRect().top + curCloseHeight else getDrawDataRect().top + curOpenHeight
        val candleCloseBottom = candleCloseTop + max(abs(curCloseHeight - curOpenHeight), CANDLE_MIN_HEIGHT)
        canvas.drawRect(curStartX, candleCloseTop, curStartX + itemWidth, candleCloseBottom, if (curData.isRise) risePaint else downPaint)

        //????????????????????????
        val curHighHeight = ((max - curData.high) / (max - min) * getDrawDataRect().height()).toFloat()
        val curLowHeight = ((max - curData.low) / (max - min) * getDrawDataRect().height()).toFloat()
        val candleHighTop = getDrawDataRect().top + curHighHeight
        val candleHighBottom = candleHighTop + abs(curHighHeight - curLowHeight)
        canvas.drawRect(
            curStartX + itemWidth / 2 - itemWidth * CANDLE_CENTER_WIDTH_RATIO / 2,
            candleHighTop,
            curStartX + itemWidth / 2 + itemWidth * CANDLE_CENTER_WIDTH_RATIO / 2,
            candleHighBottom,
            if (curData.isRise) risePaint else downPaint
        )

        //??????????????????????????????
        textPaint.textSize = getFontSize()
        textPaint.color = bitMartChartView.getGlobalProperties().textColor()
        textPaint.textAlign = Paint.Align.LEFT

        //???????????????
        when (properties.showType) {
            KLineShowType.CANDLE_WITH_MA -> {
                linePaint.color = getIndexColor()[0]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ma?.get(0), curStartX, curData.ma[0])
                linePaint.color = getIndexColor()[1]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ma?.get(1), curStartX, curData.ma[1])
                linePaint.color = getIndexColor()[2]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ma?.get(2), curStartX, curData.ma[2])
            }
            KLineShowType.CANDLE_WITH_EMA -> {
                linePaint.color = getIndexColor()[0]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ema?.get(0), curStartX, curData.ema[0])
                linePaint.color = getIndexColor()[1]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ema?.get(1), curStartX, curData.ema[1])
                linePaint.color = getIndexColor()[2]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.ema?.get(2), curStartX, curData.ema[2])
            }
            KLineShowType.CANDLE_WITH_BOLL -> {
                linePaint.color = getIndexColor()[0]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.boll?.get(0), curStartX, curData.boll[0])
                linePaint.color = getIndexColor()[1]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.boll?.get(1), curStartX, curData.boll[1])
                linePaint.color = getIndexColor()[2]
                drawLine(canvas, min, max, itemWidth, preStartX, preData?.boll?.get(2), curStartX, curData.boll[2])
            }
            KLineShowType.CANDLE_WITH_SAR -> {
                drawDot(canvas, min, max, itemWidth, preStartX, preData?.sar, curStartX, curData.sar)
            }
            else -> {

            }
        }
    }

    private fun drawDot(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: SarEntity?, curStartX: Float, curData: SarEntity) {

        if (preData == null || preStartX == null) {
            return
        }

        val difHeight = (max - min)
        val curDataHeight = max - curData.sar

        linePaint.color = if (curData.rise) bitMartChartView.getGlobalProperties().riseColor() else bitMartChartView.getGlobalProperties().downColor()

        if (preData.rise != curData.rise) {
            linePaint.color = bitMartChartView.getGlobalProperties().textColor()
        }

        val radius = itemWidth / 2

        val left = curStartX + itemWidth / 2 - radius / 2
        val right = curStartX + itemWidth / 2 + radius / 2
        val top = (getDrawDataRect().top + (curDataHeight.toFloat() / difHeight.toFloat() * getDrawDataRect().height())) - radius / 2 * bitMartChartView.getTotalScale()
        val bottom = (getDrawDataRect().top + (curDataHeight.toFloat() / difHeight.toFloat() * getDrawDataRect().height())) + radius / 2 * bitMartChartView.getTotalScale()

        canvas.drawOval(left, top, right, bottom, linePaint)
    }

    private fun drawLine(canvas: Canvas, min: Double, max: Double, itemWidth: Float, preStartX: Float?, preData: Double?, curStartX: Float, curData: Double?) {

        if (preData == null || preStartX == null || curData == null) {
            return
        }
        val difHeight = (max - min)
        val preDataHeight = max - preData
        val curDataHeight = max - curData

        canvas.drawLine(
            preStartX + itemWidth / 2,
            (getDrawDataRect().top + (preDataHeight.toFloat() / difHeight.toFloat() * getDrawDataRect().height())),
            curStartX + itemWidth / 2,
            (getDrawDataRect().top + (curDataHeight.toFloat() / difHeight.toFloat() * getDrawDataRect().height())),
            linePaint
        )
    }
}