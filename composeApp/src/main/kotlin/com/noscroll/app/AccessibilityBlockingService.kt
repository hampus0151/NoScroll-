package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.Rect
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.os.SystemClock
import android.os.Handler
import android.os.Looper
import com.noscroll.app.data.AndroidDataStoreRepository
import com.noscroll.app.domain.FocusPlatform
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class AccessibilityBlockingService : AccessibilityService() {
    @Inject
    lateinit var repository: AndroidDataStoreRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var windowManager: WindowManager
    private val entryPointBlockers = mutableMapOf<String, View>()
    private val entryPointBounds = mutableMapOf<String, Rect>()
    private var loggedMissingTarget = false
    private var lastYoutubeScanUptime = 0L
    private var automaticExitJob: Job? = null
    private var automaticExitScheduled = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        activeService = this
        serviceScope.launch {
            repository.setAccessibilityServiceEnabled(true)
        }
    }

    override fun onDestroy() {
        automaticExitJob?.cancel()
        serviceScope.cancel()
        removeEntryPointBlockers()
        activeService = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()
        when (packageName) {
            YOUTUBE_PACKAGE -> {
                val now = SystemClock.uptimeMillis()
                if (now - lastYoutubeScanUptime >= SCAN_INTERVAL_MS) {
                    lastYoutubeScanUptime = now
                    updateShortsEntryPointBlockers()
                }
                val enabled = isPlatformEnabled(FocusPlatform.YouTube)
                val shortsDetected = enabled && event.isYouTubeShortsEvent()
                publishDetection(
                    youtubeOpen = true,
                    shortsDetected = shortsDetected,
                    instagramOpen = false,
                    reelsDetected = false
                )
            }
            INSTAGRAM_PACKAGE -> {
                removeEntryPointBlockers()
                lastYoutubeScanUptime = 0L
                val enabled = isPlatformEnabled(FocusPlatform.Instagram)
                val reelsDetected = enabled && event.isInstagramReelsEvent()
                publishDetection(
                    youtubeOpen = false,
                    shortsDetected = false,
                    instagramOpen = true,
                    reelsDetected = reelsDetected
                )
            }
            else -> {
                removeEntryPointBlockers()
                lastYoutubeScanUptime = 0L
                publishDetection(youtubeOpen = false, shortsDetected = false, instagramOpen = false, reelsDetected = false)
            }
        }
    }

    override fun onInterrupt() = Unit

    private fun updateShortsEntryPointBlockers() {
        if (!repository.state.value.settings.blockingEnabled) {
            removeEntryPointBlockers()
            return
        }
        val root = rootInActiveWindow ?: run {
            removeEntryPointBlockers()
            return
        }
        val matches = linkedMapOf<String, Rect>()

        STABLE_ENTRY_POINT_IDS.forEach { id ->
            val nodes = root.findAccessibilityNodeInfosByViewId("$YOUTUBE_PACKAGE:id/$id")
            nodes.forEach { node -> matches[id] = nodeBounds(node) }
        }

        if (matches.isEmpty()) {
            if (!loggedMissingTarget) {
                Log.d(TAG, "Shorts tab entry point not found in current YouTube hierarchy")
                loggedMissingTarget = true
            }
            removeEntryPointBlockers()
            return
        }

        loggedMissingTarget = false
        entryPointBlockers.keys.filter { it !in matches }.toList().forEach(::removeEntryPointBlocker)
        matches.forEach { (key, bounds) ->
            if (!bounds.isEmpty) addOrUpdateEntryPointBlocker(key, bounds)
        }
    }

    private fun nodeBounds(node: AccessibilityNodeInfo): Rect = Rect().also(node::getBoundsInScreen)

    private fun isPlatformEnabled(platform: FocusPlatform): Boolean =
        repository.state.value.appRules.firstOrNull { it.platform == platform }?.enabled == true

    private fun publishDetection(
        youtubeOpen: Boolean,
        shortsDetected: Boolean,
        instagramOpen: Boolean,
        reelsDetected: Boolean
    ) {
        scheduleAutomaticExitIfNeeded(shortsDetected || reelsDetected)
        serviceScope.launch {
            repository.setYouTubeDetectionState(youtubeOpen, shortsDetected)
            repository.setInstagramDetectionState(instagramOpen, reelsDetected)
        }
    }

    private fun addOrUpdateEntryPointBlocker(key: String, bounds: Rect) {
        val blocker = entryPointBlockers.getOrPut(key) {
            View(this).apply {
                background = ColorDrawable(Color.BLACK)
                alpha = 0.92f
                isClickable = true
                setOnTouchListener { _, _ -> true }
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        }
        if (blocker.parent != null && entryPointBounds[key] == bounds) return
        val params = (blocker.layoutParams as? WindowManager.LayoutParams)
            ?: WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.TOP or Gravity.START
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
        params.x = bounds.left
        params.y = bounds.top
        params.width = bounds.width()
        params.height = bounds.height()
        if (blocker.parent == null) windowManager.addView(blocker, params) else windowManager.updateViewLayout(blocker, params)
        entryPointBounds[key] = Rect(bounds)
    }

    private fun removeEntryPointBlockers() {
        if (!::windowManager.isInitialized) return
        entryPointBlockers.keys.toList().forEach(::removeEntryPointBlocker)
    }

    private fun removeEntryPointBlocker(key: String) {
        entryPointBlockers.remove(key)?.let { blocker ->
            if (blocker.parent != null) windowManager.removeView(blocker)
        }
        entryPointBounds.remove(key)
    }

    private fun scheduleAutomaticExitIfNeeded(shortsDetected: Boolean) {
        if (!shortsDetected) {
            automaticExitJob?.cancel()
            automaticExitJob = null
            automaticExitScheduled = false
            return
        }

        val settings = repository.state.value.settings
        if (!settings.blockingEnabled || !settings.automaticBlocking || automaticExitScheduled) return

        automaticExitScheduled = true
        automaticExitJob = serviceScope.launch {
            delay(AUTOMATIC_EXIT_DELAY_MS)
            if (!repository.state.value.youtubeShortsDetected && !repository.state.value.instagramReelsDetected) {
                automaticExitScheduled = false
                return@launch
            }
            mainHandler.post {
                val exited = performGlobalAction(GLOBAL_ACTION_BACK)
                if (exited) {
                    serviceScope.launch { repository.recordShortsBlocked(automatic = true) }
                }
                automaticExitScheduled = false
            }
        }
    }

    fun performManualBack(): Boolean {
        val exited = performGlobalAction(GLOBAL_ACTION_BACK)
        if (exited) serviceScope.launch { repository.recordShortsBlocked(automatic = false) }
        return exited
    }

    private fun AccessibilityEvent.isYouTubeShortsEvent(): Boolean {
        val sourceId = source?.viewIdResourceName.orEmpty()
        if (sourceId.isNotEmpty()) {
            return SHORTS_VIEW_ID_MARKERS.any { marker -> sourceId.contains(marker) }
        }

        val eventText = text.joinToString(" ")
        val contentDescriptionText = contentDescription?.toString().orEmpty()
        return listOf(eventText, contentDescriptionText)
            .any { it.contains(SHORTS_LABEL, ignoreCase = true) }
    }

    private fun AccessibilityEvent.isInstagramReelsEvent(): Boolean {
        val sourceId = source?.viewIdResourceName.orEmpty()
        if (sourceId.isNotEmpty()) {
            return INSTAGRAM_REELS_VIEW_ID_MARKERS.any { marker -> sourceId.contains(marker, ignoreCase = true) }
        }

        val eventText = text.joinToString(" ")
        val contentDescriptionText = contentDescription?.toString().orEmpty()
        return listOf(eventText, contentDescriptionText)
            .any { it.contains(REELS_LABEL, ignoreCase = true) }
    }

    companion object {
        @Volatile
        private var activeService: AccessibilityBlockingService? = null

        fun performBack(): Boolean = activeService?.performManualBack() == true

        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val SHORTS_LABEL = "shorts"
        const val REELS_LABEL = "reels"
        const val TAG = "NoScrollAccessibility"
        const val SCAN_INTERVAL_MS = 120L
        const val AUTOMATIC_EXIT_DELAY_MS = 400L
        val STABLE_ENTRY_POINT_IDS = setOf("button_shorts_container")
        val SHORTS_VIEW_ID_MARKERS = setOf(
            "reel_player_",
            "reel_scrim_shorts_while_",
            "button_shorts_container"
        )
        val INSTAGRAM_REELS_VIEW_ID_MARKERS = setOf(
            "clips_tab",
            "clips_viewer",
            "reels_tab",
            "reels_viewer"
        )
    }
}
