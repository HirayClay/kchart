@file:Suppress("unused")

package com.bitmart.kchart.properties

import com.bitmart.kchart.default.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.properties.Delegates


class GlobalProperties private constructor(
    //柱子之间的空间占比柱子宽度
    val barSpaceRatio: Float,
    //默认首屏展示的数据数量
    val pageShowNum: Int,
    //一屏最大显示的数据条数
    val pageMaxNumber: Int,
    //一屏最小显示的数据条数
    val pageMinNumber: Int,
    //头部所占的比例
    val headerRatio: Float,
    //价格精度
    val priceAccuracyFormat: DecimalFormat,
    //指数精度
    val indexAccuracyFormat: DecimalFormat,
    //数量精度
    val countAccuracyFormat: DecimalFormat,
    //右边的距离
    val rightAxisWidth: Float,
    //绘制空数据
    val drawEmptyView: Boolean,
    //国际化语言配置项
    val chartLanguage: ChartLanguage,
    //阳线
    private val riseColor: ThemeColor,
    //阴线
    private val downColor: ThemeColor,
    //文字
    private val textColor: ThemeColor,

    private val textColorSecondary: ThemeColor,
    //高亮
    private val highlightingColor: ThemeColor,
) {

    //当前可显示条数宽度
    var eachWidth = -1f

    //每个条目的宽度
    var itemWidth = -1f

    //条目间隔宽度
    var spaceWidth = -1f

    //是否是黑暗模式
    var isDarkMode by Delegates.notNull<Boolean>()

    var backgroundColor by Delegates.notNull<Int>()

    fun textColor() = if (isDarkMode) textColor.dark else textColor.light

    fun textColorSecondary() = if (isDarkMode) textColorSecondary.dark else textColorSecondary.light

    fun riseColor() = if (isDarkMode) riseColor.dark else riseColor.light

    fun downColor() = if (isDarkMode) downColor.dark else downColor.light

    fun highlightingColor() = if (isDarkMode) highlightingColor.dark else highlightingColor.light

    companion object {
        fun fromProperties(properties: BitMartChartProperties): GlobalProperties {
            return GlobalProperties(
                barSpaceRatio = properties.barSpaceRatio,
                pageShowNum = properties.getStandardShowNum(),
                headerRatio = properties.headerRatio,
                pageMaxNumber = properties.pageMaxNumber,
                pageMinNumber = properties.pageMinNumber,
                priceAccuracyFormat = getDecimalFormat(properties.priceAccuracy),
                indexAccuracyFormat = getDecimalFormat(properties.indexAccuracy),
                countAccuracyFormat = getDecimalFormat(properties.countAccuracy),
                rightAxisWidth = properties.rightAxisWidth,
                drawEmptyView = properties.drawEmptyView,
                chartLanguage = properties.chartLanguage,
                riseColor = properties.riseColor,
                downColor = properties.downColor,
                textColor = properties.textColor,
                textColorSecondary = properties.textColorSecondary,
                highlightingColor = properties.highlightingColor,
            )
        }

        private fun getDecimalFormat(accuracy: Int): DecimalFormat {
            var pattern = ",##0"
            for (index in 0 until accuracy) {
                pattern += if (index == 0) ".0" else "0"
            }
            return DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ENGLISH)).apply { roundingMode = RoundingMode.DOWN }
        }
    }

}

fun BitMartChartProperties.getStandardShowNum(): Int {
    if (this.pageShowNum < this.pageMinNumber) return this.pageMinNumber
    if (this.pageShowNum > this.pageMaxNumber) return this.pageMaxNumber
    return this.pageShowNum
}

