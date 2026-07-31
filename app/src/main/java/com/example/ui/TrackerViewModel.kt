package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BleDeviceEntity
import com.example.data.BleRepository
import com.example.data.CrashLogger
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

    // Context-Aware Motion State ("WALKING" vs "STILL")
    private val _motionState = MutableStateFlow("WALKING")
    val motionState: StateFlow<String> = _motionState

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
        CrashLogger.logSystemEvent("TrackerViewModel Initialized")
        startMockScanningLoop()
    }

    fun setTab(tab: TrackerTab) {
        _selectedTab.value = tab
        CrashLogger.logSystemEvent("UI Switched Tab to: $tab")
    }

    fun setSelectedDeviceForMap(macAddress: String?) {
        _selectedDeviceForMap.value = macAddress
        CrashLogger.logSystemEvent("Map focus set to MAC: ${macAddress ?: "All"}")
    }

    fun whitelistDevice(macAddress: String, name: String) {
        repository.addManualWhitelist(macAddress, name)
        CrashLogger.logSystemEvent("Device manually whitelisted: Name='$name', MAC=$macAddress")
    }

    fun removeFromWhitelist(macAddress: String) {
        repository.deleteIgnoredDevice(macAddress)
        CrashLogger.logSystemEvent("Device removed from whitelist: MAC=$macAddress")
    }

    fun toggleIgnoreDevice(device: BleDeviceEntity) {
        repository.toggleIgnoreDevice(device.macAddress, device.name, !device.isIgnored)
        CrashLogger.logSystemEvent("Toggled ignore state for ${device.macAddress} to: ${!device.isIgnored}")
    }

    fun purgeOldLogs() {
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        repository.purgeOldLogs(cutoff)
        CrashLogger.logSystemEvent("Purged historical logs older than 7 days")
    }

    fun clearDatabase() {
        repository.clearDatabase()
        CrashLogger.logSystemEvent("Cleared all proximity events and tracked devices from database")
    }

    fun toggleScan() {
        val nextState = !_isScanning.value
        _isScanning.value = nextState
        CrashLogger.logSystemEvent("Toggled scanner state to: ${if (nextState) "ACTIVE" else "STOPPED"}")
        if (nextState) {
            startMockScanningLoop()
        } else {
            stopMockScanningLoop()
        }
    }

    fun setMotionState(state: String) {
        _motionState.value = state
        CrashLogger.logSystemEvent("Context Activity changed to: $state. Scan rate auto-throttled.")
    }

    fun readDiagnostics(): String {
        return CrashLogger.readLogs()
    }

    fun clearDiagnostics() {
        CrashLogger.clearLogs()
    }

    private fun startMockScanningLoop() {
        stopMockScanningLoop()
        scannerSimulationJob = viewModelScope.launch {
            var tick = 0
            while (true) {
                // Read current motion state and throttle scan frequency
                val currentMotion = _motionState.value
                val scanDelay = if (currentMotion == "STILL") 10000L else 3000L
                delay(scanDelay)
                tick++

                // Shift coordinates only when active (simulating walking/driving)
                if (currentMotion == "WALKING") {
                    baseLat += (Random.nextDouble() - 0.5) * 0.0003
                    baseLon += (Random.nextDouble() - 0.5) * 0.0003
                }

                // Simulate Smart Payload Parsing & Telemetry Logging
                if (tick % 2 == 0) {
                    val rawRssi = Random.nextInt(-85, -45)
                    CrashLogger.logSystemEvent("Parsed BLE payload [Sony WH-1000XM4] -> Service UUID: 0x0002, ConnectionState: Active, Signal Strength: $rawRssi dBm")
                    repository.processScannedDevice(
                        macAddress = "E3:42:1B:90:05:A1",
                        name = "Sony WH-1000XM4",
                        deviceType = "Audio Device",
                        rssi = rawRssi,
                        currentLat = baseLat + (Random.nextDouble() - 0.5) * 0.0001,
                        currentLon = baseLon + (Random.nextDouble() - 0.5) * 0.0001
                    )
                }

                if (tick % 3 == 0) {
                    val rawRssi = Random.nextInt(-75, -50)
                    CrashLogger.logSystemEvent("Parsed BLE payload [Apple AirTag] -> Service UUID: 0xFD44, BatteryTelemetry: OK, Signal Strength: $rawRssi dBm")
                    repository.processScannedDevice(
                        macAddress = "D8:96:A7:24:FC:F0",
                        name = "Apple AirTag (Unrecognized)",
                        deviceType = "AirTag / Smart Tag",
                        rssi = rawRssi,
                        currentLat = baseLat + (Random.nextDouble() - 0.5) * 0.00008,
                        currentLon = baseLon + (Random.nextDouble() - 0.5) * 0.00008
                    )
                }

                if (tick % 5 == 0) {
                    val rawRssi = Random.nextInt(-95, -70)
                    CrashLogger.logSystemEvent("Parsed BLE payload [Tile Slim] -> Service UUID: 0xFEED, BatteryTelemetry: Good, Signal Strength: $rawRssi dBm")
                    repository.processScannedDevice(
                        macAddress = "FF:EE:DD:CC:BB:AA",
                        name = "Tile Slim",
                        deviceType = "BLE Beacon",
                        rssi = rawRssi,
                        currentLat = baseLat + 0.0015,
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
