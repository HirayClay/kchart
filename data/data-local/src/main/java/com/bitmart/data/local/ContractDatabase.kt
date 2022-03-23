package com.bitmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bitmart.data.local.entity.ContractEntity

@Database(entities = [ContractEntity::class], version = 1)
abstract  class ContractDatabase: RoomDatabase() {
}