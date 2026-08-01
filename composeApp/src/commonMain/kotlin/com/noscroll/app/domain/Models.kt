package com.noscroll.app.domain

import kotlinx.coroutines.flow.StateFlow

/** Platforms that NoScroll will support through platform-specific integrations. */
enum class FocusPlatform(val label: String, val accent: Long) {
    YouTube("YouTube Shorts", 0xFFFF6B6B),
    Instagram("Instagram Reels", 0xFFE879F9),
    Snapchat("Snapchat", 0xFFFDE047),
    TikTok("TikTok", 0xFF67E8F9),
    Facebook("Facebook Reels", 0xFF60A5FA)
}

data class AppRule(
    val platform: FocusPlatform,
    val enabled: Boolean = true,
    val description: String
)

data class Statistics(
    val blockedToday: Int = 128,
    val blockedThisWeek: Int = 642,
    val blockedThisMonth: Int = 2_410,
    val minutesSaved: Int = 387
)

data class UserSettings(
    val darkMode: Boolean = true,
    val notifications: Boolean = true,
    val language: String = "Svenska",
    val startOnBoot: Boolean = false,
    val focusMode: Boolean = false,
    val premium: Boolean = false
)

data class NoScrollState(
    val selectedTab: AppTab = AppTab.Home,
    val appRules: List<AppRule> = FocusPlatform.entries.map {
        AppRule(it, description = when (it) {
            FocusPlatform.YouTube -> "Korta videor i Shorts-flödet"
            FocusPlatform.Instagram -> "Korta videor i Reels-flödet"
            FocusPlatform.Snapchat -> "Spotlight och Discover"
            FocusPlatform.TikTok -> "Det oändliga kortvideo-flödet"
            FocusPlatform.Facebook -> "Korta videor i Reels-flödet"
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

interface BlockingEngine {
    val isAvailable: Boolean
    suspend fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean)
}

interface NoScrollRepository {
    val state: StateFlow<NoScrollState>
    suspend fun setTab(tab: AppTab)
    suspend fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean)
    suspend fun updateSettings(update: (UserSettings) -> UserSettings)
}
