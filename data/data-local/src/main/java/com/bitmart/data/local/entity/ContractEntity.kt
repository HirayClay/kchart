package com.bitmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contract")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "pair")
    val pair: String,
)
