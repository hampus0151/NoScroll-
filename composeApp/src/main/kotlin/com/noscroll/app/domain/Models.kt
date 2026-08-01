package com.noscroll.app.domain

import kotlinx.coroutines.flow.StateFlow

/** Platforms that NoScroll+ will support through platform-specific integrations. */
enum class FocusPlatform(val label: String, val accent: Long) {
    YouTube("YouTube Shorts", 0xFFFF6B6B),
    Instagram("Instagram Reels", 0xFFE879F9),
    Snapchat("Snapchat Spotlight", 0xFFFDE047)
}

data class AppRule(
    val platform: FocusPlatform,
    val enabled: Boolean = true,
    val description: String
)

data class Statistics(
    val blockedToday: Int = 0,
    val blockedThisWeek: Int = 0,
    val blockedThisMonth: Int = 0,
    val totalBlocked: Int = 0,
    val manualExits: Int = 0,
    val automaticExits: Int = 0,
    val minutesSaved: Int = 0
)

data class UserSettings(
    val darkMode: Boolean = true,
    val notifications: Boolean = true,
    val language: String = "Svenska",
    val startOnBoot: Boolean = false,
    val focusMode: Boolean = false,
    val premium: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val blockingEnabled: Boolean = true,
    val automaticBlocking: Boolean = false,
    val showOverlay: Boolean = true,
    val debugMode: Boolean = false,
    val batteryOptimizationWarning: Boolean = true
)

data class NoScrollState(
    val selectedTab: AppTab = AppTab.Home,
    val secondaryScreen: SecondaryScreen = SecondaryScreen.None,
    val accessibilityServiceEnabled: Boolean = false,
    val youtubeOpen: Boolean = false,
    val youtubeShortsDetected: Boolean = false,
    val appRules: List<AppRule> = FocusPlatform.entries.map {
        AppRule(it, description = when (it) {
            FocusPlatform.YouTube -> "Korta videor i Shorts-flödet"
            FocusPlatform.Instagram -> "Korta videor i Reels-flödet"
            FocusPlatform.Snapchat -> "Spotlight-flödet"
        })
    },
    val statistics: Statistics = Statistics(),
    val settings: UserSettings = UserSettings()
)

enum class AppTab(val label: String) {
    Home("Hem"),
    Statistics("Statistik"),
    Settings("Inställningar")
}

enum class SecondaryScreen {
    None,
    About,
    Premium,
    FocusMode,
    Onboarding
}

interface BlockingEngine {
    val isAvailable: Boolean
    suspend fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean)
}

interface NoScrollRepository {
    val state: StateFlow<NoScrollState>
    suspend fun setTab(tab: AppTab)
    suspend fun setSecondaryScreen(screen: SecondaryScreen)
    suspend fun setAccessibilityServiceEnabled(enabled: Boolean)
    suspend fun setYouTubeDetectionState(youtubeOpen: Boolean, shortsDetected: Boolean)
    suspend fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean)
    suspend fun updateSettings(update: (UserSettings) -> UserSettings)
    suspend fun recordShortsBlocked(automatic: Boolean, minutesSaved: Int = 1)
    suspend fun recordBlocked(platform: FocusPlatform, minutesSaved: Int)
}
