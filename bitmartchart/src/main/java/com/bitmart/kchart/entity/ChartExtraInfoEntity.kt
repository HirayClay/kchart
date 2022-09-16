package com.bitmart.kchart.entity

data class ChartExtraInfoEntity(
    var positions: List<PositionInfo>? = null
)

data class PositionInfo(
    //未实现盈亏
    val pnl: String,
    //持仓数量
    val holding: String,
    //价格
    val price: Double,
    //收益率 0.001 = 0.1%
    val earningRate: Double,
    //方向
    val way: Way
) {
    enum class Way {
        LONG,
        SHORT,
    }
}