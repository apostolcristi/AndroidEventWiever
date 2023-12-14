package com.example.androideventwiever

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private val eventHistory = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            handleEvent("Network connectivity changed. Connected.")
        }

        override fun onLost(network: Network) {
            handleEvent("Network connectivity changed. Disconnected.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request necessary permissions
        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        // Register the network callback
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Log button clicks
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            handleEvent("Button Clicked")
        }

        // Log screen touches
        val rootView = window.decorView.rootView
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleEvent("Screen Touched")
            }
            true
        }

        // Schedule the task to save logs every 1 minute
        handler.postDelayed(saveLogsTask, 60 * 1000) // 60 seconds * 1000 milliseconds
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the network callback when the activity is destroyed
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

        // Remove any pending callbacks to prevent leaks
        handler.removeCallbacks(saveLogsTask)
    }

    private fun handleEvent(event: String) {
        val timestamp = getCurrentUtcTimestamp()
        val message = "$event at $timestamp"
        eventHistory.add(message)
        Log.d("EventLogs", message)
    }

    private fun getCurrentUtcTimestamp(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(currentTimeMillis)
        return sdf.format(date)
    }

    private val saveLogsTask: Runnable = object : Runnable {
        override fun run() {
            Log.d("EventLogs", "Before writing to file")

            // Save the events to a text file
            saveLogsToFile()

            Log.d("EventLogs", "After writing to file")

            // Reschedule the task for the next minute
            handler.postDelayed(this, 60 * 1000)
        }
    }

    private fun saveLogsToFile() {
        try {
            val fileName = "event_logs.txt"

            // Use the Downloads directory
            val file = getFile(fileName)

            Log.d("EventLogs", "File path: ${file.absolutePath}")

            val writer = FileWriter(file, true)

            for (eventEntry in eventHistory) {
                writer.append("$eventEntry\n")
            }

            writer.flush() // Flush to ensure content is written immediately

            writer.close()

            Log.d("EventLogs", "Logs saved to: ${file.absolutePath}")

            // Clear the history after saving
            eventHistory.clear()
        } catch (e: Exception) {
            Log.e("EventLogs", "Error saving logs", e)
        }
    }

    private fun getFile(fileName: String): File {
        // Use the Downloads directory
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, fileName)
    }
}
