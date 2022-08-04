package com.bitmart.demo.viewmodel

import com.bitmart.data.domain.model.KLineModel
import com.bitmart.data.domain.usecase.GetKLineListUseCase
import com.bitmart.demo.mock.MockLine
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivityViewModel(private val getKLineListUseCase: GetKLineListUseCase) : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        uiStateFlow.value = MainActivityViewState.Error(exception.toString())
    }

    private val _uiStateFLow = MutableStateFlow<MainActivityViewState>(MainActivityViewState.Loading(true))

    val uiStateFlow: MutableStateFlow<MainActivityViewState> = _uiStateFLow

    fun loadArticlePageList(pageNo: Int) {
        launchCoroutine {
            val lineList = getKLineListUseCase("BTCUSD_PERP", "1m", limit = 500)
//            val lineList = MockLine.getMockKlineModel()
            uiStateFlow.value = MainActivityViewState.Success(lineList)
        }
    }
}

sealed class MainActivityViewState {
    data class Loading(
        val loading: Boolean,
    ) : MainActivityViewState()

    data class Success(
        val lineList: List<KLineModel>
    ) : MainActivityViewState()

    data class Error(
        val message: String
    ) : MainActivityViewState()
}