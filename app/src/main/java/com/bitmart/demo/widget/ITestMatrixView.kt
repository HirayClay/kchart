package com.bitmart.demo.widget

import android.content.Context
import android.graphics.Matrix

interface ITestMatrixView {

    fun getContext(): Context

    fun getChartMatrix(): Matrix

    fun getData(): List<Int>

    fun getDisplayWidth(): Float

    fun getInitItemWidth(): Float
}