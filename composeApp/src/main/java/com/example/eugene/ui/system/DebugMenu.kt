package com.example.eugene.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.FakeEugeneApiService
import com.example.data.remote.FakeNetworkConfig
import com.example.data.remote.FakeScenario
import com.example.data.remote.SessionType
import com.example.data.repository.NetworkMonitorImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.KeepTabRepository
import com.example.domain.repository.NetworkMonitor
import com.example.data.remote.EugeneApiService
import com.example.eugene.ui.components.EugeneColors
import com.example.eugene.ui.components.EugeneShapes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object DebugMenuState {
    var showDebugMenu by mutableStateOf(false)
}

@Composable
fun DebugMenu(
    modifier: Modifier = Modifier,
    config: FakeNetworkConfig = koinInject(),
    networkMonitor: NetworkMonitor = koinInject(),
    apiService: EugeneApiService = koinInject(),
    authRepository: AuthRepository = koinInject(),
    keepTabRepository: KeepTabRepository = koinInject()
) {
    if (!DebugMenuState.showDebugMenu) return

    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val surfaceColor = if (isDark) EugeneColors.DarkBackgroundSurface else EugeneColors.LightBackgroundSurface
    val textPrimaryColor = if (isDark) EugeneColors.DarkTextPrimary else EugeneColors.LightTextPrimary
    val textSecondaryColor = if (isDark) EugeneColors.DarkTextSecondary else EugeneColors.LightTextSecondary

    var latency by remember { mutableStateOf(config.latencyMs.toFloat()) }
    var failureRate by remember { mutableStateOf(config.failureRate * 100f) }
    var forcedScenario by remember { mutableStateOf(config.forcedScenario) }
    var isOfflineSimulated by remember { mutableStateOf(config.isOffline) }
    var isSessionExpiredSimulated by remember { mutableStateOf(config.isSessionExpired) }
    
    // Track local selected session type (Guest vs New vs Established)
    val fakeApi = apiService as? FakeEugeneApiService
    var sessionTypeState by remember {
        mutableStateOf(fakeApi?.currentSessionType ?: SessionType.ESTABLISHED)
    }

    AlertDialog(
        onDismissRequest = { DebugMenuState.showDebugMenu = false },
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Developer Debug Menu",
                    color = textPrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { DebugMenuState.showDebugMenu = false }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textPrimaryColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Section 1: Network Simulation
                Text(
                    text = "NETWORK SIMULATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Latency Slider
                Text(
                    text = "Latency: ${latency.toInt()} ms",
                    fontSize = 13.sp,
                    color = textPrimaryColor
                )
                Slider(
                    value = latency,
                    onValueChange = {
                        latency = it
                        config.latencyMs = it.toLong()
                    },
                    valueRange = 0f..5000f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                        activeTrackColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
                    )
                )

                // Failure Rate Slider
                Text(
                    text = "Failure Rate: ${failureRate.toInt()}%",
                    fontSize = 13.sp,
                    color = textPrimaryColor
                )
                Slider(
                    value = failureRate,
                    onValueChange = {
                        failureRate = it
                        config.failureRate = it / 100f
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                        activeTrackColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forced Scenario Selector
                Text(
                    text = "Forced Scenario:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimaryColor
                )
                Column(modifier = Modifier.selectableGroup()) {
                    val scenarios = listOf(null) + FakeScenario.values().toList()
                    scenarios.forEach { scenario ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .selectable(
                                    selected = forcedScenario == scenario,
                                    onClick = {
                                        forcedScenario = scenario
                                        config.forcedScenario = scenario
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = forcedScenario == scenario,
                                onClick = {
                                    forcedScenario = scenario
                                    config.forcedScenario = scenario
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scenario?.name ?: "None (SUCCESS)",
                                fontSize = 13.sp,
                                color = textPrimaryColor
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 2: Connectivity Simulation
                Text(
                    text = "CONNECTIVITY SIMULATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulate Offline",
                        fontSize = 13.sp,
                        color = textPrimaryColor
                    )
                    Switch(
                        checked = isOfflineSimulated,
                        onCheckedChange = { isOffline ->
                            isOfflineSimulated = isOffline
                            config.isOffline = isOffline
                            (networkMonitor as? NetworkMonitorImpl)?.setOnline(!isOffline)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                            checkedTrackColor = if (isDark) EugeneColors.DarkOrange.copy(alpha = 0.5f) else EugeneColors.LightOrange.copy(alpha = 0.5f)
                        )
                    )
                }

                Button(
                    onClick = {
                        isOfflineSimulated = false
                        config.isOffline = false
                        (networkMonitor as? NetworkMonitorImpl)?.setOnline(true)
                        coroutineScope.launch {
                            keepTabRepository.sync()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = EugeneShapes.pill
                ) {
                    Text("Simulate Reconnect & Sync", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 3: Seed Data & Testing Fixtures
                Text(
                    text = "SEED DATA CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Button(
                    onClick = {
                        fakeApi?.resetToSeedData()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
                        contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
                    ),
                    shape = EugeneShapes.pill
                ) {
                    Text("Reset to Seed Data")
                }

                Button(
                    onClick = {
                        fakeApi?.wipeAllData()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
                        contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
                    ),
                    shape = EugeneShapes.pill
                ) {
                    Text("Wipe All Data")
                }

                Button(
                    onClick = {
                        fakeApi?.seedEdgeCases()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
                        contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
                    ),
                    shape = EugeneShapes.pill
                ) {
                    Text("Seed Edge Cases")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 4: Session Simulation
                Text(
                    text = "SESSION CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(modifier = Modifier.selectableGroup()) {
                    val sessions = listOf(
                        SessionType.GUEST to "Guest (Anonymous)",
                        SessionType.NEW to "New Authenticated (0 resolved)",
                        SessionType.ESTABLISHED to "Established Authenticated (5+ resolved)"
                    )
                    sessions.forEach { (type, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .selectable(
                                    selected = sessionTypeState == type,
                                    onClick = {
                                        sessionTypeState = type
                                        fakeApi?.forceSession(type)
                                        coroutineScope.launch {
                                            if (type == SessionType.GUEST) {
                                                authRepository.signOut()
                                            } else {
                                                val email = if (type == SessionType.NEW) "new@example.com" else "established@example.com"
                                                authRepository.signInWithEmail(email, "password")
                                            }
                                        }
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sessionTypeState == type,
                                onClick = {
                                    sessionTypeState = type
                                    fakeApi?.forceSession(type)
                                    coroutineScope.launch {
                                        if (type == SessionType.GUEST) {
                                            authRepository.signOut()
                                        } else {
                                            val email = if (type == SessionType.NEW) "new@example.com" else "established@example.com"
                                            authRepository.signInWithEmail(email, "password")
                                        }
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                color = textPrimaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulate Session Expiry",
                        fontSize = 13.sp,
                        color = textPrimaryColor
                    )
                    Switch(
                        checked = isSessionExpiredSimulated,
                        onCheckedChange = { isExpired ->
                            isSessionExpiredSimulated = isExpired
                            config.isSessionExpired = isExpired
                            if (isExpired) {
                                SessionExpiryState.isExpired = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                            checkedTrackColor = if (isDark) EugeneColors.DarkOrange.copy(alpha = 0.5f) else EugeneColors.LightOrange.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { DebugMenuState.showDebugMenu = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
                    contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
                ),
                shape = EugeneShapes.pill
            ) {
                Text("Done")
            }
        },
        containerColor = surfaceColor,
        shape = EugeneShapes.card
    )
}
