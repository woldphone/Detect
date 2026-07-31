package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    private const val LOG_FILE_NAME = "sentinel_diagnostics.log"
    private lateinit var logFile: File

    fun init(context: Context) {
        logFile = File(context.filesDir, LOG_FILE_NAME)

        // Install Global Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(thread, throwable)
            // Delegate back to system/default handler to let the system crash standardly
            defaultHandler?.uncaughtException(thread, throwable)
        }

        logSystemEvent("CRASH LOGGER INITIALIZED SUCCESSFULLY")
    }

    @Synchronized
    fun logCrash(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTraceStr = sw.toString()

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val crashReport = """
                ==================================================
                CRASH DETECTED AT $timestamp
                Thread: ${thread.name} (ID: ${thread.id})
                Exception: ${throwable.javaClass.name}
                Message: ${throwable.localizedMessage}

                Stack Trace:
                $stackTraceStr
                ==================================================

            """.trimIndent()

            writeTextToFile(crashReport)
            Log.e("CrashLogger", "Recorded uncaught exception successfully!")
        } catch (e: Exception) {
            Log.e("CrashLogger", "Failed to write crash log: ${e.message}")
        }
    }

    @Synchronized
    fun logSystemEvent(message: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val eventLine = "[$timestamp] [SYSTEM] $message\n"
            writeTextToFile(eventLine)
            Log.i("CrashLogger", "System event logged: $message")
        } catch (e: Exception) {
            Log.e("CrashLogger", "Failed to write system log: ${e.message}")
        }
    }

    @Synchronized
    fun readLogs(): String {
        return try {
            if (logFile.exists()) {
                logFile.readText()
            } else {
                "No diagnostic or crash logs recorded yet."
            }
        } catch (e: Exception) {
            "Error reading diagnostic logs: ${e.message}"
        }
    }

    @Synchronized
    fun clearLogs() {
        try {
            if (logFile.exists()) {
                logFile.delete()
            }
            logSystemEvent("DIAGNOSTIC LOGS RESET BY USER")
        } catch (e: Exception) {
            Log.e("CrashLogger", "Failed to clear logs: ${e.message}")
        }
    }

    private fun writeTextToFile(text: String) {
        if (!::logFile.isInitialized) return
        FileWriter(logFile, true).use { writer ->
            writer.write(text)
        }
    }
}
