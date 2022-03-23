package com.bitmart.kchart.entity

class ChartDataEntity {
    //高
    var high: Double = 0.0

    //低
    var low: Double = 0.0

    //开
    var open: Double = 0.0

    //收
    var close: Double = 0.0

    //成交量
    var vol: Double = 0.0

    //成交额
    var amount: Double = 0.0

    //时间（毫秒）
    var time: Long = 0L

    //涨跌副
    val change: Double
        get() {
            return close - open
        }

    //涨跌比率
    val ratio: Double
        get() {
            return (change / open) * 100
        }

    //是否是上升
    internal var isRise: Boolean = true

    //MA线
    internal var ma: Array<Double?> = Array(3) { null }

    //VolMA线
    internal var volMa: Array<Double?> = Array(2) { null }

    //EMA线
    internal var ema: Array<Double?> = Array(3) { null }

    //boll线
    internal var boll: Array<Double?> = Array(3) { null }


    //MACD指标
    internal var macd: Array<Double?> = Array(3) { null }

    //KDJ指标
    internal var kdj: Array<Double?> = Array(3) { null }

    //RSI指标
    internal var rsi: Array<Double?> = Array(3) { null }


    fun copy(): ChartDataEntity {
        return ChartDataEntity().apply {
            high = this@ChartDataEntity.high
            low = this@ChartDataEntity.low
            open = this@ChartDataEntity.open
            close = this@ChartDataEntity.close
            vol = this@ChartDataEntity.vol
            amount = this@ChartDataEntity.amount
            time = this@ChartDataEntity.time
            isRise = this@ChartDataEntity.isRise
        }
    }

    override fun toString(): String {
        return "{\n" +
                "  high: $high,\n" +
                "  low: $low,\n" +
                "  open: $open,\n" +
                "  close: $close,\n" +
                "  vol: $vol,\n" +
                "  amount: $amount,\n" +
                "  time: $time,\n" +
                "  change: $change,\n" +
                "  ratio: $ratio,\n" +
                "  ma: ${arrayToString(ma)},\n" +
                "  volMa: ${arrayToString(volMa)},\n" +
                "  ema: ${arrayToString(ema)},\n" +
                "  boll: ${arrayToString(boll)},\n" +
                "  kdj: ${arrayToString(kdj)},\n" +
                "  rsi: ${arrayToString(rsi)},\n" +
                "  macd: ${arrayToString(macd)}\n" +
                "}"
    }

    private fun arrayToString(array: Array<*>): String {
        return "[${array.joinToString { it.toString() }}]"
    }
}