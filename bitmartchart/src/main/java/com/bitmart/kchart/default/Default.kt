package com.bitmart.kchart.default

import android.graphics.Color
import com.bitmart.kchart.properties.BitMartChartProperties
import com.bitmart.kchart.properties.KLineRendererProperties
import com.bitmart.kchart.properties.VolRendererProperties

const val DEFAULT_CHART_RATIO_HEIGHT = 90
const val DEFAULT_BAR_SPACE_RATIO = 0.3f

const val DEFAULT_SHOW_PAGE_NUM = 50
const val DEFAULT_SHOW_AXIS_X_NUM = 5
const val DEFAULT_SHOW_AXIS_Y_NUM = 5
const val DEFAULT_HEADER_RATIO = 0.1f
const val DEFAULT_MAX_SCALE_RATIO = 2.0f
const val DEFAULT_MIN_SCALE_RATIO = 0.3f
const val DEFAULT_PRICE_ACCURACY = 2
const val DEFAULT_INDEX_ACCURACY = 4
const val DEFAULT_COUNT_ACCURACY = 0
const val DEFAULT_RIGHT_AXIS_WIDTH = 0f

val DEFAULT_UP_COLOR = Color.parseColor("#FF58B038")
val DEFAULT_DOWN_COLOR = Color.parseColor("#FFED6536")

const val DEFAULT_DATE_FORMAT = "yy-MM-dd HH:mm"
const val DEFAULT_TEXT_COLOR = Color.DKGRAY
const val DEFAULT_TEXT_DARK_COLOR = Color.LTGRAY
val DEFAULT_HIGH_LIGHTING_COLOR = Color.parseColor("#11000000")
val DEFAULT_HIGH_LIGHTING_DARK_COLOR = Color.parseColor("#33FFFFFF")

val DEFAULT_TIME_LINE_COLOR = Color.parseColor("#FF222F44")
val DEFAULT_TIME_LINE_DARK_COLOR = Color.parseColor("#FF222F44")

val DEFAULT_INDEX1_COLOR = Color.parseColor("#FFC9B885")
val DEFAULT_INDEX2_COLOR = Color.parseColor("#FF6CB0A6")
val DEFAULT_INDEX3_COLOR = Color.parseColor("#FF9979C6")

val DEFAULT_BIT_MART_CHART_PROPERTIES = BitMartChartProperties(chartRendererProperties = mutableListOf(KLineRendererProperties(), VolRendererProperties()))