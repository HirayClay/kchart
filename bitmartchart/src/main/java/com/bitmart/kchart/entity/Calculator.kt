package com.bitmart.kchart.entity

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

internal object Calculator {
    suspend fun calc(entities: List<ChartDataEntity>) {
        if (entities.isEmpty()) return
        calcRise(entities)
        calcMa(entities, arrayOf(5, 10, 20))
        calcVolMa(entities, arrayOf(5, 10))
        calcEma(entities, arrayOf(5, 10, 20))
        calcBoll(entities, 20, 2)
        calcKdj(entities, 9, 3, 3)
        calcRsi(entities, arrayOf(6, 12, 24))
        calcMacd(entities, 12, 26, 9)
    }

    private fun calcRise(entities: List<ChartDataEntity>) {
        entities.forEachIndexed { index, entity ->
            if (entity.close == entity.open) {
                if (index == 0) {
                    entity.isRise = true
                } else {
                    val preKEntity = entities[index - 1]
                    entity.isRise = entity.close >= preKEntity.close
                }
            } else {
                entity.isRise = entity.close > entity.open
            }
        }
    }

    private fun calcMa(entities: List<ChartDataEntity>, days: Array<Int>) {

        val temp = Array(days.size) { 0.0 }

        entities.forEachIndexed { index, entity ->
            val closePrice = entity.close
            entity.ma = Array(days.size) { 0.0 }

            days.forEachIndexed { kIndex, day ->
                temp[kIndex] += closePrice
                when {
                    index == day - 1 -> {
                        entity.ma[kIndex] = temp[kIndex] / day
                    }
                    index >= day -> {
                        temp[kIndex] -= entities[index - day].close
                        entity.ma[kIndex] = temp[kIndex] / day
                    }
                    else -> {
                        entity.ma[kIndex] = null
                    }
                }
            }
        }
    }

    private fun calcVolMa(entities: List<ChartDataEntity>, days: Array<Int>) {

        val temp = Array(days.size) { 0.0 }

        entities.forEachIndexed { index, entity ->
            val vol = entity.vol
            entity.volMa = Array(days.size) { 0.0 }

            days.forEachIndexed { kIndex, day ->
                temp[kIndex] += vol
                when {
                    index == day - 1 -> {
                        entity.volMa[kIndex] = temp[kIndex] / day
                    }
                    index >= day -> {
                        temp[kIndex] -= entities[index - day].vol
                        entity.volMa[kIndex] = temp[kIndex] / day
                    }
                    else -> {
                        entity.volMa[kIndex] = null
                    }
                }
            }
        }
    }

    private fun getEmaFactor(day: Int): Array<Double> {
        return arrayOf(2.0 / (day + 1), (day - 1) * 1.0 / (day + 1))
    }

    private fun calcEma(entities: List<ChartDataEntity>, days: Array<Int>) {
        val temp = Array(days.size) { entities.firstOrNull()?.close ?: 0.0 }

        entities.forEach { entity ->
            val closePrice = entity.close
            entity.ema = Array(days.size) { 0.0 }

            days.forEachIndexed { kIndex, day ->
                val emaFactor = getEmaFactor(day)
                val ema = emaFactor[0] * closePrice + emaFactor[1] * temp[kIndex]
                entity.ema[kIndex] = ema
                temp[kIndex] = ema
            }
        }
    }

    private fun calcBoll(entities: List<ChartDataEntity>, n: Int, k: Int) {

        // 1. MB 2.UP 3.DN
        val mbIdx = 0
        val upIdx = 1
        val dnIdx = 2

        var pFrom = 0
        var pEnd: Int
        var sum = 0.0
        entities.forEachIndexed { index, entity ->
            if (index == 0) {
                pFrom = index
            }
            pEnd = index
            sum += entity.close
            if (pEnd - pFrom + 1 == n) {
                val ma = sum / n
                sum -= entities[pFrom].close
                pFrom += 1
                var squareSum = 0.0
                for (i in pEnd downTo pEnd - n + 1) {
                    squareSum += (entities[i].close - ma).pow(2)
                }
                val std = sqrt(squareSum / n)
                entity.boll[mbIdx] = ma
                entity.boll[upIdx] = ma + k * std
                entity.boll[dnIdx] = ma - k * std
            } else {
                entity.boll[mbIdx] = null
                entity.boll[upIdx] = null
                entity.boll[dnIdx] = null
            }
        }
    }

