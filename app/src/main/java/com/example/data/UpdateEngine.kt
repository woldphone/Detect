package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UpdateEngine {

    private const val METADATA_URL = "https://raw.githubusercontent.com/woldphone/Detect/build-artifacts/update_metadata.json"

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable

    private val _latestVersionName = MutableStateFlow("")
    val latestVersionName: StateFlow<String> = _latestVersionName

    private val _downloadProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private val _downloadState = MutableStateFlow("IDLE") // IDLE, DOWNLOADING, SUCCESS, ERROR
    val downloadState: StateFlow<String> = _downloadState

    private var latestApkUrl = ""

    /**
     * Queries the raw GitHub update_metadata.json to check for new version codes.
     */
    suspend fun checkForUpdates(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                CrashLogger.logSystemEvent("Checking for updates from remote repository...")
                val url = URL(METADATA_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(rawJson)

                    val remoteVersionCode = json.optInt("versionCode", 0)
                    val remoteVersionName = json.optString("versionName", "1.0")
                    latestApkUrl = json.optString("downloadUrl", "")

                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val hasUpdate = remoteVersionCode > currentVersionCode

                    _isUpdateAvailable.value = hasUpdate
                    _latestVersionName.value = remoteVersionName

                    CrashLogger.logSystemEvent("Update check completed. Current: $currentVersionCode, Remote: $remoteVersionCode. Update Available: $hasUpdate")
                    hasUpdate
                } else {
                    CrashLogger.logSystemEvent("Failed to check updates: Server responded with code ${connection.responseCode}")
                    false
                }
            } catch (e: Exception) {
                CrashLogger.logSystemEvent("Error checking for updates: ${e.message}")
                false
            }
        }
    }

    /**
     * Downloads the APK file to cache and fires intent to trigger automatic package installer.
     */
    suspend fun downloadAndInstallUpdate(context: Context) {
        if (latestApkUrl.isBlank()) {
            _downloadState.value = "ERROR"
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _downloadState.value = "DOWNLOADING"
                _downloadProgress.value = 0f
                CrashLogger.logSystemEvent("Starting automatic update download from: $latestApkUrl")

                val url = URL(latestApkUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    _downloadState.value = "ERROR"
                    CrashLogger.logSystemEvent("Failed to download update APK: Server responded with ${connection.responseCode}")
                    return@withContext
                }

                val fileLength = connection.contentLength
                val cacheDir = context.cacheDir
                val apkFile = File(cacheDir, "sentinel_guard_update.apk")

                if (apkFile.exists()) {
                    apkFile.delete()
                }

                BufferedInputStream(connection.inputStream).use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val data = ByteArray(4096)
                        var total: Long = 0
                        var count: Int

                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            if (fileLength > 0) {
                                _downloadProgress.value = total.toFloat() / fileLength
                            }
                            output.write(data, 0, count)
                        }
                    }
                }

                _downloadState.value = "SUCCESS"
                CrashLogger.logSystemEvent("Update APK downloaded successfully to cache directory: ${apkFile.absolutePath}")

                // Launch package installer immediately!
                launchInstaller(context, apkFile)

            } catch (e: Exception) {
                _downloadState.value = "ERROR"
                CrashLogger.logSystemEvent("Failed to complete update download: ${e.message}")
            }
        }
    }

    private fun launchInstaller(context: Context, file: File) {
        try {
            CrashLogger.logSystemEvent("Launching Package Installer for download: ${file.name}")
            val authority = "com.aistudio.bletrackerguard.xkqz.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            CrashLogger.logSystemEvent("Failed to launch package installer: ${e.message}")
        }
    }
}
