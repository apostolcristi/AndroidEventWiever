package com.example.androideventwiever

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TouchService : Service() {

    private lateinit var touchOverlayView: View
    private lateinit var windowManager: WindowManager
    private val eventHistory = mutableListOf<String>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            handleEvent("Network connectivity changed. Connected.")
        }

        override fun onLost(network: Network) {
            handleEvent("Network connectivity changed. Disconnected.")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        touchOverlayView = View(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            0
        )

        windowManager.addView(touchOverlayView, params)

        touchOverlayView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Handle the touch event, e.g., log it
                handleEvent("Screen Touched")
            }
            true
        }

        // Register the network callback
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (touchOverlayView.isAttachedToWindow) {
            windowManager.removeView(touchOverlayView)
        }

        // Unregister the network callback when the service is destroyed
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

        // Save logs to file when the service is destroyed
        saveLogsToFile()
    }

    private fun handleEvent(event: String) {
        val timestamp = getCurrentUtcTimestamp()
        val message = "$event at $timestamp"
        eventHistory.add(message)
    }

    private fun getCurrentUtcTimestamp(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(currentTimeMillis)
        return sdf.format(date)
    }

    private fun saveLogsToFile() {
        try {
            val fileName = "event_logs_from_service.txt"
            val file = getFileInDownloads(fileName)

            val writer = FileWriter(file, true)

            for (eventEntry in eventHistory) {
                writer.append("$eventEntry\n")
            }

            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileInDownloads(fileName: String): File {
        // Use the Downloads directory
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, fileName)
    }
}
