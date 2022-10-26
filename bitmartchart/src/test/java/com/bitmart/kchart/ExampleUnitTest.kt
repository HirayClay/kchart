package com.bitmart.kchart

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(2.0.toStringAsFixed(0), "2")
    }

    private fun Number?.toStringAsFixed(accuracy: Int): String {
        val num = this ?: 0.0
        if (accuracy <= 0) return num.toInt().toString()
        val format = "%.${accuracy}f"
        return String.format(format, num)
    }

    @Test
    fun test_decimal_format() {
        val format = getDecimalFormat(3)


        assertEquals(format.format(9.99), "9.990")
        assertEquals(format.format(99.99), "99.990")
        assertEquals(format.format(999.99), "999.990")
        assertEquals(format.format(9999.99), "9,999.990")
        assertEquals(format.format(99999.99), "99,999.990")
        assertEquals(format.format(999999.99), "999,999.990")
        assertEquals(format.format(9999999.99), "9,999,999.990")


        assertEquals(format.format(9.9999), "9.999")
        assertEquals(format.format(99.9999), "99.999")
        assertEquals(format.format(999.9999), "999.999")
        assertEquals(format.format(9999.9999), "9,999.999")
        assertEquals(format.format(99999.9999), "99,999.999")
        assertEquals(format.format(999999.9999), "999,999.999")
        assertEquals(format.format(9999999.9999), "9,999,999.999")

        assertEquals(format.format(0.9999), "0.999")

    }

    private fun getDecimalFormat(accuracy: Int): DecimalFormat {
        var pattern = ",##0"

        for (index in 0 until accuracy) {
            pattern += if (index == 0) ".0" else "0"
        }

        println(pattern)

        return DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ENGLISH)).apply { roundingMode = RoundingMode.DOWN }
    }
}