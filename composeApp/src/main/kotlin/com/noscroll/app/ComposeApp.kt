package com.noscroll.app

import androidx.compose.runtime.Composable
import com.noscroll.app.ui.NoScrollPlusApp
import com.noscroll.app.data.InMemoryNoScrollRepository
import com.noscroll.app.domain.NoScrollRepository
import com.noscroll.app.presentation.NoScrollViewModel
import androidx.compose.runtime.remember

@Composable
fun ComposeApp(
    repository: NoScrollRepository? = null,
    onOpenAccessibilitySettings: () -> Unit = {},
    onLeaveShorts: () -> Unit = {}
) {
    val viewModel = remember(repository) {
        NoScrollViewModel(repository ?: InMemoryNoScrollRepository())
    }
    NoScrollPlusApp(viewModel, onOpenAccessibilitySettings, onLeaveShorts)
}
