package com.example.eugene.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.PredictionOption
import kotlinx.coroutines.delay

@Composable
fun OutcomeOptionBar(
    option: PredictionOption,
    isLeading: Boolean,
    isTrailing: Boolean,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Lift scale animation: lift slightly on press (scale 1.02)
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 1.02f else 1.0f,
        animationSpec = EugeneAnimationTokens.springSettle(),
        label = "outcome_press_scale"
    )

    // Lock icon delay: fade in lock icon 300ms after selection
    var showLock by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            delay(300)
            showLock = true
        } else {
            showLock = false
        }
    }

    // Determine bar color based on status guidelines:
    // Sage (leading), Orange (trailing), Neutral/Accent for others
    val barColor = when {
        isLeading -> if (isDark) EugeneColors.DarkSage else EugeneColors.LightSage
        isTrailing -> if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
        else -> EugeneColors.getAccentColor(option.accent, isDark)
    }

    // Animate progress percentage from 0 to actual value
    var progressTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(option.percentage) {
        progressTarget = option.percentage / 100f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(800, easing = EugeneAnimationTokens.StandardEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .testTag("outcome_bar_${option.id}")
            .fillMaxWidth()
            .height(44.dp)
            .scale(scaleFactor)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                enabled = onSelect != null && !isSelected
            ) {
                onSelect?.invoke()
            }
            .drawBehind {
                // Draw custom progressive overlay
                drawRect(
                    color = barColor.copy(alpha = if (isSelected) 0.25f else 0.12f),
                    size = size.copy(width = size.width * animatedProgress)
                )
                // Thin left colored vertical indicator
                drawRect(
                    color = barColor,
                    size = size.copy(width = 4.dp.toPx())
                )
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (option.imageUrl != null) {
                    AsyncImage(
                        model = option.imageUrl,
                        contentDescription = option.text,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    AnimatedVisibility(
                        visible = showLock,
                        enter = fadeIn(animationSpec = tween(EugeneAnimationTokens.Fast)),
                        exit = fadeOut(animationSpec = tween(EugeneAnimationTokens.Fast))
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Choice",
                            tint = barColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(
                text = "${option.percentage}%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            )
        }
    }
}

@Composable
fun OutcomeBarsList(
    options: List<PredictionOption>,
    modifier: Modifier = Modifier,
    selectedOptionId: String? = null,
    onOptionSelect: ((String) -> Unit)? = null
) {
    // Sort options to find leading and trailing for color scheme guidelines
    val sortedOptions = options.sortedByDescending { it.percentage }
    val leadingOptionId = sortedOptions.firstOrNull()?.id
    val trailingOptionId = if (sortedOptions.size > 1) sortedOptions.lastOrNull()?.id else null

    Column(
        modifier = modifier.testTag("outcome_bars_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            OutcomeOptionBar(
                option = option,
                isLeading = option.id == leadingOptionId,
                isTrailing = option.id == trailingOptionId,
                isSelected = option.id == selectedOptionId,
                onSelect = onOptionSelect?.let { { it.invoke(option.id) } }
            )
        }
    }
}