    private fun calcKdj(entities: List<ChartDataEntity>, n: Int, kn: Int, dn: Int) {
        var preK = 50.0
        var preD = 50.0
        val tmp = entities.first()
        tmp.kdj[0] = preK
        tmp.kdj[1] = preD
        tmp.kdj[2] = 50.0

        entities.forEachIndexed { index, entity ->

            var low = entity.low
            var high = entity.high

            for (j in max(0, index - n - 1) until index) {
                if (entities[j].low < low) {
                    low = entities[j].low
                }
                if (entities[j].high > high) {
                    high = entities[j].high
                }
            }
            val closePrice = entity.close

            val rsv = if (high == low) 0.0 else (closePrice - low) * 100.0 / (high - low)

            val k = (2 * preK + rsv) / kn
            val d = (2 * preD + k) / dn
            val j = 3 * k - 2 * d
            preK = k
            preD = d

            entity.kdj[0] = k
            entity.kdj[1] = d
            entity.kdj[2] = j
        }
    }

    private fun calcRsi(entities: List<ChartDataEntity>, days: Array<Int>) {
        val rsiAbsEma = Array(days.size) { 0.0 }
        val rsiMaxEma = Array(days.size) { 0.0 }
        val rsi = Array<Double?>(days.size) { null }
        entities.forEachIndexed { index, entity ->
            val closePrice = entity.close
            days.forEachIndexed { kIndex, day ->
                if (index == 0) {
                    rsi[kIndex] = 0.0
                    rsiAbsEma[kIndex] = 0.0
                    rsiMaxEma[kIndex] = 0.0
                } else {
                    val priceMax = max(0.0, closePrice - entities[index - 1].close)
                    val priceAbs = abs(closePrice - entities[index - 1].close)
                    rsiMaxEma[kIndex] = (priceMax + (day - 1) * rsiMaxEma[kIndex]) / day
                    rsiAbsEma[kIndex] = (priceAbs + (day - 1) * rsiAbsEma[kIndex]) / day
                    rsi[kIndex] = (rsiMaxEma[kIndex] / rsiAbsEma[kIndex]) * 100
                }
                if (index < day) {
                    rsi[kIndex] = null
                }
                if (rsi[kIndex] != null && rsi[kIndex]?.isInfinite() == true) {
                    rsi[kIndex] = null
                }

                entity.rsi[kIndex] = rsi[kIndex]
            }

        }
    }

    private fun calcMacd(entities: List<ChartDataEntity>, short: Int, long: Int, avg: Int) {
        val macdIdx = 0
        val difIdx = 1
        val deaIdx = 2
        var preEmaShort = 0.0
        var preEmaLong = 0.0
        entities.forEachIndexed { index, entity ->
            if (index == 0) {
                entity.macd[difIdx] = 0.0
                entity.macd[deaIdx] = 0.0
                entity.macd[macdIdx] = 0.0
                preEmaShort = entity.close
                preEmaLong = entity.close
            } else {
                val emaShort = 2f / (short + 1) * entity.close + (short - 1f) / (short + 1f) * preEmaShort
                val emaLong = 2f / (long + 1) * entity.close + (long - 1f) / (long + 1f) * preEmaLong

                val dif = emaShort - emaLong
                val dea = 2f / (avg + 1) * dif + (avg - 1f) / (avg + 1f) * entities[index - 1].macd[deaIdx]!!
                val macd = (dif - dea) * 2

                entity.macd[difIdx] = dif
                entity.macd[deaIdx] = dea
                entity.macd[macdIdx] = macd

                preEmaShort = emaShort
                preEmaLong = emaLong
            }
        }
    }
}