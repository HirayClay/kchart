package com.bitmart.demo.util

import android.content.Context
import android.content.SharedPreferences
import com.bitmart.demo.MyApplication
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharePreferencesDelegate<T>(private val default: T) : ReadWriteProperty<Any?, T> {

    companion object {
        val sharedPreferences: SharedPreferences = MyApplication.instance.getSharedPreferences("sp_bitmart_demo", Context.MODE_PRIVATE)
    }

    private val timer = Timer()

    private var task: TimerTask? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return sharedPreferences.findPreference(property.name, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        task?.cancel()
        task = object : TimerTask() {
            override fun run() {
                sharedPreferences.putPreference(property.name, value)
                task?.cancel()
                task = null
            }
        }
        timer.schedule(task, 1500L)
    }

    private fun <U> SharedPreferences.findPreference(name: String, default: U): U = with(this) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default) ?: default
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> {
                throw IllegalArgumentException("not support type ${default.toString()}")
            }
        }
        return@with res as U
    }

    private fun <T> SharedPreferences.putPreference(name: String, value: T) = with(this.edit()) {

        println("putPreference  $name $value")

        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> {
                throw IllegalArgumentException("not support type ${value.toString()}")
            }
        }.apply()
    }

}