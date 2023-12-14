package com.example.androideventwiever

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class TouchAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val timestamp = System.currentTimeMillis()
            Toast.makeText(
                this,
                "Screen Touched at $timestamp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onInterrupt() {
        // Not used atm
    }
}
