package com.example.data

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

object SmartLoggingEngine {

    private val rssiHistory = ConcurrentHashMap<String, MutableList<Int>>()

    /**
     * Smooths out raw RSSI volatility using a sliding-window average of size 5.
     */
    fun getSmoothedRssi(macAddress: String, rawRssi: Int): Int {
        val history = rssiHistory.getOrPut(macAddress) { Collections.synchronizedList(mutableListOf()) }
        synchronized(history) {
            history.add(rawRssi)
            if (history.size > 5) {
                history.removeAt(0)
            }
            return history.average().toInt()
        }
    }

    /**
     * Clears signal histories when resetting/clearing database.
     */
    fun clearSignalHistories() {
        rssiHistory.clear()
    }

    /**
     * Calculates the distance in meters between two lat/lon coordinates using Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Evaluates whether a new ping should trigger a new ProximityEvent log
     * or be suppressed to conserve storage, battery, and CPU.
     */
    fun shouldLogNewEvent(
        lastLat: Double?,
        lastLon: Double?,
        lastTimestamp: Long?,
        currentLat: Double,
        currentLon: Double,
        currentTimestamp: Long
    ): Boolean {
        if (lastLat == null || lastLon == null || lastTimestamp == null) {
            return true
        }

        val distanceMeters = calculateDistanceMeters(lastLat, lastLon, currentLat, currentLon)
        val timeDiffMillis = currentTimestamp - lastTimestamp

        // Throttling Rule:
        // If device is in the exact same spot (<15m away) AND logged recently (<3 minutes ago), suppress!
        val isSameLocation = distanceMeters < 15.0
        val isRecent = timeDiffMillis < 3 * 60 * 1000 // 3 minutes

        return !(isSameLocation && isRecent)
    }

    /**
     * Evaluates if a BLE device meets stalker / tracking threat thresholds.
     * Integrates Trajectory Co-location and Time-of-Arrival (ToA) correlation
     * to distinguish stationary background signals from active stalkers.
     */
    fun calculateStalkerRisk(
        events: List<ProximityEventEntity>,
        deviceSightingCount: Int
    ): Pair<Int, Boolean> {
        if (events.size < 2) return Pair(0, false)

        val sortedEvents = events.sortedBy { it.timestamp }
        val firstEvent = sortedEvents.first()
        val lastEvent = sortedEvents.last()

        // 1. Calculate overall distance between first and last encounter (to check if it travels with us)
        val spanDistance = calculateDistanceMeters(
            firstEvent.latitude, firstEvent.longitude,
            lastEvent.latitude, lastEvent.longitude
        )

        // 2. Count distinct spatial location clusters (> 80 meters apart)
        val clusters = mutableListOf<Pair<Double, Double>>()
        for (event in sortedEvents) {
            val isDistinct = clusters.none { (cLat, cLon) ->
                calculateDistanceMeters(cLat, cLon, event.latitude, event.longitude) < 80.0
            }
            if (isDistinct) {
                clusters.add(Pair(event.latitude, event.longitude))
            }
        }

        val firstTimestamp = firstEvent.timestamp
        val lastTimestamp = lastEvent.timestamp
        val timeSpanMinutes = (lastTimestamp - firstTimestamp) / (1000 * 60)

        val clusterCount = clusters.size

        // Trajectory Correlation Rule:
        // If seen frequently over a long time (>5 minutes) but always within the exact same location/cluster
        // (spanDistance < 20 meters), it is a static neighbor or office beacon, NOT a stalker!
        val isStaticBeacon = spanDistance < 20.0 && timeSpanMinutes > 5

        var riskScore = 0
        if (isStaticBeacon) {
            // Highly suppressed risk because it doesn't move with the user's trajectory
            riskScore = 5
        } else {
            // Mobile Threat Evaluation (Active Trajectory Correlation)
            if (clusterCount >= 3) {
                riskScore += 60 + (clusterCount - 3) * 15
            } else if (clusterCount == 2) {
                riskScore += 35
            } else {
                riskScore += 10
            }

            if (timeSpanMinutes >= 15) {
                riskScore += 30
            } else if (timeSpanMinutes >= 8) {
                riskScore += 15
            }

            if (deviceSightingCount >= 4) {
                riskScore += 15
            }
        }

        val finalScore = min(100, max(0, riskScore))
        val isAlert = finalScore >= 60 && !isStaticBeacon

        return Pair(finalScore, isAlert)
    }
}
