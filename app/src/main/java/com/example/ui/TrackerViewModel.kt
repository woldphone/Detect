package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BleDeviceEntity
import com.example.data.BleRepository
import com.example.data.ProximityEventEntity
import com.example.data.WhitelistedDeviceEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BleRepository.getInstance(application)

    // UI Tab state
    private val _selectedTab = MutableStateFlow(TrackerTab.DASHBOARD)
    val selectedTab: StateFlow<TrackerTab> = _selectedTab

    // Focus state for Locations Map screen
    private val _selectedDeviceForMap = MutableStateFlow<String?>(null)
    val selectedDeviceForMap: StateFlow<String?> = _selectedDeviceForMap

    // Scanning state
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning

    // Database flow bindings
    val allDevices: StateFlow<List<BleDeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeThreats: StateFlow<List<BleDeviceEntity>> = repository.activeThreats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedDevices: StateFlow<List<WhitelistedDeviceEntity>> = repository.ignoredDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<ProximityEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined stats flow
    val smartLoggingStats: StateFlow<SmartLoggingStats> = combine(
        repository.deviceCount,
        repository.eventsCount,
        repository.suppressedPingsCount
    ) { deviceCount, eventsCount, suppressedCount ->
        SmartLoggingStats(
            totalDevices = deviceCount,
            totalEvents = eventsCount,
            suppressedPings = suppressedCount ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SmartLoggingStats(0, 0, 0))

    // Background scanner job simulation
    private var scannerSimulationJob: Job? = null
    private var baseLat = 37.7749
    private var baseLon = -122.4194

    init {
        startMockScanningLoop()
    }

    fun setTab(tab: TrackerTab) {
        _selectedTab.value = tab
    }

    fun setSelectedDeviceForMap(macAddress: String?) {
        _selectedDeviceForMap.value = macAddress
    }

    fun whitelistDevice(macAddress: String, name: String) {
        repository.addManualWhitelist(macAddress, name)
    }

    fun removeFromWhitelist(macAddress: String) {
        repository.deleteIgnoredDevice(macAddress)
    }

    fun toggleIgnoreDevice(device: BleDeviceEntity) {
        repository.toggleIgnoreDevice(device.macAddress, device.name, !device.isIgnored)
    }

    fun purgeOldLogs() {
        // Cutoff older than 7 days
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        repository.purgeOldLogs(cutoff)
    }

    fun clearDatabase() {
        repository.clearDatabase()
    }

    fun toggleScan() {
        val nextState = !_isScanning.value
        _isScanning.value = nextState
        if (nextState) {
            startMockScanningLoop()
        } else {
            stopMockScanningLoop()
        }
    }

    private fun startMockScanningLoop() {
        stopMockScanningLoop()
        scannerSimulationJob = viewModelScope.launch {
            // Keep simulating pings as long as scanning is on
            var tick = 0
            while (true) {
                delay(3000) // periodic ping simulation
                tick++

                // Shift base location slightly to simulate walking/movement
                baseLat += (Random.nextDouble() - 0.5) * 0.0003
                baseLon += (Random.nextDouble() - 0.5) * 0.0003

                // Simulate 1 standard device (e.g. user smartwatch or random passerby)
                if (tick % 2 == 0) {
                    repository.processScannedDevice(
                        macAddress = "E3:42:1B:90:05:A1",
                        name = "Sony WH-1000XM4",
                        deviceType = "Audio Device",
                        rssi = Random.nextInt(-85, -45),
                        currentLat = baseLat + (Random.nextDouble() - 0.5) * 0.0001,
                        currentLon = baseLon + (Random.nextDouble() - 0.5) * 0.0001
                    )
                }

                // Simulate a persistent tracking device (airtag/smarttag following the user)
                // This triggers the stalker calculation logic and creates alerts
                if (tick % 3 == 0) {
                    repository.processScannedDevice(
                        macAddress = "D8:96:A7:24:FC:F0",
                        name = "Apple AirTag (Unrecognized)",
                        deviceType = "AirTag / Smart Tag",
                        rssi = Random.nextInt(-75, -50),
                        // Very close to the user's current moving location (simulating stalker proximity)
                        currentLat = baseLat + (Random.nextDouble() - 0.5) * 0.00008,
                        currentLon = baseLon + (Random.nextDouble() - 0.5) * 0.00008
                    )
                }

                // Occasionally simulate another unknown BLE beacon passing by (seen once)
                if (tick % 5 == 0) {
                    repository.processScannedDevice(
                        macAddress = "FF:EE:DD:CC:BB:AA",
                        name = "Tile Slim",
                        deviceType = "BLE Beacon",
                        rssi = Random.nextInt(-95, -70),
                        currentLat = baseLat + 0.0015, // far away
                        currentLon = baseLon - 0.0012
                    )
                }
            }
        }
    }

    private fun stopMockScanningLoop() {
        scannerSimulationJob?.cancel()
        scannerSimulationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopMockScanningLoop()
    }
}
