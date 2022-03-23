package com.bitmart.demo.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TestMatrixView : View, TouchHelperListener, ITestMatrixView {

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    //********************************************************************初始********************************************************************//

    private val toucheHelper by lazy { TouchHelper(this) }
    private val areaCalcHelper by lazy { AreaCalcHelper(this) }

    private var chartData = mutableListOf<Int>()


    fun setData(data: List<Int>) {
        chartData.clear()
        chartData.addAll(data)
        this.post {
            canvasMatrix.setTranslate(areaCalcHelper.calcInitTranslate(), 0f)
            invalidate()
        }
    }

    fun addData(data: List<Int>) {
        chartData.addAll(data)
        this.post {
            invalidate()
        }
    }

    private fun initView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int? = null) {
        setWillNotDraw(false)
    }

    //********************************************************************绘制********************************************************************//


    private val canvasMatrix by lazy { Matrix() }

    private val ratio = 0.5f
    private val maxScale = 2.5f
    private val minScale = 0.5f

    private var pageSize = 40

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.setMatrix(canvasMatrix)
        val data = areaCalcHelper.getDrawDataRange().reversed()

        if (data.isEmpty()) return

        val eachWidth = getInitItemWidth()
        val itemWidth = eachWidth / (ratio + 1)
        val spaceWidth = itemWidth * ratio

        var startX = spaceWidth / 2

        val max = data.maxOrNull()!!

        data.forEach { i ->
            canvas.drawRect(startX, (i.toFloat() / max * height), startX + itemWidth, 0f, paint)
            startX += (spaceWidth + itemWidth)
        }
    }
    //********************************************************************事件********************************************************************//

    private var focusX = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return toucheHelper.onToucheEvent(event, this)
    }

    override fun onHorizontalScroll(distanceX: Float) {
        canvasMatrix.postTranslate(-distanceX, 0f)
        invalidate()
    }

    override fun onTouchScaling(scaleFactor: Float) {
        val totalScale = areaCalcHelper.getTotalScale()
        if (totalScale * scaleFactor < maxScale && totalScale * scaleFactor > minScale) {
            canvasMatrix.postScale(scaleFactor, 1f, focusX, 1f)
            invalidate()
        }

    }

    override fun onLongPressMove(x: Float, y: Float) {

    }

    override fun onTouchScaleBegin(focusX: Float) {
        this.focusX = focusX
    }

    override fun onTap(x: Float, y: Float) {

    }

    override fun onTouchLeave() {

    }

    //********************************************************************接口********************************************************************//

    override fun getChartMatrix(): Matrix {
        return canvasMatrix
    }

    override fun getData(): List<Int> {
        return chartData
    }

    override fun getInitItemWidth(): Float {
        return width / pageSize.toFloat()
    }

    override fun getDisplayWidth(): Float {
        return this.width.toFloat()
    }
}

