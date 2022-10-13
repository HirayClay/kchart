@file:Suppress("unused")

package com.bitmart.kchart.properties

import com.bitmart.kchart.default.*
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
    val priceAccuracy: Int,
    //指数精度
    val indexAccuracy: Int,
    //数量精度
    val countAccuracy: Int,
    //右边的距离
    val rightAxisWidth: Float,
    //绘制空数据
    val drawEmptyView: Boolean,
    //国际化语言配置项
    val chartLanguage: ChartLanguage,
    //阳线
    private val riseColor: Int,
    private val riseDarkColor: Int,
    //阴线
    private val downColor: Int,
    private val downDarkColor: Int,
    //文字
    private val textColor: Int,
    private val textDarkColor: Int,
    //高亮
    private val highlightingColor: Int,
    private val highlightingDarkColor: Int,
) {

    //当前可显示条数宽度
    var eachWidth =-1f

    //每个条目的宽度
    var itemWidth =-1f

    //条目间隔宽度
    var spaceWidth =-1f

    //是否是黑暗模式
    var isDarkMode by Delegates.notNull<Boolean>()

    var backgroundColor by Delegates.notNull<Int>()

    fun textColor() = if (isDarkMode) textDarkColor else textColor

    fun riseColor() = if (isDarkMode) riseDarkColor else riseColor

    fun downColor() = if (isDarkMode) downDarkColor else downColor

    fun highlightingColor() = if (isDarkMode) highlightingDarkColor else highlightingColor

    companion object {
        fun fromProperties(properties: BitMartChartProperties): GlobalProperties {
            return GlobalProperties(
                barSpaceRatio = properties.barSpaceRatio,
                pageShowNum = properties.getStandardShowNum(),
                headerRatio = properties.headerRatio,
                pageMaxNumber = properties.pageMaxNumber,
                pageMinNumber = properties.pageMinNumber,
                priceAccuracy = properties.priceAccuracy,
                indexAccuracy = properties.indexAccuracy,
                countAccuracy = properties.countAccuracy,
                rightAxisWidth = properties.rightAxisWidth,
                drawEmptyView = properties.drawEmptyView,
                chartLanguage = properties.chartLanguage,
                riseColor = properties.riseColor,
                riseDarkColor = properties.riseDarkColor,
                downColor = properties.downColor,
                downDarkColor = properties.downDarkColor,
                textColor = properties.textColor,
                textDarkColor = properties.textDarkColor,
                highlightingColor = properties.highlightingColor,
                highlightingDarkColor = properties.highlightingDarkColor,
            )
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
    var riseColor: Int = DEFAULT_UP_COLOR,
    //黑暗主题阳线
    var riseDarkColor: Int = DEFAULT_UP_COLOR,
    //阴线
    var downColor: Int = DEFAULT_DOWN_COLOR,
    //黑暗主题阴线
    var downDarkColor: Int = DEFAULT_DOWN_COLOR,
    //文字颜色
    var textColor: Int = DEFAULT_TEXT_COLOR,
    //黑暗主题文字颜色
    var textDarkColor: Int = DEFAULT_TEXT_DARK_COLOR,
    //高亮颜色
    var highlightingColor: Int = DEFAULT_HIGH_LIGHTING_COLOR,
    //黑暗主题高亮颜色
    var highlightingDarkColor: Int = DEFAULT_HIGH_LIGHTING_DARK_COLOR,
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
    var timeLineColor: Int = DEFAULT_TIME_LINE_COLOR,
    //黑暗主题TimeLine颜色
    var timeLineDarkColor: Int = DEFAULT_TIME_LINE_DARK_COLOR,
    //指数颜色
    var indexColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
    //黑暗主题指数颜色；
    var indexDarkColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
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
    var indexColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
    //黑暗主题指数颜色；
    var indexDarkColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)
) : IRendererProperties

data class MacdRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
    //黑暗主题指数颜色；
    var indexDarkColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)
) : IRendererProperties

data class KdjRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
    //黑暗主题指数颜色；
    var indexDarkColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)
) : IRendererProperties

data class RsiRendererProperties constructor(
    override var heightRatio: Float = 1f,
    //指数颜色
    var indexColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR),
    //黑暗主题指数颜色；
    var indexDarkColor: List<Int> = listOf(DEFAULT_INDEX1_COLOR, DEFAULT_INDEX2_COLOR, DEFAULT_INDEX3_COLOR)
) : IRendererProperties