package com.bitmart.demo.widget

import android.graphics.Matrix

class AreaCalcHelper(private val view: ITestMatrixView) {

    companion object {
        private const val FIXED_COUNT = 1
    }

    private val temp9Array by lazy { FloatArray(9) }

    fun getDrawDataRange(): List<Int> {
        return view.getData()
    }


    fun calcInitTranslate(): Float {
        val scaledItemWidth = view.getInitItemWidth() * getTotalScale()
        return -(view.getData().size * scaledItemWidth) + view.getDisplayWidth()
    }

    fun getTotalScale(): Float {
        view.getChartMatrix().getValues(temp9Array)
        return temp9Array[Matrix.MSCALE_X]
    }

    fun getTotalTranslate(): Float {
        view.getChartMatrix().getValues(temp9Array)
        return temp9Array[Matrix.MTRANS_X]
    }
}