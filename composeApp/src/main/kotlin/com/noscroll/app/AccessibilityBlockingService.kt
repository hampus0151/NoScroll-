package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceScope.launch {
            repository.setAccessibilityServiceEnabled(true)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()
        val youtubeOpen = packageName == YOUTUBE_PACKAGE
        val shortsDetected = youtubeOpen && event.isYouTubeShortsEvent()
        serviceScope.launch {
            repository.setYouTubeDetectionState(youtubeOpen, shortsDetected)
        }
    }

    override fun onInterrupt() = Unit

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

    private companion object {
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        const val SHORTS_LABEL = "shorts"
        val SHORTS_VIEW_ID_MARKERS = setOf(
            "reel_player_",
            "reel_scrim_shorts_while_",
            "button_shorts_container"
        )
    }
}
