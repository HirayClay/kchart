package com.bitmart.demo.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bitmart.demo.R
import com.bitmart.demo.util.ChartConfigCacheManager
import com.bitmart.demo.viewmodel.MainActivityViewModel
import com.bitmart.demo.viewmodel.MainActivityViewState
import com.bitmart.kchart.BitMartChartView
import com.bitmart.kchart.controller.BitMartChartViewController
import com.bitmart.kchart.controller.ChartChangeListener
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.entity.ChartExtraInfoEntity
import com.bitmart.kchart.entity.PositionInfo
import com.bitmart.kchart.properties.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val mainActivityViewModel by viewModel<MainActivityViewModel>()

    private val tmv by lazy { findViewById<BitMartChartView>(R.id.tmv_test) }

    private val controller by lazy { BitMartChartViewController() }

    private var tempCacheList = listOf<ChartDataEntity>()

    private val cacheManager = ChartConfigCacheManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initEvent()
    }

    private fun initEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.uiStateFlow.collect { viewState ->
                    println(viewState)
                    when (viewState) {
                        is MainActivityViewState.Loading -> {

                        }

                        is MainActivityViewState.Success -> {


                            val list = viewState.lineList.map {
                                val chartData = ChartDataEntity()
                                chartData.high = it.high
                                chartData.low = it.low
                                chartData.open = it.open
                                chartData.close = it.close
                                chartData.vol = it.vol
                                chartData.amount = it.amount
                                chartData.time = it.time
                                return@map chartData
                            }

                            tempCacheList = list

                            controller.setChartData(list)
                        }


                        is MainActivityViewState.Error -> {

                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            cacheManager.storageData()
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            tmv.setController(controller)
            tmv.setProperties(cacheManager.setUp())
        }

        controller.setLoadMoreListener {
            println("onLoadingMore")
            //mainActivityViewModel.loadArticlePageList(1)
        }

        controller.setChartChangeListener(object : ChartChangeListener {

            override fun onPageShowNumChange(pageShowNum: Int) {
                cacheManager.cachePageShowNum(pageShowNum)
            }

            override fun onChartPropertiesChange(properties: BitMartChartProperties) {
                cacheManager.cacheProperties(properties)
            }
        })

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            mainActivityViewModel.loadArticlePageList(1)
        }

        findViewById<Button>(R.id.btn_add_one).setOnClickListener {
            lifecycleScope.launch {
                controller.addNewData(controller.getChartData().last())
            }
        }

        findViewById<Button>(R.id.btn_change_style).setOnClickListener {
            tmv.setProperties(cacheManager.properties.apply {
                pageShowNum = 10
                kLineRendererProperties = KLineRendererProperties(
                    showType = KLineShowType.CANDLE_WITH_SAR,
                    showMaxPrice = false,
                    showNowPrice = false,
                    showExtraInfo = true
                )
                volRendererProperties = VolRendererProperties()
                kdjRendererProperties = KdjRendererProperties()
                rsiRendererProperties = RsiRendererProperties()
                macdRendererProperties = MacdRendererProperties()
            })


            val updateRunnable = PositionUpdateRunnable()
            it.postDelayed(updateRunnable, 1000)
        }

        findViewById<Button>(R.id.btn_update_newer).setOnClickListener {
            lifecycleScope.launch {
                val entity = controller.getChartData().last().copy()
                entity.close = entity.close - 5
                entity.open = entity.open - 5
                entity.high = entity.high - 5
                entity.low = entity.low - 5
                controller.updateNewerData(entity)
            }
        }

    }

    inner class PositionUpdateRunnable : Runnable {
        override fun run() {
            controller.setChartExtraInfo(ChartExtraInfoEntity(positions = arrayListOf(PositionInfo(Random.nextFloat().toString(), Random.nextFloat().toString(), 20000.0, 0.002))))
            this@MainActivity.findViewById<Button>(R.id.btn_change_style).postDelayed(this, 1000)
        }

    }
}