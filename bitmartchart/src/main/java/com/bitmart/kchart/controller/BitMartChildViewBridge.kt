package com.bitmart.kchart.controller

interface BitMartChildViewBridge {

    fun onDataSetChanged()

    fun onDataSetAdd(newDataSize: Int)

    fun finishLoadMore(noMore:Boolean)
}