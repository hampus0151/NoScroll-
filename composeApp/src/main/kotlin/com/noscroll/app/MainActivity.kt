package com.noscroll.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.noscroll.app.data.AndroidDataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: AndroidDataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeApp(
                repository = repository,
                onOpenAccessibilitySettings = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onLeaveShorts = { AccessibilityBlockingService.performBack() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            repository.setAccessibilityServiceEnabled(isAccessibilityServiceEnabled())
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val serviceName = ComponentName(this, AccessibilityBlockingService::class.java).flattenToString()
        return enabledServices.split(':').any { it.equals(serviceName, ignoreCase = true) }
    }
}