data class BitMartChartProperties(
    //kline
    var kLineRendererProperties: KLineRendererProperties = KLineRendererProperties(),
    //vol
    var volRendererProperties: VolRendererProperties? = null,
    //macd
    var macdRendererProperties: MacdRendererProperties? = null,
    //kdj
    var kdjRendererProperties: KdjRendererProperties? = null,
    //rsi
    var rsiRendererProperties: RsiRendererProperties? = null,
    //价格精度
    var priceAccuracy: Int = DEFAULT_PRICE_ACCURACY,
    //指数精度
    var indexAccuracy: Int = DEFAULT_INDEX_ACCURACY,
    //数量精度
    var countAccuracy: Int = DEFAULT_COUNT_ACCURACY,
    //右边坐标轴的宽度
    var rightAxisWidth: Float = DEFAULT_RIGHT_AXIS_WIDTH,
    //柱子之间的空间占比柱子宽度
    var barSpaceRatio: Float = DEFAULT_BAR_SPACE_RATIO,
    //默认首屏展示的数据数量
    var pageShowNum: Int = DEFAULT_SHOW_PAGE_NUM,
    //头部标题占比
    var headerRatio: Float = DEFAULT_HEADER_RATIO,
    //一屏最大显示的数据条数
    var pageMaxNumber: Int = DEFAULT_MAX_PAGE_SHOW_NUM,
    //一屏最小显示的数据条数
    var pageMinNumber: Int = DEFAULT_MIN_PAGE_SHOW_NUM,
    //阳线
    var riseColor: ThemeColor = ThemeColor(DEFAULT_UP_COLOR, DEFAULT_UP_COLOR),
    //阴线
    var downColor: ThemeColor = ThemeColor(DEFAULT_DOWN_COLOR, DEFAULT_DOWN_COLOR),
    //文字颜色
    var textColor: ThemeColor = ThemeColor(DEFAULT_TEXT_COLOR, DEFAULT_TEXT_DARK_COLOR),
    //文字次级颜色
    var textColorSecondary: ThemeColor = ThemeColor(DEFAULT_TEXT_DARK_COLOR_SECONDARY, DEFAULT_TEXT_COLOR_SECONDARY),
    //高亮颜色
    var highlightingColor: ThemeColor = ThemeColor(DEFAULT_HIGH_LIGHTING_COLOR, DEFAULT_HIGH_LIGHTING_DARK_COLOR),
    //绘制空数据占位
    var drawEmptyView: Boolean = false,
    //国际化语言配置项
    var chartLanguage: ChartLanguage = ChartLanguage.english(),
) {
    fun deepCopy(): BitMartChartProperties {
        return this.copy(
            kLineRendererProperties = kLineRendererProperties.copy(),
            volRendererProperties = volRendererProperties?.copy(),
            macdRendererProperties = macdRendererProperties?.copy(),
            kdjRendererProperties = kdjRendererProperties?.copy(),
            rsiRendererProperties = rsiRendererProperties?.copy(),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitMartChartProperties

        if (kLineRendererProperties != other.kLineRendererProperties) return false
        if (volRendererProperties != other.volRendererProperties) return false
        if (macdRendererProperties != other.macdRendererProperties) return false
        if (kdjRendererProperties != other.kdjRendererProperties) return false
        if (rsiRendererProperties != other.rsiRendererProperties) return false
        if (priceAccuracy != other.priceAccuracy) return false
        if (indexAccuracy != other.indexAccuracy) return false
        if (countAccuracy != other.countAccuracy) return false
        if (rightAxisWidth != other.rightAxisWidth) return false
        if (barSpaceRatio != other.barSpaceRatio) return false
        if (pageShowNum != other.pageShowNum) return false
        if (headerRatio != other.headerRatio) return false
        if (pageMaxNumber != other.pageMaxNumber) return false
        if (pageMinNumber != other.pageMinNumber) return false
        if (riseColor != other.riseColor) return false
        if (downColor != other.downColor) return false
        if (textColor != other.textColor) return false
        if (textColorSecondary != other.textColorSecondary) return false
        if (highlightingColor != other.highlightingColor) return false
        if (drawEmptyView != other.drawEmptyView) return false
        if (chartLanguage != other.chartLanguage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kLineRendererProperties.hashCode()
        result = 31 * result + (volRendererProperties?.hashCode() ?: 0)
        result = 31 * result + (macdRendererProperties?.hashCode() ?: 0)
        result = 31 * result + (kdjRendererProperties?.hashCode() ?: 0)
        result = 31 * result + (rsiRendererProperties?.hashCode() ?: 0)
        result = 31 * result + priceAccuracy
        result = 31 * result + indexAccuracy
        result = 31 * result + countAccuracy
        result = 31 * result + rightAxisWidth.hashCode()
        result = 31 * result + barSpaceRatio.hashCode()
        result = 31 * result + pageShowNum
        result = 31 * result + headerRatio.hashCode()
        result = 31 * result + pageMaxNumber
        result = 31 * result + pageMinNumber
        result = 31 * result + riseColor.hashCode()
        result = 31 * result + downColor.hashCode()
        result = 31 * result + textColor.hashCode()
        result = 31 * result + textColorSecondary.hashCode()
        result = 31 * result + highlightingColor.hashCode()
        result = 31 * result + drawEmptyView.hashCode()
        result = 31 * result + chartLanguage.hashCode()
        return result
    }


}

interface IRendererProperties {
    //高度权重
    var heightRatio: Float
}

class ChartLanguage(
    val date: String = "Date",
    val open: String = "Open",
    val close: String = "Close",
    val high: String = "High",
    val low: String = "Low",
    val change: String = "Change",
    val changeRatio: String = "Change%",
    val vol: String = "Vol",
) {

    companion object {
        fun chinese(): ChartLanguage {
            return ChartLanguage(
                date = "时间",
                open = "开盘价",
                close = "收盘价",
                high = "最高",
                low = "最低",
                change = "涨跌额",
                changeRatio = "涨跌幅",
                vol = "成交量",
            )
        }

        fun english(): ChartLanguage {
            return ChartLanguage()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChartLanguage
        if (date != other.date) return false
        if (open != other.open) return false
        if (close != other.close) return false
        if (high != other.high) return false
        if (low != other.low) return false
        if (change != other.change) return false
        if (changeRatio != other.changeRatio) return false
        if (vol != other.vol) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + open.hashCode()
        result = 31 * result + close.hashCode()
        result = 31 * result + high.hashCode()
        result = 31 * result + low.hashCode()
        result = 31 * result + change.hashCode()
        result = 31 * result + changeRatio.hashCode()
        result = 31 * result + vol.hashCode()
        return result
    }
}

data class KLineRendererProperties constructor(
    override var heightRatio: Float = 3.5f,
    //X轴数量
    var showAxisXNum: Int = DEFAULT_SHOW_AXIS_X_NUM,
    //Y轴数量
    var showAxisYNum: Int = DEFAULT_SHOW_AXIS_Y_NUM,
    //日期格式化工具
    var dataFormat: String = DEFAULT_DATE_FORMAT,
    //显示的类型
    var showType: KLineShowType = KLineShowType.CANDLE_WITH_MA,
    //TimeLine颜色
    var timeLineColor: ThemeColor = ThemeColor(DEFAULT_TIME_LINE_COLOR, DEFAULT_TIME_LINE_DARK_COLOR),
    //指数颜色
    var indexColor: ThemeColorList = ThemeColorList(listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR), listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)),
    //是否显示当前价格
    var showNowPrice: Boolean = true,
    //是否显示最高价格
    var showMaxPrice: Boolean = true,
    //是否显示最低价格
    var showMinPrice: Boolean = true,
    //是否显示额外信息
    var showExtraInfo: Boolean = true,
) : IRendererProperties

enum class KLineShowType {
    TIME_LINE,
    CANDLE_WITH_MA,
    CANDLE_WITH_EMA,
    CANDLE_WITH_BOLL,
    CANDLE_WITH_SAR,
}

data class VolRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: ThemeColorList = ThemeColorList(listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR), listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)),
) : IRendererProperties

data class MacdRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: ThemeColorList = ThemeColorList(listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR), listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)),
) : IRendererProperties

data class KdjRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: ThemeColorList = ThemeColorList(listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR), listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)),
) : IRendererProperties

data class RsiRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: ThemeColorList = ThemeColorList(listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR), listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)),
) : IRendererProperties

abstract class ThemeData<T> {
    abstract val light: T
    abstract val dark: T
}

data class ThemeColor(override val light: Int, override val dark: Int) : ThemeData<Int>()
data class ThemeColorList(override val light: List<Int>, override val dark: List<Int>) : ThemeData<List<Int>>()