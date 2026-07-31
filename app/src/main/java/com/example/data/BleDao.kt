package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BleDao {
    @Query("SELECT * FROM ble_devices ORDER BY lastSeen DESC")
    fun getAllDevicesFlow(): Flow<List<BleDeviceEntity>>

    @Query("SELECT * FROM ble_devices WHERE isStalkerAlert = 1 AND isIgnored = 0 ORDER BY stalkerRiskScore DESC, lastSeen DESC")
    fun getActiveThreatsFlow(): Flow<List<BleDeviceEntity>>

    @Query("SELECT * FROM ble_devices WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getDeviceByMac(macAddress: String): BleDeviceEntity?

    @Query("SELECT * FROM ignored_devices ORDER BY addedAt DESC")
    fun getIgnoredDevicesFlow(): Flow<List<WhitelistedDeviceEntity>>

    @Query("SELECT * FROM proximity_events WHERE macAddress = :macAddress ORDER BY timestamp ASC")
    fun getProximityEventsForDeviceFlow(macAddress: String): Flow<List<ProximityEventEntity>>

    @Query("SELECT * FROM proximity_events WHERE macAddress = :macAddress ORDER BY timestamp ASC")
    suspend fun getProximityEventsForDeviceList(macAddress: String): List<ProximityEventEntity>

    @Query("SELECT * FROM proximity_events ORDER BY timestamp DESC")
    fun getAllProximityEventsFlow(): Flow<List<ProximityEventEntity>>

    @Query("SELECT * FROM proximity_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentProximityEventsFlow(limit: Int = 100): Flow<List<ProximityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: BleDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProximityEvent(event: ProximityEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIgnoredDevice(ignored: WhitelistedDeviceEntity)

    @Query("DELETE FROM ignored_devices WHERE macAddress = :macAddress")
    suspend fun deleteIgnoredDevice(macAddress: String)

    @Query("UPDATE ble_devices SET isIgnored = :isIgnored WHERE macAddress = :macAddress")
    suspend fun updateDeviceIgnoredState(macAddress: String, isIgnored: Boolean)

    @Query("SELECT COUNT(*) FROM ble_devices")
    fun getDeviceCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM proximity_events")
    fun getTotalProximityEventsCountFlow(): Flow<Int>

    @Query("SELECT SUM(suppressedPingCount) FROM ble_devices")
    fun getTotalSuppressedPingsFlow(): Flow<Int?>

    @Query("DELETE FROM proximity_events")
    suspend fun clearProximityEvents()

    @Query("DELETE FROM ble_devices")
    suspend fun clearDevices()

    @Query("DELETE FROM proximity_events WHERE timestamp < :cutoffTimestamp")
    suspend fun purgeOldEvents(cutoffTimestamp: Long)
}
