package com.example.eugene.ui.system

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.NetworkMonitor
import com.example.eugene.ui.components.EugeneAnimationTokens
import com.example.eugene.ui.components.EugeneColors
import org.koin.compose.koinInject

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    networkMonitor: NetworkMonitor = koinInject()
) {
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) EugeneColors.DarkBackgroundSurfaceSunken else EugeneColors.LightBackgroundSurfaceSunken
    val textPrimaryColor = if (isDark) EugeneColors.DarkTextPrimary else EugeneColors.LightTextPrimary
    val textSecondaryColor = if (isDark) EugeneColors.DarkTextSecondary else EugeneColors.LightTextSecondary

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            // Rotates ±15°, 3 cycles, Fast per cycle (150ms per cycle)
            for (i in 0 until 3) {
                rotation.animateTo(-15f, animationSpec = tween(37, easing = LinearEasing))
                rotation.animateTo(15f, animationSpec = tween(75, easing = LinearEasing))
                rotation.animateTo(0f, animationSpec = tween(38, easing = LinearEasing))
            }
        } else {
            rotation.animateTo(0f)
        }
    }

    AnimatedVisibility(
        visible = !isOnline,
        enter = slideInVertically(
            initialOffsetY = { -it }, // slides down from below top bar
            animationSpec = EugeneAnimationTokens.springDefault()
        ) + expandVertically(
            animationSpec = EugeneAnimationTokens.springDefault()
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }, // slides up
            animationSpec = tween(
                durationMillis = EugeneAnimationTokens.Standard,
                easing = EugeneAnimationTokens.AccelerateEasing
            )
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = EugeneAnimationTokens.Standard,
                easing = EugeneAnimationTokens.AccelerateEasing
            )
        ) + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "No Connection",
                tint = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation.value)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "You appear to be offline",
                    color = textPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Some features may be unavailable.",
                    color = textSecondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
