package com.bitmart.kchart.util

import android.content.Context
import android.os.Build

fun Number.dp2px(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return this.toFloat() * scale + 0.5f
}

fun Number.px2dp(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return this.toFloat() / scale + 0.5f
}

fun Number.sp2px(context: Context): Float {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return this.toFloat() * fontScale + 0.5f
}

fun Number.px2sp(context: Context): Float {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return this.toFloat() / fontScale + 0.5f
}

fun Number?.toStringAsFixed(accuracy: Int): String {
    val num = this ?: 0.0
    if (accuracy <= 0) return num.toInt().toString()
    val format = "%.${accuracy}f"
    return String.format(format, num)
}

fun Context.isDarkMode() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) resources.configuration.isNightModeActive else resources.configuration.uiMode == 0x21

fun Context.getBackgroundColor(): Int {
    val attributes = this.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
    val color = attributes.getColor(0, 0xFFFFFF)
    attributes.recycle()
    return color
}