package com.bitmart.demo.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bitmart.kchart.BitMartChartView
import com.bitmart.kchart.controller.BitMartChartViewController
import com.bitmart.kchart.entity.ChartDataEntity
import com.bitmart.kchart.properties.*
import com.bitmart.demo.R
import com.bitmart.demo.viewmodel.MainActivityViewModel
import com.bitmart.demo.viewmodel.MainActivityViewState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainActivityViewModel by viewModel<MainActivityViewModel>()

    private val tmv by lazy { findViewById<BitMartChartView>(R.id.tmv_test) }

    private val controller by lazy { BitMartChartViewController() }

    private val kLineRendererProperties by lazy { KLineRendererProperties() }
    private val volRendererProperties by lazy { VolRendererProperties() }
    private val macdRendererProperties by lazy { MacdRendererProperties() }
    private val kdjRendererProperties by lazy { KdjRendererProperties() }
    private val rsiRendererProperties by lazy { RsiRendererProperties() }

    private val bitMartChartProperties by lazy {
        BitMartChartProperties(
            chartRendererProperties = mutableListOf(
                kLineRendererProperties,
                volRendererProperties,
                macdRendererProperties,
                kdjRendererProperties,
                rsiRendererProperties,
            ),
        )
    }

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

                            controller.setChartData(list)
                        }

                        is MainActivityViewState.Error -> {

                        }
                    }
                }
            }
        }
    }

    private fun initData() {
        controller.setLoadMoreListener {
            println("onLoadingMore")
            //mainActivityViewModel.loadArticlePageList(1)
        }
        tmv.setController(controller)
        tmv.setProperties(bitMartChartProperties)
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            mainActivityViewModel.loadArticlePageList(1)
        }

        findViewById<Button>(R.id.btn_add_one).setOnClickListener {
            lifecycleScope.launch {
                controller.addNewData(controller.getChartData().last())
            }
        }

        findViewById<Button>(R.id.btn_change_style).setOnClickListener {
            tmv.setProperties(
                bitMartChartProperties.apply {
                    chartRendererProperties.apply {
                        this[0] = kLineRendererProperties.apply {
                            showType = KLineShowType.CANDLE_WITH_BOLL
                            dataFormat = "dd HH:mm"
                            showAxisYNum = 6
                            showAxisXNum = 6
                        }
                    }
                }
            )
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
}