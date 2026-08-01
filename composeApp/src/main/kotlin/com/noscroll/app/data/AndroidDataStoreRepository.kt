package com.noscroll.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.NoScrollRepository
import com.noscroll.app.domain.NoScrollState
import com.noscroll.app.domain.SecondaryScreen
import com.noscroll.app.domain.Statistics
import com.noscroll.app.domain.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.noScrollDataStore by preferencesDataStore(name = "noscroll_plus_preferences")

class AndroidDataStoreRepository(context: Context) : NoScrollRepository {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(NoScrollState())
    override val state: StateFlow<NoScrollState> = mutableState.asStateFlow()

    init {
        scope.launch {
            appContext.noScrollDataStore.data.collect { preferences ->
                mutableState.value = mutableState.value.copy(
                    settings = UserSettings(
                        onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
                        blockingEnabled = preferences[Keys.BLOCKING_ENABLED] ?: true,
                        automaticBlocking = preferences[Keys.AUTOMATIC_BLOCKING] ?: false
                    ),
                    statistics = Statistics(
                        blockedToday = preferences[Keys.BLOCKED_TODAY] ?: 0,
                        totalBlocked = preferences[Keys.TOTAL_BLOCKED] ?: 0
                    ),
                    appRules = mutableState.value.appRules.map { rule ->
                        rule.copy(enabled = preferences[Keys.ruleEnabled(rule.platform)] ?: true)
                    }
                )
            }
        }
    }

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
        appContext.noScrollDataStore.edit { preferences ->
            preferences[Keys.ruleEnabled(platform)] = enabled
        }
    }

    override suspend fun updateSettings(update: (UserSettings) -> UserSettings) {
        val next = update(mutableState.value.settings)
        mutableState.value = mutableState.value.copy(settings = next)
        appContext.noScrollDataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = next.onboardingCompleted
            preferences[Keys.BLOCKING_ENABLED] = next.blockingEnabled
            preferences[Keys.AUTOMATIC_BLOCKING] = next.automaticBlocking
        }
    }

    override suspend fun recordShortsBlocked(automatic: Boolean, minutesSaved: Int) {
        val current = mutableState.value.statistics
        val next = current.copy(
            blockedToday = current.blockedToday + 1,
            totalBlocked = current.totalBlocked + 1
        )
        mutableState.value = mutableState.value.copy(statistics = next)
        appContext.noScrollDataStore.edit { preferences ->
            preferences[Keys.BLOCKED_TODAY] = next.blockedToday
            preferences[Keys.TOTAL_BLOCKED] = next.totalBlocked
        }
    }

    override suspend fun recordBlocked(platform: FocusPlatform, minutesSaved: Int) {
        val current = mutableState.value.statistics
        val next = current.copy(
            blockedToday = current.blockedToday + 1,
            totalBlocked = current.totalBlocked + 1
        )
        mutableState.value = mutableState.value.copy(statistics = next)
        appContext.noScrollDataStore.edit { preferences ->
            preferences[Keys.BLOCKED_TODAY] = next.blockedToday
            preferences[Keys.TOTAL_BLOCKED] = next.totalBlocked
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val AUTOMATIC_BLOCKING = booleanPreferencesKey("automatic_blocking")
        val BLOCKED_TODAY = intPreferencesKey("blocked_today")
        val TOTAL_BLOCKED = intPreferencesKey("total_blocked")

        fun ruleEnabled(platform: FocusPlatform) = booleanPreferencesKey("rule_${platform.name.lowercase()}")
    }
}