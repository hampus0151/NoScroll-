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
import com.noscroll.app.data.AndroidDataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccessibilityBlockingService : AccessibilityService() {
    @Inject
    lateinit var repository: AndroidDataStoreRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var windowManager: WindowManager
    private val entryPointBlockers = mutableMapOf<String, View>()
    private var loggedMissingTarget = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        activeService = this
        serviceScope.launch {
            repository.setAccessibilityServiceEnabled(true)
        }
    }

    override fun onDestroy() {
        removeEntryPointBlockers()
        activeService = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()
        if (packageName != YOUTUBE_PACKAGE) {
            removeEntryPointBlockers()
            return
        }

        updateShortsEntryPointBlockers()
        val youtubeOpen = packageName == YOUTUBE_PACKAGE
        val shortsDetected = youtubeOpen && event.isYouTubeShortsEvent()
        serviceScope.launch {
            repository.setYouTubeDetectionState(youtubeOpen, shortsDetected)
        }
    }

    override fun onInterrupt() = Unit

    private fun updateShortsEntryPointBlockers() {
        val root = rootInActiveWindow ?: run {
            removeEntryPointBlockers()
            return
        }
        val matches = linkedMapOf<String, Rect>()

        STABLE_ENTRY_POINT_IDS.forEach { id ->
            root.findAccessibilityNodeInfosByViewId("$YOUTUBE_PACKAGE:id/$id")
                .forEach { node -> matches[id] = nodeBounds(node) }
        }

        if (matches.isEmpty()) {
            findFallbackEntryPoints(root, matches)
        }

        if (matches.isEmpty()) {
            if (!loggedMissingTarget) {
                Log.d(TAG, "No Shorts entry point found in current YouTube hierarchy")
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

    private fun findFallbackEntryPoints(root: AccessibilityNodeInfo, matches: MutableMap<String, Rect>) {
        val pending = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        pending.add(root to 0)
        var visited = 0
        while (pending.isNotEmpty() && visited < MAX_FALLBACK_NODES) {
            val (node, depth) = pending.removeFirst()
            visited++
            val text = node.text?.toString().orEmpty()
            val description = node.contentDescription?.toString().orEmpty()
            if (node.isVisibleToUser && (text + " " + description).contains(SHORTS_LABEL, ignoreCase = true)) {
                matches["fallback_${matches.size}"] = nodeBounds(node)
            }
            if (depth < MAX_FALLBACK_DEPTH) {
                for (index in 0 until node.childCount) {
                    node.getChild(index)?.let { pending.add(it to depth + 1) }
                }
            }
        }
    }

    private fun nodeBounds(node: AccessibilityNodeInfo): Rect = Rect().also(node::getBoundsInScreen)

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
    }

    private fun removeEntryPointBlockers() {
        entryPointBlockers.keys.toList().forEach(::removeEntryPointBlocker)
    }

    private fun removeEntryPointBlocker(key: String) {
        entryPointBlockers.remove(key)?.let { blocker ->
            if (blocker.parent != null) windowManager.removeView(blocker)
        }
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

    companion object {
        @Volatile
        private var activeService: AccessibilityBlockingService? = null

        fun performBack(): Boolean = activeService?.performGlobalAction(GLOBAL_ACTION_BACK) == true

        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        const val SHORTS_LABEL = "shorts"
        const val TAG = "NoScrollAccessibility"
        const val MAX_FALLBACK_NODES = 160
        const val MAX_FALLBACK_DEPTH = 12
        val STABLE_ENTRY_POINT_IDS = setOf("button_shorts_container")
        val SHORTS_VIEW_ID_MARKERS = setOf(
            "reel_player_",
            "reel_scrim_shorts_while_",
            "button_shorts_container"
        )
    }
}
