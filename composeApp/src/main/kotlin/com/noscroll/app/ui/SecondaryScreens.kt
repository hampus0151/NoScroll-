package com.noscroll.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
private val Accent = Color(0xFFA78BFA)

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
private fun OnboardingScreen(viewModel: NoScrollViewModel) {
    SecondaryScaffold("Välkommen till NoScroll+", viewModel) {
        Column(Modifier.fillMaxSize().padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Mer av det du väljer. Mindre av det som bara fortsätter.", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("NoScroll+ är byggt för att hjälpa dig pausa kortvideo-flöden utan att blockera hela appar.", color = TextMuted, fontSize = 16.sp)
            InfoCard("1. Välj flöden", "Aktivera de appar och flöden du vill få bättre kontroll över.")
            InfoCard("2. Sätt ett fokus", "Använd inställningar och framtida fokuslägen när du vill arbeta ostört.")
            InfoCard("3. Följ din utveckling", "Statistiken visar tid och avbrott när riktig mätning är inkopplad.")
            Spacer(Modifier.weight(1f))
            Button(onClick = viewModel::closeScreen, colors = ButtonDefaults.buttonColors(containerColor = Accent), modifier = Modifier.fillMaxWidth()) {
                Text("Kom igång", color = Color(0xFF15121E))
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
