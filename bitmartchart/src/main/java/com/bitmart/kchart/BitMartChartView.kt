package com.bitmart.kchart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bitmart.kchart.base.*
import com.bitmart.kchart.child.base.IRenderer
import com.bitmart.kchart.child.renderer.*
import com.bitmart.kchart.controller.BitMartChartViewController
import com.bitmart.kchart.controller.BitMartChildViewBridge
import com.bitmart.kchart.default.DEFAULT_BIT_MART_CHART_PROPERTIES
import com.bitmart.kchart.default.DEFAULT_CHART_RATIO_HEIGHT
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.*
import com.bitmart.kchart.util.dp2px
import com.bitmart.kchart.util.getBackgroundColor
import com.bitmart.kchart.util.isDarkMode
import com.bitmart.kchart.util.sp2px
import kotlin.math.abs
import kotlin.math.roundToInt

class BitMartChartView : View, TouchHelperListener, IBitMartChartView, BitMartChildViewBridge, LoadMoreListener {

    private val canvasMatrix by lazy { Matrix() }

    private lateinit var properties: GlobalProperties

    private val longPressPoint by lazy { PointF(0f, 0f) }

    private val toucheHelper by lazy { TouchHelper(this) }

    private val areaCalcHelper by lazy { AreaCalcHelper(this, canvasMatrix) }

    private val childRenders = mutableListOf<IRenderer<IRendererProperties>>()

    private var controller: BitMartChartViewController = BitMartChartViewController()

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    //********************************************************************初始********************************************************************//

    private fun initView() {
        setWillNotDraw(false)
        controller.setListener(this)
        areaCalcHelper.setListener(this)
        covertProperties(DEFAULT_BIT_MART_CHART_PROPERTIES)
    }

    private fun calcEachHeight(viewHeight: Int, viewWidth: Int) {
        var top = 0f + paddingTop
        val bottom = viewHeight - paddingBottom.toFloat()
        val left = paddingLeft.toFloat()
        val right = viewWidth.toFloat() - paddingRight - left
        val drawAreaHeight = (bottom - top)
        val drawAreaWidth = right - left

        val eachWidth = drawAreaWidth / this.properties.showPageNum.toFloat()
        val itemWidth = eachWidth / (this.properties.barSpaceRatio + 1)
        val spaceWidth = itemWidth * this.properties.barSpaceRatio

        this.properties.eachWidth = eachWidth
        this.properties.itemWidth = itemWidth
        this.properties.spaceWidth = spaceWidth
        this.properties.isDarkMode = context.isDarkMode()
        this.properties.backgroundColor = context.getBackgroundColor()

        val sumRatio = this.childRenders.map { it.properties.heightRatio }.sum()
        val perHeight = drawAreaHeight / sumRatio

        //计算每一份需要绘制的大小
        this.childRenders.forEach { renderer ->
            val rendererHeight = perHeight * renderer.properties.heightRatio
            renderer.rendererRect.set(left, top, right, rendererHeight + top)
            renderer.dataRect.set(left, top, right, rendererHeight + top)
            top += rendererHeight
        }
    }

