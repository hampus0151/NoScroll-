package com.noscroll.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noscroll.app.domain.SecondaryScreen
import com.noscroll.app.domain.UserSettings
import com.noscroll.app.presentation.NoScrollViewModel

private val Background = Color(0xFF0B0D12)
private val Surface = Color(0xFF141821)
private val SurfaceStrong = Color(0xFF1B202B)
private val TextPrimary = Color(0xFFF4F5F7)
private val TextMuted = Color(0xFF9DA5B4)
private val Accent = Color(0xFF31D6A6)

@Composable
fun SecondaryScreenView(screen: SecondaryScreen, settings: UserSettings, viewModel: NoScrollViewModel) {
    when (screen) {
        SecondaryScreen.About -> AboutScreen(viewModel)
        SecondaryScreen.Premium -> PremiumScreen(viewModel)
        SecondaryScreen.FocusMode -> FocusModeScreen(settings, viewModel)
        SecondaryScreen.Onboarding -> OnboardingScreen(viewModel)
        SecondaryScreen.None -> Unit
    }
}

@Composable
private fun SecondaryScaffold(title: String, viewModel: NoScrollViewModel, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::closeScreen) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Tillbaka", tint = TextPrimary)
            }
            Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        content()
    }
}

@Composable
private fun AboutScreen(viewModel: NoScrollViewModel) {
    SecondaryScaffold("Om NoScroll+", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Spacer(Modifier.height(18.dp))
                Icon(Icons.Outlined.Info, contentDescription = null, tint = Accent, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(14.dp))
                Text("Ett verktyg för mer medveten skärmtid.", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("NoScroll+ hjälper dig att pausa de kortvideo-flöden som lätt tar över, utan att du behöver lämna apparna du använder till vardags.", color = TextMuted, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                InfoCard("Version", "0.1.0 - UI foundation")
                InfoCard("Vision", "Gör plats för det som betyder något.")
                InfoCard("Integritet", "NoScroll+ ska samla in så lite data som möjligt och behandla fokusdata lokalt när det går.")
            }
        }
    }
}

@Composable
private fun PremiumScreen(viewModel: NoScrollViewModel) {
    SecondaryScaffold("NoScroll+ Premium", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Spacer(Modifier.height(18.dp))
                Icon(Icons.Outlined.Stars, contentDescription = null, tint = Accent, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Bygg en lugnare vardag på dina villkor.", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Premium låser upp verktyg för längre fokus och mer kontroll.", color = TextMuted, fontSize = 16.sp)
                Spacer(Modifier.height(20.dp))
                PremiumFeature("Schemaläggning", "Välj när olika regler ska vara aktiva.")
                PremiumFeature("Fokuslägen", "Spara olika kombinationer för arbete, kväll och återhämtning.")
                PremiumFeature("Obegränsade regler", "Skapa fler regler när ditt behov växer.")
                PremiumFeature("Flera appar", "Utöka skyddet till fler flöden och appar.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Accent), modifier = Modifier.fillMaxWidth()) {
                    Text("Premium kommer snart", color = Color(0xFF15121E))
                }
            }
        }
    }
}

@Composable
private fun FocusModeScreen(settings: UserSettings, viewModel: NoScrollViewModel) {
    SecondaryScaffold("Fokusläge", viewModel) {
        Column(Modifier.fillMaxSize().padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("En enkel paus från det oändliga flödet.", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Fokusläge är förberett i gränssnittet. Den faktiska plattformsintegrationen kommer efter att behörigheter och blockeringslogik är på plats.", color = TextMuted, fontSize = 16.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Accent)
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("Aktivera fokusläge", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Tillfälligt lugnare regler", color = TextMuted, fontSize = 13.sp)
                    }
                    Switch(checked = settings.focusMode, onCheckedChange = { value -> viewModel.updateSettings { it.copy(focusMode = value) } })
                }
            }
            InfoCard("Nästa steg", "Schemaläggning och plattformskoppling byggs i en senare milstolpe.")
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: NoScrollViewModel) {
    var page by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Outlined.Info,
            eyebrow = "VÄLKOMMEN TILL NOSCROLL+",
            title = "Gör plats för det som betyder något.",
            body = "NoScroll+ hjälper dig att pausa kortvideo-flöden innan de tar över din uppmärksamhet. Du väljer själv när skyddet ska vara aktivt."
        ),
        OnboardingPage(
            icon = Icons.Outlined.Shield,
            eyebrow = "SKYDDA DINA FLÖDEN",
            title = "Mindre scroll. Mer kontroll.",
            body = "På Android kan NoScroll+ blockera YouTube Shorts, Instagram Reels och Snapchat Spotlight utan att blockera hela apparna."
        ),
        OnboardingPage(
            icon = Icons.Outlined.Lock,
            eyebrow = "VARFÖR BEHÖRIGHETEN BEHÖVS",
            title = "Tillgänglighet gör skyddet möjligt.",
            body = "Androids Accessibility-behörighet behövs för att NoScroll+ ska kunna känna igen de valda flödena och stoppa dem. Vi ber inte om behörigheten ännu."
        ),
        OnboardingPage(
            icon = Icons.Outlined.CheckCircle,
            eyebrow = "DU ÄR REDO",
            title = "Börja använda din uppmärksamhet mer medvetet.",
            body = "Du kan ändra vilka flöden som skyddas när som helst från Home-skärmen."
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("NoScroll+", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        AnimatedContent(
            targetState = page,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * direction } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it * direction } + fadeOut())
            },
            label = "onboarding_page"
        ) { currentPage ->
            OnboardingPageContent(pages[currentPage])
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { index ->
                val selected = index == page
                val width by animateDpAsState(if (selected) 28.dp else 8.dp, label = "indicator_width")
                val color by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    label = "indicator_color"
                )
                    Box(Modifier.padding(horizontal = 4.dp).height(8.dp).width(width).clip(RoundedCornerShape(50)).background(color))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page > 0) {
                TextButton(onClick = { page -= 1 }, modifier = Modifier.weight(1f)) {
                    Text("Tillbaka", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Button(
                onClick = {
                    if (page < pages.lastIndex) {
                        page += 1
                    } else {
                        viewModel.updateSettings { it.copy(onboardingCompleted = true) }
                        viewModel.closeScreen()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1.4f)
            ) {
                Text(if (page == pages.lastIndex) "Get Started" else "Nästa", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val eyebrow: String,
    val title: String,
    val body: String
)

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 34.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(92.dp).clip(RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(page.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(30.dp))
        Text(page.eyebrow, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(page.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(page.body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 17.sp, lineHeight = 25.sp)
        if (page.eyebrow == "SKYDDA DINA FLÖDEN") {
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("YouTube Shorts", "Instagram Reels", "Snapchat Spotlight").forEach { app ->
                    Text(
                        app,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeature(title: String, detail: String) {
    InfoCard(title, detail, Icons.Outlined.CheckCircle)
}

@Composable
private fun InfoCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.Lock) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Accent)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(detail, color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}
