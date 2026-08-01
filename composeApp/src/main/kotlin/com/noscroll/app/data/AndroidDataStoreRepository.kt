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
                        darkMode = preferences[Keys.DARK_MODE] ?: true,
                        notifications = preferences[Keys.NOTIFICATIONS] ?: true,
                        language = "Svenska",
                        startOnBoot = preferences[Keys.START_ON_BOOT] ?: false,
                        focusMode = preferences[Keys.FOCUS_MODE] ?: false,
                        premium = preferences[Keys.PREMIUM] ?: false,
                        onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false
                    ),
                    statistics = Statistics(
                        blockedToday = preferences[Keys.BLOCKED_TODAY] ?: 0,
                        blockedThisWeek = preferences[Keys.BLOCKED_WEEK] ?: 0,
                        blockedThisMonth = preferences[Keys.BLOCKED_MONTH] ?: 0,
                        minutesSaved = preferences[Keys.MINUTES_SAVED] ?: 0
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
            preferences[Keys.DARK_MODE] = next.darkMode
            preferences[Keys.NOTIFICATIONS] = next.notifications
            preferences[Keys.START_ON_BOOT] = next.startOnBoot
            preferences[Keys.FOCUS_MODE] = next.focusMode
            preferences[Keys.PREMIUM] = next.premium
            preferences[Keys.ONBOARDING_COMPLETED] = next.onboardingCompleted
        }
    }

    override suspend fun recordBlocked(platform: FocusPlatform, minutesSaved: Int) {
        val current = mutableState.value.statistics
        val next = current.copy(
            blockedToday = current.blockedToday + 1,
            blockedThisWeek = current.blockedThisWeek + 1,
            blockedThisMonth = current.blockedThisMonth + 1,
            minutesSaved = current.minutesSaved + minutesSaved
        )
        mutableState.value = mutableState.value.copy(statistics = next)
        appContext.noScrollDataStore.edit { preferences ->
            preferences[Keys.BLOCKED_TODAY] = next.blockedToday
            preferences[Keys.BLOCKED_WEEK] = next.blockedThisWeek
            preferences[Keys.BLOCKED_MONTH] = next.blockedThisMonth
            preferences[Keys.MINUTES_SAVED] = next.minutesSaved
        }
    }

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode")
        val PREMIUM = booleanPreferencesKey("premium")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BLOCKED_TODAY = intPreferencesKey("blocked_today")
        val BLOCKED_WEEK = intPreferencesKey("blocked_week")
        val BLOCKED_MONTH = intPreferencesKey("blocked_month")
        val MINUTES_SAVED = intPreferencesKey("minutes_saved")

        fun ruleEnabled(platform: FocusPlatform) = booleanPreferencesKey("rule_${platform.name.lowercase()}")
    }
}