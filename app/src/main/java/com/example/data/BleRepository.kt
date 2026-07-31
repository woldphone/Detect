package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BleRepository private constructor(context: Context) {

    private val db = AppDatabase.getDatabase(context.applicationContext)
    private val dao = db.bleDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val allDevices: Flow<List<BleDeviceEntity>> = dao.getAllDevicesFlow()
    val activeThreats: Flow<List<BleDeviceEntity>> = dao.getActiveThreatsFlow()
    val ignoredDevices: Flow<List<IgnoredDeviceEntity>> = dao.getIgnoredDevicesFlow()
    val recentEvents: Flow<List<ProximityEventEntity>> = dao.getRecentProximityEventsFlow(100)
    val allEvents: Flow<List<ProximityEventEntity>> = dao.getAllProximityEventsFlow()
    val deviceCount: Flow<Int> = dao.getDeviceCountFlow()
    val eventsCount: Flow<Int> = dao.getTotalProximityEventsCountFlow()
    val suppressedPingsCount: Flow<Int?> = dao.getTotalSuppressedPingsFlow()

    fun getEventsForDevice(macAddress: String): Flow<List<ProximityEventEntity>> {
        return dao.getProximityEventsForDeviceFlow(macAddress)
    }

    suspend fun processScannedDevice(
        macAddress: String,
        name: String?,
        deviceType: String,
        rssi: Int,
        currentLat: Double,
        currentLon: Double
    ) {
        val now = System.currentTimeMillis()
        val existingDevice = dao.getDeviceByMac(macAddress)

        // Check if device is ignored / whitelisted
        if (existingDevice?.isIgnored == true) {
            // Update last seen timestamp but suppress logging
            val updated = existingDevice.copy(
                lastSeen = now,
                rssi = rssi,
                suppressedPingCount = existingDevice.suppressedPingCount + 1
            )
            dao.insertOrUpdateDevice(updated)
            return
        }

        val shouldLog = SmartLoggingEngine.shouldLogNewEvent(
            lastLat = existingDevice?.lastLatitude,
            lastLon = existingDevice?.lastLongitude,
            lastTimestamp = existingDevice?.lastSeen,
            currentLat = currentLat,
            currentLon = currentLon,
            currentTimestamp = now
        )

        var newSightings = (existingDevice?.totalSightingsCount ?: 0) + 1
        var newSuppressed = existingDevice?.suppressedPingCount ?: 0

        if (shouldLog) {
            val event = ProximityEventEntity(
                macAddress = macAddress,
                timestamp = now,
                latitude = currentLat,
                longitude = currentLon,
                rssi = rssi
            )
            dao.insertProximityEvent(event)
        } else {
            newSuppressed += 1
        }

        // Fetch events for threat calculation
        val deviceEvents = dao.getProximityEventsForDeviceList(macAddress)
        val (riskScore, isStalker) = SmartLoggingEngine.calculateStalkerRisk(
            events = deviceEvents,
            deviceSightingCount = newSightings
        )

        val updatedDevice = BleDeviceEntity(
            macAddress = macAddress,
            name = name ?: existingDevice?.name,
            deviceType = deviceType,
            rssi = rssi,
            isIgnored = existingDevice?.isIgnored ?: false,
            firstSeen = existingDevice?.firstSeen ?: now,
            lastSeen = now,
            totalSightingsCount = newSightings,
            suppressedPingCount = newSuppressed,
            stalkerRiskScore = riskScore,
            isStalkerAlert = isStalker || (existingDevice?.isStalkerAlert == true && riskScore > 40),
            lastLatitude = currentLat,
            lastLongitude = currentLon
        )

        dao.insertOrUpdateDevice(updatedDevice)
    }

    fun toggleIgnoreDevice(macAddress: String, name: String?, isIgnored: Boolean) {
        repositoryScope.launch {
            dao.updateDeviceIgnoredState(macAddress, isIgnored)
            if (isIgnored) {
                val ignored = IgnoredDeviceEntity(
                    macAddress = macAddress,
                    deviceName = name ?: "Whitelisted Device ($macAddress)",
                    addedAt = System.currentTimeMillis()
                )
                dao.insertIgnoredDevice(ignored)
            } else {
                dao.deleteIgnoredDevice(macAddress)
            }
        }
    }

    fun addManualWhitelist(macAddress: String, name: String) {
        repositoryScope.launch {
            val now = System.currentTimeMillis()
            val existing = dao.getDeviceByMac(macAddress)
            if (existing != null) {
                dao.updateDeviceIgnoredState(macAddress, true)
            } else {
                val newDevice = BleDeviceEntity(
                    macAddress = macAddress,
                    name = name,
                    deviceType = "Trusted User Device",
                    rssi = -60,
                    isIgnored = true,
                    firstSeen = now,
                    lastSeen = now
                )
                dao.insertOrUpdateDevice(newDevice)
            }

            val ignored = IgnoredDeviceEntity(
                macAddress = macAddress,
                deviceName = name,
                addedAt = now
            )
            dao.insertIgnoredDevice(ignored)
        }
    }

    fun deleteIgnoredDevice(macAddress: String) {
        repositoryScope.launch {
            dao.deleteIgnoredDevice(macAddress)
            dao.updateDeviceIgnoredState(macAddress, false)
        }
    }

    fun clearDatabase() {
        repositoryScope.launch {
            dao.clearProximityEvents()
            dao.clearDevices()
        }
    }

    fun purgeOldLogs(cutoffMillis: Long) {
        repositoryScope.launch {
            dao.purgeOldEvents(cutoffMillis)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BleRepository? = null

        fun getInstance(context: Context): BleRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BleRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
