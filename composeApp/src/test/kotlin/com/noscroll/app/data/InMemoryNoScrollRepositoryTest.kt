package com.noscroll.app.data

import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.SecondaryScreen
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryNoScrollRepositoryTest {
    @Test
    fun ruleChangesAndSecondaryNavigationAreReflectedInState() = runTest {
        val repository = InMemoryNoScrollRepository()

        repository.setTab(AppTab.Settings)
        repository.setSecondaryScreen(SecondaryScreen.About)
        repository.setRuleEnabled(FocusPlatform.TikTok, false)

        assertEquals(AppTab.Settings, repository.state.value.selectedTab)
        assertEquals(SecondaryScreen.About, repository.state.value.secondaryScreen)
        assertFalse(repository.state.value.appRules.first { it.platform == FocusPlatform.TikTok }.enabled)
    }

    @Test
    fun statisticsCanBeRecorded() = runTest {
        val repository = InMemoryNoScrollRepository()

        repository.recordBlocked(FocusPlatform.YouTube, minutesSaved = 3)

        assertTrue(repository.state.value.statistics.blockedToday > 128)
        assertEquals(390, repository.state.value.statistics.minutesSaved)
    }
}