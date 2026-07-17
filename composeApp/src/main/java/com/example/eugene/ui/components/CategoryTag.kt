package com.example.eugene.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.PredictionCategory

@Composable
fun CategoryTag(
    category: PredictionCategory,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val categoryColor = EugeneColors.getCategoryColor(category, isDark)
    val backgroundColor = categoryColor.copy(alpha = 0.12f)
    val sweepProgress = remember { Animatable(0f) }

    LaunchedEffect(category) {
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = EugeneAnimationTokens.Standard,
                easing = EugeneAnimationTokens.DecelerateEasing
            )
        )
    }

    Box(
        modifier = modifier
            .testTag("category_tag_${category.name.lowercase()}")
            .clip(EugeneShapes.pill)
            .drawBehind {
                val width = size.width * sweepProgress.value
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset.Zero,
                    size = Size(width, size.height)
                )
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )
        )
    }
}
