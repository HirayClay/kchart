package com.bitmart.kchart.base

import android.graphics.Matrix
import com.bitmart.kchart.util.dp2px
import kotlin.math.abs
import kotlin.math.roundToInt

class AreaCalcHelper(private val view: IBitMartChartView, private val canvasMatrix: Matrix) {

    companion object {
        private const val FIXED_COUNT = 1
    }

    private var loadingMore = false

    private var loadMoreListener: LoadMoreListener? = null

    private var focusX = 0f

    private val temp9Array by lazy { FloatArray(9) }

    fun setListener(loadMoreListener: LoadMoreListener) {
        this.loadMoreListener = loadMoreListener
    }

    fun setLoadingMoreFinish() {
        this.loadingMore = false
    }

    fun inScreenArea(index: Int): Boolean {
        val range = getDataInScreenRange()
        return !(index > range.second + FIXED_COUNT || index < range.first - FIXED_COUNT)
    }

    fun getDataInScreenRange(): Pair<Int, Int> {
        if (view.getChartData().isEmpty()) return Pair(0, 0)
        val totalScale = getTotalScale()

        //总共要显示的条数
        val totalCount = view.getChartData().size
        //当前屏幕可显示的条数
        val displayCount = (view.getGlobalProperties().showPageNum / totalScale).roundToInt()
        //划过的条数
        val transCount = (getTotalTranslate() / (view.getGlobalProperties().eachWidth * totalScale)).toInt()

        if (totalCount <= displayCount) {

            when {
                transCount == 0 -> {
                    return Pair(0, totalCount - 1)
                }
                transCount > 0 -> {
                    return when {
                        totalCount <= transCount -> {
                            Pair(-1, -1)
                        }
                        else -> {
                            Pair(0, totalCount - 1)
                        }
                    }
                }
                transCount < 0 -> {
                    return when {
                        totalCount <= abs(transCount) -> {
                            Pair(-1, -1)
                        }
                        else -> {
                            Pair(abs(transCount), totalCount - 1)
                        }
                    }
                }
            }
        } else {
            when {
                transCount == 0 -> {
                    return Pair(0, displayCount - 1)
                }
                transCount > 0 -> {
                    return when {
                        totalCount <= transCount -> {
                            Pair(-1, -1)
                        }
                        else -> {
                            Pair(0, displayCount - transCount - 1)
                        }
                    }
                }
                transCount < 0 -> {
                    return when {
                        totalCount <= abs(transCount) -> {
                            Pair(-1, -1)
                        }
                        else -> {
                            if (displayCount + abs(transCount) >= totalCount) {
                                Pair(abs(transCount), totalCount - 1)
                            } else {
                                Pair(abs(transCount), abs(transCount) + displayCount)
                            }
                        }
                    }
                }
            }
        }
        return Pair(-1, -1)
    }

    fun getDataIndexByScreenPointX(x: Float): Int? {
        val totalScale = getTotalScale()
        val totalTranslate = getTotalTranslate()
        //滑动和缩放逻辑已经限制数据不会滑动到屏幕之外
        if (totalTranslate > 0) return null
        if (totalTranslate + getMaxTranslateWidth(totalScale) < 0) return null
        return ((abs(totalTranslate) + x) / (view.getGlobalProperties().eachWidth * totalScale)).toInt()
    }

    fun getDataScreenPointXbyIndex(index: Int): Float? {
        val totalScale = getTotalScale()
        val totalTranslate = getTotalTranslate()
        //滑动和缩放逻辑已经限制数据不会滑动到屏幕之外
        if (totalTranslate > 0) return null
        if (totalTranslate + getMaxTranslateWidth(totalScale) < 0) return null
        return (index * view.getGlobalProperties().eachWidth * totalScale) - abs(totalTranslate)
    }

    fun getTotalTranslate(): Float {
        canvasMatrix.getValues(temp9Array)
        return temp9Array[Matrix.MTRANS_X]
    }

    fun getTotalScale(): Float {
        canvasMatrix.getValues(temp9Array)
        return temp9Array[Matrix.MSCALE_X]
    }

    fun isReachLeftBorder(): Boolean {
        return getTotalTranslate() >= 0
    }

    fun isReachRightBorder(): Boolean {
        return getTotalTranslate() + getMaxTranslateWidth(getTotalScale()) <= 0
    }

