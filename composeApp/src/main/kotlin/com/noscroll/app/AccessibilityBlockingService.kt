package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AccessibilityBlockingService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit
}
