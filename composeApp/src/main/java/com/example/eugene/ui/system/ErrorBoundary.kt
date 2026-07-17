package com.example.eugene.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eugene.ui.components.EugeneColors
import com.example.eugene.ui.components.EugeneShapes

@Composable
fun FullscreenErrorState(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ErrorOutline
) {
    val isDark = isSystemInDarkTheme()
    val textPrimaryColor = if (isDark) EugeneColors.DarkTextPrimary else EugeneColors.LightTextPrimary
    val textSecondaryColor = if (isDark) EugeneColors.DarkTextSecondary else EugeneColors.LightTextSecondary
    val surfaceColor = if (isDark) EugeneColors.DarkBackgroundSurface else EugeneColors.LightBackgroundSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) EugeneColors.DarkBackgroundBase else EugeneColors.LightBackgroundBase)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (isDark) EugeneColors.DarkBackgroundSurfaceSunken else EugeneColors.LightBackgroundSurfaceSunken,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            color = textPrimaryColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            color = textSecondaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
                contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
            ),
            shape = EugeneShapes.pill,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = actionLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DegradedStateBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textPrimaryColor = if (isDark) EugeneColors.DarkTextPrimary else EugeneColors.LightTextPrimary
    val surfaceColor = if (isDark) EugeneColors.DarkBackgroundSurfaceSunken else EugeneColors.LightBackgroundSurfaceSunken

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = EugeneShapes.card,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = if (isDark) EugeneColors.DarkAmber else EugeneColors.LightAmber,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = textPrimaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textPrimaryColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun InlineValidationErrorText(
    message: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Text(
        text = message,
        color = if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
fun RetryButtonWithCeiling(
    retryCount: Int,
    onRetry: () -> Unit,
    onVisitStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isCeilingHit = retryCount >= 3

    Button(
        onClick = {
            if (isCeilingHit) {
                onVisitStatus()
            } else {
                onRetry()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDark) EugeneColors.DarkSurfaceInverse else EugeneColors.LightSurfaceInverse,
            contentColor = if (isDark) EugeneColors.DarkTextOnInverse else EugeneColors.LightTextOnInverse
        ),
        shape = EugeneShapes.pill,
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = if (isCeilingHit) "Still having trouble? Visit status.eugene.app" else "Retry",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
