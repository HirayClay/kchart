package com.bitmart.data.domain.model

data class KLineModel(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val vol: Double,
    val amount: Double,
)