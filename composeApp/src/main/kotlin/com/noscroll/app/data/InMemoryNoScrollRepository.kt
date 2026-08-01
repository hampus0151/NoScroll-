package com.noscroll.app.data

import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.NoScrollRepository
import com.noscroll.app.domain.NoScrollState
import com.noscroll.app.domain.SecondaryScreen
import com.noscroll.app.domain.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Temporary NoScroll+ repository. Replace persistence with DataStore-backed implementations per platform. */
class InMemoryNoScrollRepository : NoScrollRepository {
    private val mutableState = MutableStateFlow(NoScrollState())
    override val state: StateFlow<NoScrollState> = mutableState.asStateFlow()

    override suspend fun setTab(tab: AppTab) {
        mutableState.value = mutableState.value.copy(selectedTab = tab)
    }

    override suspend fun setSecondaryScreen(screen: SecondaryScreen) {
        mutableState.value = mutableState.value.copy(secondaryScreen = screen)
    }

    override suspend fun setAccessibilityServiceEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(accessibilityServiceEnabled = enabled)
    }

    override suspend fun setYouTubeDetectionState(youtubeOpen: Boolean, shortsDetected: Boolean) {
        mutableState.value = mutableState.value.copy(
            youtubeOpen = youtubeOpen,
            youtubeShortsDetected = shortsDetected
        )
    }

    override suspend fun setInstagramDetectionState(instagramOpen: Boolean, reelsDetected: Boolean) {
        mutableState.value = mutableState.value.copy(
            instagramOpen = instagramOpen,
            instagramReelsDetected = reelsDetected
        )
    }

    override suspend fun setFacebookDetectionState(facebookOpen: Boolean, reelsDetected: Boolean) {
        mutableState.value = mutableState.value.copy(
            facebookOpen = facebookOpen,
            facebookReelsDetected = reelsDetected
        )
    }

    override suspend fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean) {
        mutableState.value = mutableState.value.copy(
            appRules = mutableState.value.appRules.map { rule ->
                if (rule.platform == platform) rule.copy(enabled = enabled) else rule
            }
        )
    }

    override suspend fun updateSettings(update: (UserSettings) -> UserSettings) {
        mutableState.value = mutableState.value.copy(settings = update(mutableState.value.settings))
    }

    override suspend fun recordShortsBlocked(automatic: Boolean, minutesSaved: Int) {
        val current = mutableState.value.statistics
        mutableState.value = mutableState.value.copy(
            statistics = current.copy(
                blockedToday = current.blockedToday + 1,
                blockedThisWeek = current.blockedThisWeek + 1,
                blockedThisMonth = current.blockedThisMonth + 1,
                totalBlocked = current.totalBlocked + 1,
                automaticExits = current.automaticExits + if (automatic) 1 else 0,
                manualExits = current.manualExits + if (automatic) 0 else 1,
                minutesSaved = current.minutesSaved + minutesSaved
            )
        )
    }

    override suspend fun recordBlocked(platform: FocusPlatform, minutesSaved: Int) {
        mutableState.value = mutableState.value.copy(
            statistics = mutableState.value.statistics.copy(
                blockedToday = mutableState.value.statistics.blockedToday + 1,
                blockedThisWeek = mutableState.value.statistics.blockedThisWeek + 1,
                blockedThisMonth = mutableState.value.statistics.blockedThisMonth + 1,
                minutesSaved = mutableState.value.statistics.minutesSaved + minutesSaved
            )
        )
    }
}
