package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CourierEntity
import com.example.data.local.entity.LaundromatEntity
import com.example.data.local.entity.OfflineQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaundromatDao {
    @Query("SELECT * FROM laundromats")
    fun getAll(): Flow<List<LaundromatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<LaundromatEntity>)
}

@Dao
interface CourierDao {
    @Query("SELECT * FROM couriers")
    fun getAll(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getCourierById(id: String): CourierEntity?

    @Query("UPDATE couriers SET currentLat = :lat, currentLng = :lng, headingDegrees = :heading, speedKmh = :speed WHERE id = :id")
    suspend fun updateLocation(id: String, lat: Double, lng: Double, heading: Float, speed: Float)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(courier: CourierEntity)
}

@Dao
interface OfflineQueueDao {
    @Query("SELECT * FROM offline_queue ORDER BY timestamp ASC")
    fun getPendingActions(): Flow<List<OfflineQueueEntity>>

    @Query("SELECT * FROM offline_queue ORDER BY timestamp ASC")
    suspend fun getPendingActionsList(): List<OfflineQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun queueAction(action: OfflineQueueEntity)

    @Query("DELETE FROM offline_queue WHERE queueId = :id")
    suspend fun removeAction(id: Long)

    @Query("UPDATE offline_queue SET retryCount = retryCount + 1 WHERE queueId = :id")
    suspend fun incrementRetry(id: Long)
}
