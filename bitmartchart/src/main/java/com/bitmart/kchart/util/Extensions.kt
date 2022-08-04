package com.bitmart.kchart.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.bitmart.kchart.BuildConfig

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
    val attributes = this.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
    val color = attributes.getColor(0, 0xFFFFFF)
    attributes.recycle()
    return color
}

fun Any.debug(message: Any? = null, level: Int = Log.INFO, throwable: Throwable? = null) {
    if (!BuildConfig.DEBUG) return
    val msg = message ?: "NULL"
    when (level) {
        Log.VERBOSE -> Log.v(this::class.java.simpleName, msg.toString(), throwable)
        Log.DEBUG -> Log.d(this::class.java.simpleName, msg.toString(), throwable)
        Log.INFO -> Log.i(this::class.java.simpleName, msg.toString(), throwable)
        Log.WARN -> Log.w(this::class.java.simpleName, msg.toString(), throwable)
        Log.ERROR -> Log.e(this::class.java.simpleName, msg.toString(), throwable)
        Log.ASSERT -> Log.println(level, this::class.java.simpleName, "$msg \\n ${throwable.toString()}")
    }
}

fun Any.debug(vararg msg: Any?, level: Int = Log.INFO, throwable: Throwable? = null) {
    if (!BuildConfig.DEBUG) return
    val message = msg.joinToString(separator = "  ====  ") { it.toString() }
    when (level) {
        Log.VERBOSE -> Log.v(this::class.java.simpleName, message, throwable)
        Log.DEBUG -> Log.d(this::class.java.simpleName, message, throwable)
        Log.INFO -> Log.i(this::class.java.simpleName, message, throwable)
        Log.WARN -> Log.w(this::class.java.simpleName, message, throwable)
        Log.ERROR -> Log.e(this::class.java.simpleName, message, throwable)
        Log.ASSERT -> Log.println(level, this::class.java.simpleName, "$msg \\n ${throwable.toString()}")
    }
}