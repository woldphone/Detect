package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ble_devices")
data class BleDeviceEntity(
    @PrimaryKey val macAddress: String,
    val name: String?,
    val deviceType: String, // e.g. "AirTag / Smart Tag", "Smartwatch", "BLE Beacon", "Audio Device", "Unknown BLE"
    val rssi: Int,
    val isIgnored: Boolean = false,
    val firstSeen: Long,
    val lastSeen: Long,
    val totalSightingsCount: Int = 1,
    val suppressedPingCount: Int = 0,
    val stalkerRiskScore: Int = 0, // 0 to 100
    val isStalkerAlert: Boolean = false,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null
)

@Entity(
    tableName = "proximity_events",
    foreignKeys = [
        ForeignKey(
            entity = BleDeviceEntity::class,
            parentColumns = ["macAddress"],
            childColumns = ["macAddress"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["macAddress"]), Index(value = ["timestamp"])]
)
data class ProximityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macAddress: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val rssi: Int,
    val locationTag: String = "Location Pin"
)

@Entity(tableName = "ignored_devices")
data class WhitelistedDeviceEntity(
    @PrimaryKey val macAddress: String,
    val deviceName: String,
    val addedAt: Long,
    val note: String = "User Whitelisted"
)
