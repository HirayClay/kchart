package com.bitmart.kchart.base

import android.animation.ValueAnimator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs

class TouchHelper(private val view: IBitMartChartView) : GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener, ValueAnimator.AnimatorUpdateListener {

    private val gestureDetector by lazy { GestureDetectorCompat(view.getContext(), this) }
    private val scaleGestureDetector by lazy { ScaleGestureDetector(view.getContext(), this) }

    private lateinit var listener: TouchHelperListener

    companion object {
        private const val VELOCITY_X_LIMIT = 1000
    }

    //是否长按
    private var isLongPressing = false

    //长按第一个位置Id
    private var longPressingPointerId = -1

    //是否可以横向惯性滑动
    private var horizontalFlingAble = false

    // 是否正在缩放
    private var isTouchScaling = false

    // 开始缩放后手指是否离开屏幕
    private var isTouchScalePointersLeave = true

    //处理Fling动画
    private var valueAnimator: ValueAnimator? = null

    //处理Fling滑动距离
    private var totalDistanceX = 0f

    fun onToucheEvent(event: MotionEvent, listener: TouchHelperListener): Boolean {
        this.listener = listener
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //是否需要判断点击区域主要是Padding影响
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLongPressing && event.getPointerId(event.actionIndex) == longPressingPointerId) {
                    listener.onLongPressMove(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isLongPressing = false
                isTouchScalePointersLeave = true
                listener.onTouchLeave()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (isLongPressing && event.getPointerId(event.actionIndex) == longPressingPointerId) {
                    isLongPressing = false
                }
            }
        }

        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        listener.onTap(e.x, e.y)
        return super.onSingleTapUp(e)
    }

    override fun onLongPress(e: MotionEvent) {
        isLongPressing = true
        longPressingPointerId = e.getPointerId(0)

        listener.onLongPressMove(e.x, e.y)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

        if (isTouchScaling) {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        //横向滑动距离比竖向滑动大
        if (abs(distanceX) > abs(distanceY)) {
            horizontalFlingAble = true
            listener.onHorizontalScroll(distanceX)
        } else {
            horizontalFlingAble = false
        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }


    override fun onScale(detector: ScaleGestureDetector): Boolean {
        listener.onTouchScaling(detector.scaleFactor)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (isTouchScaling) {
            return true
        }
        isTouchScaling = true
        isTouchScalePointersLeave = false
        listener.onTouchScaleBegin(detector.focusX)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        isTouchScaling = false
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

        if (abs(velocityX) < VELOCITY_X_LIMIT) {
            return super.onFling(e1, e2, velocityX, velocityY)
        }
        if (view.isReachLeftBorder() || view.isReachRightBorder()) {
            return super.onFling(e1, e2, velocityX, velocityY)
        }
        totalDistanceX = e1.x - e2.x
        if (horizontalFlingAble) {
            valueAnimator?.removeAllUpdateListeners()
            if (valueAnimator?.isRunning == true) {
                valueAnimator?.cancel()
            }
            valueAnimator = ValueAnimator.ofFloat(totalDistanceX, 0f).setDuration(abs(velocityX / 10).toLong())
            valueAnimator?.interpolator = ViscousFluidInterpolator()
            valueAnimator?.addUpdateListener(this)
            valueAnimator?.start()
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (view.isReachLeftBorder() || view.isReachRightBorder()) {
            animation.cancel()
        }
        val animatedValue = animation.animatedValue as Float
        val distanceX = totalDistanceX - animatedValue
        totalDistanceX = animatedValue
        listener.onHorizontalScroll(distanceX)
    }
}

interface TouchHelperListener {

    ///长按拖动
    fun onLongPressMove(x: Float, y: Float)

    ///水平滚动
    fun onHorizontalScroll(distanceX: Float)

    ///缩放
    fun onTouchScaling(scaleFactor: Float)

    ///开始缩放
    fun onTouchScaleBegin(focusX: Float)

    ///单击
    fun onTap(x: Float, y: Float)

    //手指离开屏幕
    fun onTouchLeave()

}