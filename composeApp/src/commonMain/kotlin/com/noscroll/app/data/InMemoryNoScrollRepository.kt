package com.noscroll.app.data

import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.NoScrollRepository
import com.noscroll.app.domain.NoScrollState
import com.noscroll.app.domain.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Temporary repository. Replace persistence with DataStore-backed implementations per platform. */
class InMemoryNoScrollRepository : NoScrollRepository {
    private val mutableState = MutableStateFlow(NoScrollState())
    override val state: StateFlow<NoScrollState> = mutableState.asStateFlow()

    override suspend fun setTab(tab: AppTab) {
        mutableState.value = mutableState.value.copy(selectedTab = tab)
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
}
