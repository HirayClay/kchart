package com.bitmart.kchart

import org.junit.Test

import org.junit.Assert.*

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
}