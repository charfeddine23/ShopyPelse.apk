package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SpendingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingDao {
    @Query("SELECT * FROM spending_records ORDER BY timestamp DESC")
    fun getAllSpendingRecords(): Flow<List<SpendingRecord>>

    @Query("SELECT * FROM spending_records WHERE monthKey = :monthKey ORDER BY timestamp DESC")
    fun getSpendingForMonth(monthKey: String): Flow<List<SpendingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpendingRecord(record: SpendingRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpendingRecords(records: List<SpendingRecord>)

    @Query("DELETE FROM spending_records WHERE id = :id")
    suspend fun deleteSpendingRecord(id: Long)

    @Query("DELETE FROM spending_records")
    suspend fun clearAll()
}