    private fun covertProperties(bitMartChartProperties: BitMartChartProperties) {
        this.properties = GlobalProperties.fromProperties(bitMartChartProperties)
        val calcProperties = mutableListOf<IRendererProperties>()
        bitMartChartProperties.chartRendererProperties.filter { it !is DividerRendererProperties }.forEachIndexed { index, properties ->
            if (index == 0) {
                calcProperties.add(properties)
            } else {
                calcProperties.add(DividerRendererProperties())
                calcProperties.add(properties)
            }
        }

        val renderers = calcProperties.mapNotNull {
            when (it) {
                is KLineRendererProperties -> KLineRenderer(it, this)
                is VolRendererProperties -> VolRenderer(it, this)
                is MacdRendererProperties -> MacdRenderer(it, this)
                is KdjRendererProperties -> KdjRenderer(it, this)
                is RsiRendererProperties -> RsiRenderer(it, this)
                is DividerRendererProperties -> DividerRenderer(this)
                else -> null
            }
        }

        this.childRenders.clear()
        this.childRenders.addAll(renderers)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = 0
        val totalRatio = this.childRenders.map { it.properties.heightRatio }.sum()
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                height = (DEFAULT_CHART_RATIO_HEIGHT * totalRatio).dp2px(this.context).roundToInt()
            }
            MeasureSpec.EXACTLY -> {
                height = MeasureSpec.getSize(heightMeasureSpec)
            }
        }
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        calcEachHeight(height, width)
        setMeasuredDimension(width, height)
    }

    //********************************************************************绘制********************************************************************//

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!this::properties.isInitialized) {
            return
        }

        if (getChartData().isEmpty()) {
            if (this.properties.drawEmptyView) {
                drawEmptyView(canvas)
            }
            return
        }

        val chartData = getChartData()
        val spaceWidth = getGlobalProperties().spaceWidth
        val startX = spaceWidth / 2
        val itemWidth = getGlobalProperties().itemWidth

        canvas.save()
        canvas.setMatrix(canvasMatrix)
        childRenders.forEach {
            it.draw(canvas, chartData, startX, spaceWidth, itemWidth)
        }
        canvas.restore()

        childRenders.forEach {
            it.noCovertDraw(canvas, chartData, startX, spaceWidth, itemWidth)
        }
    }

    private val emptyTextPaint by lazy {
        Paint().apply {
            color = getGlobalProperties().textColor()
            textSize = 20.sp2px(this@BitMartChartView.context)
            textAlign = Paint.Align.CENTER
        }
    }

    private fun drawEmptyView(canvas: Canvas) {
        val message = "NO DATA"
        canvas.drawText(message, (width) / 2f, height / 2f, emptyTextPaint)
    }

    //********************************************************************事件********************************************************************//

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return toucheHelper.onToucheEvent(event, this)
    }

    override fun onHorizontalScroll(distanceX: Float) {
        if (getChartData().isEmpty()) return
        parent.requestDisallowInterceptTouchEvent(true)
        areaCalcHelper.onHorizontalScroll(distanceX)
    }

    override fun onTouchScaling(scaleFactor: Float) {
        if (getChartData().isEmpty()) return
        parent.requestDisallowInterceptTouchEvent(true)
        areaCalcHelper.onTouchScaling(scaleFactor)
    }

    override fun onLongPressMove(x: Float, y: Float) {
        if (getChartData().isEmpty()) return
        longPressPoint.set(x, y)
        invalidate()
        parent.requestDisallowInterceptTouchEvent(true)
    }

    override fun onTouchScaleBegin(focusX: Float) {
        if (getChartData().isEmpty()) return
        areaCalcHelper.onTouchScaleBegin(focusX)
        parent.requestDisallowInterceptTouchEvent(true)
    }


    override fun onTap(x: Float, y: Float) {
        if (getChartData().isEmpty()) return
    }

    override fun onTouchLeave() {
        longPressPoint.set(0f, 0f)
        invalidate()
        parent.requestDisallowInterceptTouchEvent(false)
    }

    override fun getTouchArea(): Rect {
        return Rect(left + paddingLeft, top + paddingTop, right - paddingRight, bottom - paddingBottom)
    }

    override fun getChartData(): List<ChartDataEntity> {
        return controller.getChartData()
    }

    override fun getTotalScale(): Float {
        return areaCalcHelper.getTotalScale()
    }

    override fun getTotalTranslate(): Float {
        return areaCalcHelper.getTotalTranslate()
    }

    override fun isReachLeftBorder(): Boolean {
        return areaCalcHelper.isReachLeftBorder()
    }

    override fun isReachRightBorder(): Boolean {
        return areaCalcHelper.isReachRightBorder()
    }

    override fun getGlobalProperties(): GlobalProperties {
        return properties
    }

    //********************************************************************接口********************************************************************//

    override fun invalidate() {
        super.invalidate()
    }

    override fun isDataInScreen(index: Int): Boolean {
        return areaCalcHelper.inScreenArea(index)
    }

    override fun getDataInScreenRange(): Pair<Int, Int> {
        return areaCalcHelper.getDataInScreenRange()
    }

    override fun getHighlightingPoint(): PointF? {
        if (longPressPoint.x == 0f && longPressPoint.y == 0f) return null
        return longPressPoint
    }

    override fun getDataIndexByScreenPointX(x: Float): Int? {
        return areaCalcHelper.getDataIndexByScreenPointX(x)
    }

    override fun getDataScreenPointXbyIndex(index: Int): Float? {
        return areaCalcHelper.getDataScreenPointXbyIndex(index)
    }

    override fun onDataSetChanged() {
        this.post {
            this.childRenders.forEach { renderer ->
                renderer.dataRect.set(0f, renderer.rendererRect.top, abs(areaCalcHelper.getDataWidth(getChartData().size)).roundToInt().toFloat(), renderer.rendererRect.bottom)
            }
            val distanceX = areaCalcHelper.getMaxTranslateWidth(getTotalScale())
            if (distanceX <= 0) {
                areaCalcHelper.setTranslate(0f)
            } else {
                areaCalcHelper.setTranslate(-distanceX)
            }
        }
    }

    override fun onDataSetAdd(newDataSize: Int) {
        this.post {
            this.childRenders.forEach { renderer ->
                renderer.dataRect.set(0f, renderer.rendererRect.top, abs(areaCalcHelper.getDataWidth(getChartData().size)).roundToInt().toFloat(), renderer.rendererRect.bottom)
            }
            val distanceX = areaCalcHelper.getMaxTranslateWidth(getTotalScale())
            if (distanceX <= 0) {
                areaCalcHelper.setTranslate(0f)
            } else {
                val oldDataSize = getChartData().size - newDataSize
                val x = oldDataSize * getTotalScale() * getGlobalProperties().eachWidth - getGlobalProperties().showPageNum * getGlobalProperties().eachWidth + getGlobalProperties().rightAxisWidth.dp2px(context)
                if (x <= 0) {
                    areaCalcHelper.setTranslate(-distanceX)
                } else {
                    val newDataWidth = areaCalcHelper.getDataWidth(newDataSize)
                    areaCalcHelper.setTranslate(-newDataWidth + getTotalTranslate())
                }
            }
        }
    }

    override fun finishLoadMore(noMore: Boolean) {
        areaCalcHelper.setLoadingMoreFinish(noMore)
    }

    override fun getCurrentPageSize(): Int {
        return areaCalcHelper.getCurrentPageSize()
    }

    fun setController(controller: BitMartChartViewController) {
        this.controller = controller
        this.controller.setListener(this)
    }

    fun setProperties(bitMartChartProperties: BitMartChartProperties) {
        covertProperties(bitMartChartProperties)
        calcEachHeight(height, width)
        requestLayout()
    }

    override fun onLoadMore() {
        controller.loadMoreListener?.invoke()
    }
}