package com.noscroll.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noscroll.app.domain.AppTab
import com.noscroll.app.domain.AppRule
import com.noscroll.app.domain.FocusPlatform
import com.noscroll.app.domain.Statistics
import com.noscroll.app.domain.UserSettings
import com.noscroll.app.domain.SecondaryScreen
import com.noscroll.app.presentation.NoScrollViewModel

private val Background = Color(0xFF0B0D12)
private val Surface = Color(0xFF141821)
private val SurfaceStrong = Color(0xFF1B202B)
private val TextPrimary = Color(0xFFF4F5F7)
private val TextMuted = Color(0xFF9DA5B4)
private val Accent = Color(0xFFA78BFA)

@Composable
fun NoScrollPlusApp(viewModel: NoScrollViewModel = remember { NoScrollViewModel() }) {
    val state by viewModel.state.collectAsState()

    MaterialTheme {
        if (state.secondaryScreen == SecondaryScreen.None) {
            Scaffold(
                containerColor = Background,
                bottomBar = {
                    NavigationBar(containerColor = Surface) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = state.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(tab.icon(), contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                when (state.selectedTab) {
                    AppTab.Home -> HomeScreen(state.appRules, viewModel, Modifier.padding(padding))
                    AppTab.Statistics -> StatisticsScreen(state.statistics, Modifier.padding(padding))
                    AppTab.Settings -> SettingsScreen(state.settings, viewModel, Modifier.padding(padding))
                }
            }
        } else {
            SecondaryScreenView(state.secondaryScreen, state.settings, viewModel)
        }
    }
}

@Composable
private fun HomeScreen(rules: List<AppRule>, viewModel: NoScrollViewModel, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("NoScroll+", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Gör plats för det som betyder något.", color = TextMuted, fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))
            PremiumBanner(viewModel)
            Spacer(Modifier.height(22.dp))
            SectionHeading("Skyddade flöden", "Välj vilka kortvideo-flöden du vill pausa.")
        }
        items(rules, key = { it.platform.name }) { rule ->
            AppRuleCard(rule) { enabled -> viewModel.setRuleEnabled(rule.platform, enabled) }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun PremiumBanner(viewModel: NoScrollViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF211B38)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Stars, null, tint = Accent, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("NoScroll+ Premium", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Schemaläggning och fokuslägen kommer snart.", color = TextMuted, fontSize = 13.sp)
            }
            TextButton(onClick = { viewModel.openScreen(SecondaryScreen.Premium) }) { Text("Utforska", color = Accent) }
        }
    }
}

@Composable
private fun AppRuleCard(rule: AppRule, onEnabledChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(rule.platform.accent)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Shield, null, tint = Color(0xFF111318))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(rule.platform.label, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(rule.description, color = TextMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Switch(checked = rule.enabled, onCheckedChange = onEnabledChange)
        }
    }
}

@Composable
private fun StatisticsScreen(statistics: Statistics, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("Statistik", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("En lugnare bild av din uppmärksamhet.", color = TextMuted)
            Spacer(Modifier.height(20.dp))
            StatCard("Blockerade idag", statistics.blockedToday.toString(), "+18 % mot förra veckan")
            StatCard("Blockerade den här veckan", statistics.blockedThisWeek.toString(), "Flest på kvällstid")
            StatCard("Blockerade den här månaden", statistics.blockedThisMonth.toString(), "Dummy-data i första versionen")
            StatCard("Sparad tid", "${statistics.minutesSaved} min", "Tid som kunde användas på annat")
            Spacer(Modifier.height(4.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceStrong), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Veckans mönster", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        listOf(42, 62, 34, 78, 58, 88, 64).forEachIndexed { index, height ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.width(22.dp).height(height.dp).clip(RoundedCornerShape(8.dp)).background(if (index == 5) Accent else Color(0xFF414858)))
                                Spacer(Modifier.height(6.dp))
                                Text(listOf("M", "T", "O", "T", "F", "L", "S")[index], color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.BarChart, null, tint = Accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = TextMuted, fontSize = 13.sp)
                Text(value, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(settings: UserSettings, viewModel: NoScrollViewModel, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("Inställningar", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Bestäm hur NoScroll+ ska fungera för dig.", color = TextMuted)
            Spacer(Modifier.height(20.dp))
            SettingToggle("Mörkt tema", "Ett lugnt gränssnitt för fokus.", Icons.Outlined.DarkMode, settings.darkMode) { value -> viewModel.updateSettings { it.copy(darkMode = value) } }
            SettingToggle("Notiser", "Påminnelser om dina fokuslägen.", Icons.Outlined.Notifications, settings.notifications) { value -> viewModel.updateSettings { it.copy(notifications = value) } }
            SettingToggle("Starta vid uppstart", "Aktivera NoScroll när telefonen startar.", Icons.Outlined.CheckCircle, settings.startOnBoot) { value -> viewModel.updateSettings { it.copy(startOnBoot = value) } }
            SettingToggle("Fokusläge", "Tillfälligt lugnare regler för ett valt pass.", Icons.Outlined.Shield, settings.focusMode) { value -> viewModel.updateSettings { it.copy(focusMode = value) } }
            Spacer(Modifier.height(18.dp))
            SectionHeading("Mer", "Funktioner som byggs ut stegvis.")
            SettingLink("Språk", settings.language)
            SettingLink("Premium", if (settings.premium) "Aktivt" else "Kommer snart") { viewModel.openScreen(SecondaryScreen.Premium) }
            SettingLink("Fokusläge", if (settings.focusMode) "Aktivt" else "Konfigurera") { viewModel.openScreen(SecondaryScreen.FocusMode) }
            SettingLink("Om NoScroll+", "Version 0.1.0") { viewModel.openScreen(SecondaryScreen.About) }
            SettingLink("Introduktion", "Visa igen") { viewModel.openScreen(SecondaryScreen.Onboarding) }
            SettingLink("Integritet", "Läs mer")
            SettingLink("Villkor", "Läs mer")
            SettingLink("Feedback", "Berätta vad du tycker")
        }
    }
}

@Composable
private fun SettingToggle(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold); Text(detail, color = TextMuted, fontSize = 12.sp) }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingLink(title: String, value: String, onClick: () -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onClick) { Text(value, color = TextMuted, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun SectionHeading(title: String, detail: String) {
    Text(title, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    Text(detail, color = TextMuted, fontSize = 13.sp)
}

private fun AppTab.icon() = when (this) {
    AppTab.Home -> Icons.Outlined.Home
    AppTab.Statistics -> Icons.Outlined.BarChart
    AppTab.Settings -> Icons.Outlined.Settings
}
