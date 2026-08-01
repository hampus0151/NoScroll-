package com.noscroll.app.presentation

import com.noscroll.app.data.InMemoryNoScrollRepository
import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.NoScrollRepository
import com.noscroll.app.domain.NoScrollState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Shared state holder with a ViewModel-shaped API for both Android and iOS. */
class NoScrollViewModel(
    private val repository: NoScrollRepository = InMemoryNoScrollRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val state: StateFlow<NoScrollState> = repository.state

    fun selectTab(tab: AppTab) = scope.launch { repository.setTab(tab) }

    fun setRuleEnabled(platform: FocusPlatform, enabled: Boolean) = scope.launch {
        repository.setRuleEnabled(platform, enabled)
    }

    fun updateSettings(update: (com.noscroll.app.domain.UserSettings) -> com.noscroll.app.domain.UserSettings) = scope.launch {
        repository.updateSettings(update)
    }
}
