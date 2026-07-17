package com.example.eugene.ui.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.NetworkMonitor
import com.example.eugene.ui.components.EugeneColors
import org.koin.compose.koinInject

@Composable
fun isOnlineState(networkMonitor: NetworkMonitor = koinInject()): Boolean {
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    return isOnline
}

@Composable
fun OfflineRequiresConnectionText(
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Text(
        text = "Requires an internet connection",
        color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(vertical = 4.dp)
    )
}
