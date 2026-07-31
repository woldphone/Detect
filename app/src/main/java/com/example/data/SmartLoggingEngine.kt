package com.example.data

import kotlin.math.*

object SmartLoggingEngine {

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
     * Evaluates distinct spatial clusters and temporal span.
     */
    fun calculateStalkerRisk(
        events: List<ProximityEventEntity>,
        deviceSightingCount: Int
    ): Pair<Int, Boolean> {
        if (events.size < 2) return Pair(0, false)

        // Count distinct location clusters (> 80 meters apart)
        val clusters = mutableListOf<Pair<Double, Double>>()
        for (event in events) {
            val isDistinct = clusters.none { (cLat, cLon) ->
                calculateDistanceMeters(cLat, cLon, event.latitude, event.longitude) < 80.0
            }
            if (isDistinct) {
                clusters.add(Pair(event.latitude, event.longitude))
            }
        }

        val firstTimestamp = events.firstOrNull()?.timestamp ?: 0L
        val lastTimestamp = events.lastOrNull()?.timestamp ?: 0L
        val timeSpanSpanMinutes = (lastTimestamp - firstTimestamp) / (1000 * 60)

        val clusterCount = clusters.size

        // Threat rules:
        // 1. Detected across >= 3 separate location clusters -> High Threat
        // 2. Detected in >= 2 location clusters spanning >= 10 minutes -> Medium-High Threat
        // 3. Repeated pings over 20+ minutes even if localized -> Low-Medium Risk

        var riskScore = 0
        if (clusterCount >= 3) {
            riskScore += 60 + (clusterCount - 3) * 15
        } else if (clusterCount == 2) {
            riskScore += 35
        } else {
            riskScore += 10
        }

        if (timeSpanSpanMinutes >= 15) {
            riskScore += 30
        } else if (timeSpanSpanMinutes >= 8) {
            riskScore += 15
        }

        if (deviceSightingCount >= 4) {
            riskScore += 15
        }

        val finalScore = min(100, max(0, riskScore))
        val isAlert = finalScore >= 60 || clusterCount >= 3

        return Pair(finalScore, isAlert)
    }
}