    private val tempMatrix by lazy { Matrix() }
    private val tempMatrixArray by lazy { FloatArray(9) }

    fun onTouchScaling(scaleFactor: Float) {
        val totalScale = getTotalScale()

        if (getMaxTranslateWidth(totalScale) <= 0.0f) {
            return
        }

        val currentTotalScale = totalScale * scaleFactor

        if (currentTotalScale < view.getGlobalProperties().maxScaleRatio && currentTotalScale > view.getGlobalProperties().minScaleRatio) {

            tempMatrix.reset()
            tempMatrix.postScale(totalScale, 1f)
            tempMatrix.postScale(scaleFactor, 1f, focusX, 0f)
            tempMatrix.postTranslate(getTotalTranslate(), 0f)
            tempMatrix.getValues(tempMatrixArray)
            val tempTotalScale = tempMatrixArray[Matrix.MSCALE_X]
            val tempTotalTranslate = tempMatrixArray[Matrix.MTRANS_X]
            val tempMaxTranslateWidth = getMaxTranslateWidth(tempTotalScale)

            //这次缩放到左边界了缩放中心就移动到最左边
            if (tempTotalTranslate >= 0) {
                canvasMatrix.postScale(scaleFactor, 1f, view.getTouchArea().left.toFloat(), view.getTouchArea().height().toFloat() / 2 + view.getTouchArea().top)
                view.invalidate()
                return
            }

            //这次缩放到右边界了缩放中心移动到最右边
            if (tempTotalTranslate < 0 && tempTotalTranslate + tempMaxTranslateWidth <= 0) {
                canvasMatrix.postScale(scaleFactor, 1f, view.getTouchArea().right.toFloat(), view.getTouchArea().height().toFloat() / 2 + view.getTouchArea().top)
                view.invalidate()
                return
            }

            canvasMatrix.postScale(scaleFactor, 1f, focusX, 0f)
            view.invalidate()
        }
    }

    fun onTouchScaleBegin(focusX: Float) {
        this.focusX = focusX
    }

    fun getMaxTranslateWidth(scale: Float): Float {
        return view.getChartData().size * scale * view.getGlobalProperties().eachWidth - view.getGlobalProperties().showPageNum * view.getGlobalProperties().eachWidth + view.getGlobalProperties().rightAxisWidth.dp2px(view.getContext())
    }

    fun getDataWidth(dataSize: Int): Float {
        return dataSize * getTotalScale() * view.getGlobalProperties().eachWidth
    }

    fun onHorizontalScroll(distanceX: Float) {

        val totalScale = getTotalScale()

        if (getMaxTranslateWidth(totalScale) <= 0.0f) {
            return
        }

        tempMatrix.reset()
        tempMatrix.postScale(totalScale, 1f)
        tempMatrix.postTranslate(getTotalTranslate(), 0f)
        tempMatrix.postTranslate(-distanceX, 0f)
        tempMatrix.getValues(tempMatrixArray)
        val tempTotalTranslate = tempMatrixArray[Matrix.MTRANS_X]
        val tempMaxTranslateWidth = getMaxTranslateWidth(totalScale)

        if (tempTotalTranslate >= 0) {
            canvasMatrix.postTranslate(-distanceX, 0f)
            canvasMatrix.postTranslate(-tempTotalTranslate, 0f)
            view.invalidate()

            if (!loadingMore) {
                loadingMore = true
                this.loadMoreListener?.onLoadMore()
            }
            return
        }

        if (abs(tempTotalTranslate) > tempMaxTranslateWidth) {
            canvasMatrix.postTranslate(-distanceX, 0f)
            canvasMatrix.postTranslate(-(tempMaxTranslateWidth + tempTotalTranslate), 0f)
            view.invalidate()
            return
        }

        canvasMatrix.postTranslate(-distanceX, 0f)
        view.invalidate()
        return
    }

    fun setTranslate(distanceX: Float) {
        canvasMatrix.setScale(getTotalScale(), 1f)
        canvasMatrix.postTranslate(-distanceX, 0f)
        view.invalidate()
    }

    fun postTranslate(distanceX: Float) {
        canvasMatrix.postTranslate(-distanceX, 0f)
        view.invalidate()
    }
}

interface LoadMoreListener {
    fun onLoadMore()
}