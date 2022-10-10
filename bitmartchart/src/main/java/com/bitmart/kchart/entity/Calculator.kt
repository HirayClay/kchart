package com.bitmart.kchart.entity

import kotlin.math.*

internal object Calculator {

    fun calc(entities: List<ChartDataEntity>): MutableList<ChartDataEntity> {
        val local = entities.toMutableList()
        if (local.isEmpty()) return mutableListOf()
        calcRise(local)
        calcMa(local, arrayOf(5, 10, 20))
        calcVolMa(local, arrayOf(5, 10))
        calcEma(local, arrayOf(5, 10, 20))
        calcBoll(local, 20, 2)
        calcKdj(local, 9, 3, 3)
        calcRsi(local, arrayOf(6, 12, 24))
        calcMacd(local, 12, 26, 9)
        calcSar(local)
        return local
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

    /**
     * 计算sar
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
    private fun calcSar(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
        //加速因子
        var af = 0.0
        //极值
        var ep = -100.0
        //判断是上涨还是下跌  false：下跌
        var isIncreasing = false
        var sar = 0.0

        return calc(dataList) { i ->
            //上一个周期的sar
            val preSar = sar
            val highestPrice = dataList[i].high
            val lowestPrice = dataList[i].low
            if (isIncreasing) {
                //上涨
                if (ep == -100.0 || ep < highestPrice) {
                    //重新初始化值
                    ep = highestPrice
                    af = min(af + 0.02, 2.0)
                }
                sar = preSar + af * (ep - preSar)
                val lowestPriceMin = min(dataList[max(1, i) - 1].low, lowestPrice)
                if (sar > dataList[i].low) {
                    sar = ep
                    //重新初始化值
                    af = 0.0
                    ep = -100.0
                    isIncreasing = !isIncreasing

                } else if (sar > lowestPriceMin) {
                    sar = lowestPriceMin
                }
            } else {
                if (ep == -100.0 || ep > lowestPrice) {
                    //重新初始化值
                    ep = lowestPrice
                    af = min(af + 0.02, 0.2)
                }
                sar = preSar + af * (ep - preSar)
                val highestPriceMax = max(dataList[max(1, i) - 1].low, highestPrice)
                if (sar < dataList[i].high) {
                    sar = ep
                    //重新初始化值
                    af = 0.0
                    ep = -100.0
                    isIncreasing = !isIncreasing

                } else if (sar < highestPriceMax) {
                    sar = highestPriceMax
                }
            }
            dataList[i].sar = SarEntity(sar, isIncreasing)
        }
    }

    /**
     * 计算bias
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcBias(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        // 乖离率=[(当日收盘价-N日平均价)/N日平均价]*100%
//        // 参数：6，12、24
//        var bias1: Double
//        var bias2: Double
//        var bias3: Double
//
//        var mean1: Double
//        var mean2: Double
//        var mean3: Double
//
//        var closes1 = 0.0
//        var closes2 = 0.0
//        var closes3 = 0.0
//
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//            closes1 += close
//            closes2 += close
//            closes3 += close
//
//            if (i < 6) {
//                mean1 = closes1 / (i + 1)
//            } else {
//                closes1 -= dataList[i - 6].close
//                mean1 = closes1 / 6
//            }
//            bias1 = (close - mean1) / mean1 * 100
//
//            if (i < 12) {
//                mean2 = closes2 / (i + 1)
//            } else {
//                closes2 -= dataList[i - 12].close
//                mean2 = closes2 / 12
//            }
//            bias2 = (close - mean2) / mean2 * 100
//
//            if (i < 24) {
//                mean3 = closes3 / (i + 1)
//            } else {
//                closes3 -= dataList[i - 24].close
//                mean3 = closes3 / 24
//            }
//            bias3 = ((close - mean3) / mean3) * 100
//            dataList[i].bias = BiasModel(bias1, bias2, bias3)
//        }
//    }

    /**
     * 计算brar
     *
     * 参数是26。
     * 公式N日BR=N日内（H－CY）之和除以N日内（CY－L）之和*100，
     * 其中，H为当日最高价，L为当日最低价，CY为前一交易日的收盘价，N为设定的时间参数。
     * N日AR=(N日内（H－O）之和除以N日内（O－L）之和)*100，
     * 其中，H为当日最高价，L为当日最低价，O为当日开盘价，N为设定的时间参数
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcBrar(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var br = 0.0
//        var ar = 0.0
//        var hcy = 0.0
//        var cyl = 0.0
//        var ho = 0.0
//        var ol = 0.0
//
//        return calc(dataList) { i ->
//            val highestPrice = dataList[i].high
//            val lowestPrice = dataList[i].low
//            val open = dataList[i].open
//            ho += (highestPrice - open)
//            ol += (open - lowestPrice)
//            if (i > 0) {
//                val refclose = dataList[i - 1].close
//                hcy += (highestPrice - refclose)
//                cyl += (refclose - lowestPrice)
//                if (i > 25) {
//                    val agoHighestPrice = dataList[i - 26].high
//                    val agoLowestPrice = dataList[i - 26].low
//                    val agoopen = dataList[i - 26].open
//                    if (i > 26) {
//                        val agoRefclose = dataList[i - 27].close
//                        hcy -= (agoHighestPrice - agoRefclose)
//                        cyl -= (agoRefclose - agoLowestPrice)
//                    }
//                    ho -= (agoHighestPrice - agoopen)
//                    ol -= (agoopen - agoLowestPrice)
//                }
//                ar = if (ol != 0.0) ho / ol * 100 else 0.0
//
//                br = if (cyl != 0.0) hcy / cyl * 100 else 0.0
//            }
//            dataList[i].brar = BrarModel(br, ar)
//        }
//    }

    /**
     * 计算cci
     * CCI（N日）=（TP－MA）÷MD÷0.015
     * 其中，TP=（最高价+最低价+收盘价）÷3
     * MA=近N日收盘价的累计之和÷N
     * MD=最近n日 (MA - TP)的绝对值的累计和 ÷ N
     *
     * 参数14
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcCci(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var closes = 0.0
//        var closeMa: Double
//        val closeMaList = mutableListOf<Double>()
//        var md: Double
//        var maCloseSum = 0.0
//        var cci: Double
//
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//            closes += close
//
//            val tp = (dataList[i].high + dataList[i].low + close) / 3
//            if (i < 13) {
//                closeMa = closes / (i + 1)
//                maCloseSum += abs(closeMa - close)
//                closeMaList.add(closeMa)
//                md = maCloseSum / (i + 1)
//            } else {
//                val agoclose = dataList[i - 13].close
//                closes -= agoclose
//                closeMa = closes / 13
//                closeMaList.add(closeMa)
//                maCloseSum += abs(closeMa - close)
//                maCloseSum -= abs(closeMaList[i - 13] - agoclose)
//                md = maCloseSum / 13
//            }
//            cci = if (md != 0.0) {
//                (tp - closeMa) / md / 0.015
//            } else {
//                0.0
//            }
//            dataList[i].cci = CciModel(cci)
//        }
//    }

    /**
     * 计算dmi
     *
     * 参数 14，6
     * MTR:=EXPMEMA(MAX(MAX(HIGH-LOW,ABS(HIGH-REF(CLOSE,1))),ABS(REF(CLOSE,1)-LOW)),N)
     * HD :=HIGH-REF(HIGH,1);
     * LD :=REF(LOW,1)-LOW;
     * DMP:=EXPMEMA(IF(HD>0&&HD>LD,HD,0),N);
     * DMM:=EXPMEMA(IF(LD>0&&LD>HD,LD,0),N);
     *
     * PDI: DMP*100/MTR;
     * MDI: DMM*100/MTR;
     * ADX: EXPMEMA(ABS(MDI-PDI)/(MDI+PDI)*100,MM);
     * ADXR:EXPMEMA(ADX,MM);
     *
     * 公式含义：
     * MTR赋值:最高价-最低价和最高价-昨收的绝对值的较大值和昨收-最低价的绝对值的较大值的N日指数平滑移动平均
     * HD赋值:最高价-昨日最高价
     * LD赋值:昨日最低价-最低价
     * DMP赋值:如果HD>0并且HD>LD,返回HD,否则返回0的N日指数平滑移动平均
     * DMM赋值:如果LD>0并且LD>HD,返回LD,否则返回0的N日指数平滑移动平均
     * 输出PDI: DMP*100/MTR
     * 输出MDI: DMM*100/MTR
     * 输出ADX: MDI-PDI的绝对值/(MDI+PDI)*100的MM日指数平滑移动平均
     * 输出ADXR:ADX的MM日指数平滑移动平均
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcDmi(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var pdi = 0.0
//        var mdi = 0.0
//        var adx = 0.0
//        var adxr = 0.0
//
//        val trList = mutableListOf(0.0)
//        var trSum = 0.0
//        val dmpList = mutableListOf(0.0)
//        var dmpSum = 0.0
//        val dmmList = mutableListOf(0.0)
//        var dmmSum = 0.0
//        val dxList = mutableListOf(0.0)
//        var dxSum = 0.0
//
//        return calc(dataList) { i ->
//            if (i > 0) {
//                val refClose = dataList[i - 1].close
//                val high = dataList[i].high
//                val low = dataList[i].low
//                val hl = high - low
//                val hcy = abs(high - refClose)
//                val lcy = abs(low - refClose)
//                val hhy = high - dataList[i - 1].high
//                val lyl = dataList[i - 1].low - low
//                val tr = max(max(hl, hcy), lcy)
//                trSum += tr
//                trList.add(tr)
//
//                val h = if (hhy > 0.0 && hhy > lyl) {
//                    hhy
//                } else {
//                    0.0
//                }
//                dmpSum += h
//                dmpList.add(h)
//
//                val l = if (lyl > 0 && lyl > hhy) {
//                    lyl
//                } else {
//                    0.0
//                }
//                dmmSum += l
//                dmmList.add(l)
//
//                if (i > 13) {
//                    trSum -= trList[i - 14]
//                    dmpSum -= dmpList[i - 14]
//                    dmmSum -= dmmList[i - 14]
//                }
//
//                if (trSum == 0.0) {
//                    pdi = 0.0
//                    mdi = 0.0
//                } else {
//                    pdi = dmpSum * 100 / trSum
//                    mdi = dmmSum * 100 / trSum
//                }
//
//                val dx = abs((mdi - pdi)) / (mdi + pdi) * 100
//                dxSum += dx
//                dxList.add(dx)
//                if (i < 6) {
//                    adx = dxSum / (i + 1)
//                    adxr = adx
//                } else {
//                    val agoAdx = dxList[i - 6]
//                    dxSum -= agoAdx
//                    adx = dxSum / 6
//                    adxr = (adx + agoAdx) / 2
//                }
//            }
//
//            dataList[i].dmi = DmiModel(pdi, mdi, adx, adxr)
//        }
//    }

    /**
     * 计算cr
     *
     * 参数26、10、20、40、60
     * MID:=REF(HIGH+LOW,1)/2;
     * CR:SUM(MAX(0,HIGH-MID),N)/SUM(MAX(0,MID-LOW),N)*100;
     * MA1:REF(MA(CR,M1),M1/2.5+1);
     * MA2:REF(MA(CR,M2),M2/2.5+1);
     * MA3:REF(MA(CR,M3),M3/2.5+1);
     * MA4:REF(MA(CR,M4),M4/2.5+1);
     * MID赋值:(昨日最高价+昨日最低价)/2
     * 输出带状能量线:0和最高价-MID的较大值的N日累和/0和MID-最低价的较大值的N日累和*100
     * 输出MA1:M1(5)/2.5+1日前的CR的M1(5)日简单移动平均
     * 输出MA2:M2(10)/2.5+1日前的CR的M2(10)日简单移动平均
     * 输出MA3:M3(20)/2.5+1日前的CR的M3(20)日简单移动平均
     * 输出MA4:M4/2.5+1日前的CR的M4日简单移动平均
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcCr(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var cr = 0.0
//        var ma1: Double
//        var ma2: Double
//        var ma3: Double
//        var ma4: Double
//        var p1 = 0.0
//        var p2 = 0.0
//        var ma10Sum = 0.0
//        var ma10: Double
//        val ma10List = mutableListOf<Double>()
//        var ma20Sum = 0.0
//        var ma20: Double
//        val ma20List = mutableListOf<Double>()
//        var ma40Sum = 0.0
//        var ma40: Double
//        val ma40List = mutableListOf<Double>()
//        var ma60Sum = 0.0
//        var ma60: Double
//        val ma60List = mutableListOf<Double>()
//
//        return calc(dataList) { i ->
//            if (i > 0) {
//                val preHighestPrice = dataList[i - 1].high
//                val preLowestPrice = dataList[i - 1].low
//                val preClose = dataList[i - 1].close
//                val preOpen = dataList[i - 1].open
//                val preMidPrice = (preHighestPrice + preClose + preLowestPrice + preOpen) / 4
//
//                val highestPrice = dataList[i].high
//                val lowestPrice = dataList[i].low
//
//                var highSubPreMid = highestPrice - preMidPrice
//                if (highSubPreMid < 0.0) {
//                    highSubPreMid = 0.0
//                }
//                p1 += highSubPreMid
//
//                var preMidSubLow = preMidPrice - lowestPrice
//                if (preMidSubLow < 0.0) {
//                    preMidSubLow = 0.0
//                }
//                p2 += preMidSubLow
//
//                if (i > 26) {
//                    val firstHighestPrice = dataList[i - 27].high
//                    val firstLowestPrice = dataList[i - 27].low
//                    val firstclose = dataList[i - 27].close
//                    val firstopen = dataList[i - 27].open
//                    val firstMidPrice = (firstHighestPrice + firstLowestPrice + firstclose + firstopen) / 4
//
//                    val secondHighestPrice = dataList[i - 26].high
//                    val secondLowestPrice = dataList[i - 26].low
//
//                    var secondHighSubFirstMid = secondHighestPrice - firstMidPrice
//                    if (secondHighSubFirstMid < 0.0) {
//                        secondHighSubFirstMid = 0.0
//                    }
//
//                    var firstMidSubSecondLow = firstMidPrice - secondLowestPrice
//                    if (firstMidSubSecondLow < 0.0) {
//                        firstMidSubSecondLow = 0.0
//                    }
//                    p1 -= secondHighSubFirstMid
//                    p2 -= firstMidSubSecondLow
//                }
//
//                if (p2 != 0.0) {
//                    cr = p1 / p2 * 100
//                }
//
//                val ym = (dataList[i - 1].high + dataList[i - 1].low + dataList[i - 1].close) / 3
//                val hym = dataList[i].high - ym
//                p1 += (if (0.0 >= hym) 0.0 else hym)
//                val lym = ym - dataList[i].low
//                p2 += (if (0.0 >= lym) 0.0 else lym)
//            }
//            ma10Sum += cr
//            ma20Sum += cr
//            ma40Sum += cr
//            ma60Sum += cr
//
//            if (i < 10) {
//                ma10 = ma10Sum / (i + 1)
//            } else {
//                ma10Sum -= dataList[i - 10].cr?.cr ?: 0.0
//                ma10 = ma10Sum / 10
//            }
//            ma10List.add(ma10)
//
//            if (i < 20) {
//                ma20 = ma20Sum / (i + 1)
//            } else {
//                ma20Sum -= dataList[i - 20].cr?.cr ?: 0.0
//                ma20 = ma20Sum / 20
//            }
//            ma20List.add(ma20)
//
//            if (i < 40) {
//                ma40 = ma40Sum / (i + 1)
//            } else {
//                ma40Sum -= dataList[i - 40].cr?.cr ?: 0.0
//                ma40 = ma40Sum / 40
//            }
//            ma40List.add(ma40)
//
//            if (i < 60) {
//                ma60 = ma60Sum / (i + 1)
//            } else {
//                ma60Sum -= dataList[i - 60].cr?.cr ?: 0.0
//                ma60 = ma60Sum / 60
//            }
//            ma60List.add(ma60)
//
//            ma1 = if (i < 5) {
//                ma10List[0]
//            } else {
//                ma10List[i - 5]
//            }
//
//            ma2 = if (i < 9) {
//                ma20List[0]
//            } else {
//                ma20List[i - 9]
//            }
//
//            ma3 = if (i < 17) {
//                ma40List[0]
//            } else {
//                ma40List[i - 17]
//            }
//
//            ma4 = if (i < 25) {
//                ma60List[0]
//            } else {
//                ma60List[i - 25]
//            }
//
//             dataList[i].cr = CrModel(cr, ma1, ma2, ma3, ma4)
//        }
//    }

    /**
     * 计算psy
     *
     * PSY：参数是12。公式：PSY=N日内的上涨天数/N×100%。
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcPsy(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var psy = 0.0
//        var upDay = 0.0
//        return calc(dataList) { i ->
//            if (i > 0) {
//                upDay += (if (dataList[i].close - dataList[i - 1].close > 0.0)  1.0 else 0.0)
//                if (i < 12) {
//                    psy = upDay / (i + 1) * 100
//                } else {
//                    if (i > 12) {
//                        upDay -= (if (dataList[i - 11].close - dataList[i - 12].close > 0) 1.0 else 0.0)
//                    }
//                    psy = upDay / 12 * 100
//                }
//            }
//            dataList[i].psy = PsyModel(psy)
//        }
//    }

    /**
     * 计算dma
     *
     * 参数是10、50、10。公式：DIF:MA(CLOSE,N1)-MA(CLOSE,N2);DIFMA:MA(DIF,M)
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcDma(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var dif: Double
//        var difMa: Double
//        var ma10s = 0.0
//        var ma10: Double
//        var ma50s = 0.0
//        var ma50: Double
//        var dif10s = 0.0
//
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//
//            ma10s += close
//            ma50s += close
//
//            if (i < 10) {
//                ma10 = ma10s / (i + 1)
//            } else {
//                ma10s -= dataList[i - 10].close
//                ma10 = ma10s / 10
//            }
//
//            if (i < 50) {
//                ma50 = ma50s / (i + 1)
//            } else {
//                ma50s -= dataList[i - 50].close
//                ma50 = ma50s / 50
//            }
//            dif = ma10 - ma50
//            dif10s += dif
//
//            if (i < 10) {
//                difMa = dif10s / (i + 1)
//            } else {
//                dif10s -= dataList[i - 10].dma?.dif ?: 0.0
//                difMa = dif10s / 10
//            }
//
//            dataList[i].dma = DmaModel(dif, difMa)
//        }
//    }

    /**
     * 计算trix
     *
     * TR=收盘价的N日指数移动平均的N日指数移动平均的N日指数移动平均；
     * TRIX=(TR-昨日TR)/昨日TR*100；
     * MATRIX=TRIX的M日简单移动平均；
     * 参数N设为12，参数M设为20；
     * 参数12、20
     * 公式：MTR:=EMA(EMA(EMA(CLOSE,N),N),N)
     * TRIX:(MTR-REF(MTR,1))/REF(MTR,1)*100;
     * TRMA:MA(TRIX,M)
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcTrix(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var trix = 0.0
//        var maTrix: Double
//        var sumTrix = 0.0
//
//        var emaClose1: Double
//        var oldEmaClose1 = 0.0
//
//        var emaClose2: Double
//        var oldEmaClose2 = 0.0
//
//        var emaClose3: Double
//        var oldEmaClose3 = 0.0
//        val emaClose3List = mutableListOf<Double>()
//
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//            if (i == 0) {
//                emaClose1 = close
//                emaClose2 = emaClose1
//                emaClose3 = emaClose2
//            } else {
//                emaClose1 = (2 * close + 11 * oldEmaClose1) / 13f
//                emaClose2 = (2 * emaClose1 + 11 * oldEmaClose2) / 13f
//                emaClose3 = (2 * emaClose2 + 11 * oldEmaClose3) / 13f
//                val refEmaClose3 = emaClose3List[i - 1]
//                trix = if (refEmaClose3 == 0.0) 0.0 else (emaClose3 - refEmaClose3) / refEmaClose3 * 100
//            }
//            oldEmaClose1 = emaClose1
//            oldEmaClose2 = emaClose2
//            oldEmaClose3 = emaClose3
//            emaClose3List.add(emaClose3)
//            sumTrix += trix
//            if (i < 20) {
//                maTrix = sumTrix / (i + 1)
//            } else {
//                //   sumTrix -= (dataList[i - 20].trix?.trix ?: 0.0)
//                maTrix = sumTrix / 20
//            }
//            //  dataList[i].trix = TrixModel(trix, maTrix)
//        }
//    }

    /**
     * 计算obv指标
     *
     * VA:=IF(CLOSE>REF(CLOSE,1),VOL,-VOL);
     * OBV:SUM(IF(CLOSE=REF(CLOSE,1),0,VA),0);
     * MAOBV:MA(OBV,M);
     * VA赋值:如果收盘价>昨收,返回成交量(手),否则返回-成交量(手)
     * 输出OBV:如果收盘价=昨收,返回0,否则返回VA的历史累和
     * 输出MAOBV:OBV的M日简单移动平均
     *
     * 参数30
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcObv(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var obv: Double
//        var sumObv = 0.0
//        var maObv: Double
//        var sumVa = 0.0
//        return calc(dataList) { i ->
//            val vol = dataList[i].vol
//            if (i == 0) {
//                obv = vol
//                sumVa += vol
//            } else {
//                val refclose = dataList[i - 1].close
//                val close = dataList[i].close
//                val va = if (close > refclose) vol else -vol
//
//                sumVa += va
//                obv = if (close == refclose) 0.0 else sumVa
//            }
//            sumObv += obv
//            if (i < 30) {
//                maObv = sumObv / (i + 1)
//            } else {
//                sumObv -= (dataList[i - 30].obv?.obv ?: 0.0)
//                maObv = sumObv / 30
//            }
//
//            dataList[i].obv = ObvModel(obv, maObv)
//        }
//    }

    /**
     * 计算vr指标
     *
     * 默认参数24 ， 30
     * VR=（AVS+1/2CVS）/（BVS+1/2CVS）
     * 24天以来凡是股价上涨那一天的成交量都称为AV，将24天内的AV总和相加后称为AVS
     * 24天以来凡是股价下跌那一天的成交量都称为BV，将24天内的BV总和相加后称为BVS
     * 24天以来凡是股价不涨不跌，则那一天的成交量都称为CV，将24天内的CV总和相加后称为CVS
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcVr(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var avs = 0.0
//        var bvs = 0.0
//        var cvs = 0.0
//        var vr = 0.0
//        var maVr: Double
//        var sumVr = 0.0
//
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//            val open = dataList[i].open
//            val vol = dataList[i].vol
//            when {
//                close > open -> avs += vol
//                close < open -> bvs += vol
//                else -> cvs += vol
//            }
//
//            if (i > 23) {
//                val agoclose = dataList[i - 24].close
//                val agoopen = dataList[i - 24].open
//                val agovol = dataList[i - 24].vol
//                when {
//                    agoclose > agoopen -> avs -= agovol
//                    agoclose < agoopen -> bvs -= agovol
//                    else -> cvs += agovol
//                }
//            }
//
//            val v = bvs + 1.0 / 2.0 * cvs
//            if (v != 0.0) {
//                vr = (avs + 1.0 / 2.0 * cvs) / v * 100
//            }
//            sumVr += vr
//            if (i < 30) {
//                maVr = sumVr / (i + 1)
//            } else {
//                sumVr -= dataList[i - 30].vr?.vr ?: 0.0
//                maVr = sumVr / 30
//            }
//
//            dataList[i].vr = VrModel(vr, maVr)
//        }
//    }

    /**
     * 计算wr指标
     *
     * 默认参数13 34 89
     * 公式 WR(N) = 100 * [ HIGH(N)-C ] / [ HIGH(N)-LOW(N) ]
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcWr(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var wr1: Double
//        var wr2: Double
//        var wr3: Double
//        var h1 = Double.NEGATIVE_INFINITY
//        var l1 = Double.POSITIVE_INFINITY
//        var h2 = Double.NEGATIVE_INFINITY
//        var l2 = Double.POSITIVE_INFINITY
//        var h3 = Double.NEGATIVE_INFINITY
//        var l3 = Double.POSITIVE_INFINITY
//
//        var hl1: Double
//        var hl2: Double
//        var hl3: Double
//        return calc(dataList) { i ->
//            val close = dataList[i].close
//            val high = dataList[i].high
//            val low = dataList[i].low
//            if (i < 13) {
//                h1 = max(high, h1)
//                l1 = min(low, l1)
//            } else {
//                val highlowArray = getHighLow(dataList.subList(i - 13, i))
//                h1 = highlowArray[0]
//                l1 = highlowArray[1]
//            }
//            hl1 = h1 - l1
//            wr1 = if (hl1 != 0.0) {
//                (h1 - close) / hl1 * 100
//            } else {
//                0.0
//            }
//
//            if (i < 34) {
//                h2 = max(high, h2)
//                l2 = min(low, l2)
//            } else {
//                val highlowArray = getHighLow(dataList.subList(i - 34, i))
//                h2 = highlowArray[0]
//                l2 = highlowArray[1]
//            }
//            hl2 = h2 - l2
//            wr2 = if (hl2 != 0.0) {
//                (h2 - close) / hl2 * 100
//            } else {
//                0.0
//            }
//
//            if (i < 89) {
//                h3 = max(high, h3)
//                l3 = min(low, l3)
//            } else {
//                val highlowArray = getHighLow(dataList.subList(i - 89, i))
//                h3 = highlowArray[0]
//                l3 = highlowArray[1]
//            }
//            hl3 = h3 - l3
//            wr3 = if (hl3 != 0.0) {
//                (h3 - close) / hl3 * 100
//            } else {
//                0.0
//            }
//
//            dataList[i].wr = WrModel(wr1, wr2, wr3)
//        }
//    }

    /**
     * 计算mtm指标
     *
     * 默认参数6 10
     * 公式 MTM（N日）=C－CN
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcMtm(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var mtm: Double
//        var mtmSum = 0.0
//        var mtmMa: Double
//
//        return calc(dataList) { i ->
//            if (i < 6) {
//                mtm = 0.0
//                mtmMa = 0.0
//            } else {
//                val close = dataList[i].close
//                mtm = close - dataList[i - 6].close
//                mtmSum += mtm
//                if (i < 16) {
//                    mtmMa = mtmSum / (i - 6 + 1)
//                } else {
//                    mtmMa = mtmSum / 10
//                    mtmSum -= dataList[i - 10].mtm?.mtm ?: 0.0
//                }
//            }
//            dataList[i].mtm = MtmModel(mtm, mtmMa)
//        }
//    }

    /**
     * 简易波动指标
     * 默认参数N为14，参数M为9
     * 公式：
     * A=（今日最高+今日最低）/2
     * B=（前日最高+前日最低）/2
     * C=今日最高-今日最低
     * EM=（A-B）*C/今日成交额
     * EMV=N日内EM的累和
     * MAEMV=EMV的M日的简单移动平均
     *
     * @param dataList MutableList<ChartDataEntity>
     * @return MutableList<ChartDataEntity>
     */
//    fun calcEmv(dataList: MutableList<ChartDataEntity>): MutableList<ChartDataEntity> {
//        var emv = 0.0
//        var maEmv: Double
//        var sumEmv = 0.0
//        var em = 0.0
//        val emList = mutableListOf<Double>()
//
//        return calc(dataList) { i ->
//            if (i > 0) {
//                val highestPrice = dataList[i].high
//                val lowestPrice = dataList[i].low
//                val preHighestPrice = dataList[i - 1].high
//                val preLowestPrice = dataList[i - 1].low
//                val highSubLow = highestPrice - lowestPrice
//                val halfHighAddLow = (highestPrice + lowestPrice) / 2
//                val preHalfHighAddLow = (preHighestPrice + preLowestPrice) / 2
//                em = (halfHighAddLow - preHalfHighAddLow) * highSubLow / dataList[i].amount
//            }
//            emList.add(em)
//            if (i < 14) {
//                emv += em
//            } else {
//                emv -= emList[i - 14]
//            }
//            sumEmv += emv
//            if (i < 9) {
//                maEmv = sumEmv / (i + 1)
//            } else {
//                sumEmv -= dataList[i - 9].emv?.emv ?: 0.0
//                maEmv = sumEmv / 9
//            }
//            dataList[i].emv = EmvModel(emv, maEmv)
//        }
//    }

    private inline fun calc(dataList: MutableList<ChartDataEntity>, calcIndicator: (index: Int) -> Unit): MutableList<ChartDataEntity> {
        val dataSize = dataList.size
        for (i in 0 until dataSize) {
            calcIndicator(i)
        }
        return dataList
    }

    /**
     * 获取最高最低价
     * @param list MutableList<ChartDataEntity>
     * @return DoubleArray
     */
//    private fun getHighLow(list: MutableList<ChartDataEntity>): DoubleArray {
//        var high = 0.0
//        var low = 0.0
//        val size = list.size
//        if (size > 0) {
//            high = list[0].high
//            low = list[0].low
//            for (i in 0 until size) {
//                high = max(list[i].high, high)
//                low = min(list[i].low, low)
//            }
//        }
//        return doubleArrayOf(high, low)
//    }
}