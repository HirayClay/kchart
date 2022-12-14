@file:Suppress("unused")

package com.bitmart.kchart.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.bitmart.kchart.BuildConfig
import java.math.BigDecimal
import java.util.*


fun Double.abs(): Double {
    return kotlin.math.abs(this)
}

fun Float.abs(): Float {
    return kotlin.math.abs(this)
}

fun Int.abs(): Int {
    return kotlin.math.abs(this)
}

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

private val zeroRegex = Regex("^[0]+\\.*[0]*$")
private val additiveRegex = Regex("^[0-9]+\\.*[0-9]*$")

internal fun String.addPlusSign(): String {

    if (this.matches(zeroRegex)) {
        return this
    }

    if (this.matches(additiveRegex)) {
        return "+$this"
    }

    return this
}

fun Context.isDarkMode() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) resources.configuration.isNightModeActive else resources.configuration.uiMode == 0x21

fun Context.getBackgroundColor(): Int {
    val attributes = this.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
    val color = attributes.getColor(0, 0xFFFFFF)
    attributes.recycle()
    return color
}

internal fun Any.debug(message: Any? = null, level: Int = Log.INFO, throwable: Throwable? = null) {
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

internal fun Any.debug(vararg msg: Any?, level: Int = Log.INFO, throwable: Throwable? = null) {
    if (!BuildConfig.DEBUG) return
    val message = msg.joinToString(separator = "    ----    ") { it.toString() }
    when (level) {
        Log.VERBOSE -> Log.v(this::class.java.simpleName, message, throwable)
        Log.DEBUG -> Log.d(this::class.java.simpleName, message, throwable)
        Log.INFO -> Log.i(this::class.java.simpleName, message, throwable)
        Log.WARN -> Log.w(this::class.java.simpleName, message, throwable)
        Log.ERROR -> Log.e(this::class.java.simpleName, message, throwable)
        Log.ASSERT -> Log.println(level, this::class.java.simpleName, "$msg \\n ${throwable.toString()}")
    }
}