package com.example.eugene.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Prediction
import com.example.domain.model.PredictionCategory
import com.example.domain.model.PredictionStatus
import com.example.domain.model.PredictorSummary
import kotlin.random.Random

@Composable
fun PredictionCard(
    prediction: Prediction,
    notablePredictors: List<PredictorSummary>,
    onClick: () -> Unit,
    onAvatarClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedOptionId: String? = null,
    onOptionSelect: ((String) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier
            .testTag("prediction_card_${prediction.id}")
            .fillMaxWidth()
            .clickable { onClick() },
        shape = EugeneShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 40x40dp Thumbnail (circular as radius.card is 20dp on 40dp box)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(EugeneShapes.card)
                        .background(
                            EugeneColors
                                .getCategoryColor(prediction.category, isDark)
                                .copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (prediction.heroImageUrl != null) {
                        AsyncImage(
                            model = prediction.heroImageUrl,
                            contentDescription = prediction.title,
                            modifier = Modifier.clip(EugeneShapes.card),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Category Flooded fallback icon
                        val categoryIcon = getCategoryIcon(prediction.category)
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = prediction.category.name,
                            tint = EugeneColors.getCategoryColor(prediction.category, isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    CategoryTag(category = prediction.category)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Closes ${formatDate(prediction.closesAt.toEpochMilliseconds())}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                StatusBadge(status = prediction.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question / Title Text
            Text(
                text = prediction.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Outcome Option Bars list
            OutcomeBarsList(
                options = prediction.options,
                selectedOptionId = selectedOptionId,
                onOptionSelect = onOptionSelect
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Row (Notable Predictors only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "NOTABLE PREDICTORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AvatarCluster(
                        predictors = notablePredictors,
                        totalCount = notablePredictors.size,
                        onAvatarClick = onAvatarClick
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: PredictionStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val badgeColor = when (status) {
        PredictionStatus.LIVE, PredictionStatus.APPROVED -> if (isDark) EugeneColors.DarkSage else EugeneColors.LightSage
        PredictionStatus.RESOLVED -> if (isDark) Color(0xFF7EB0E6) else Color(0xFF2D70B3)
        PredictionStatus.PENDING, PredictionStatus.RESOLVING -> if (isDark) EugeneColors.DarkAmber else EugeneColors.LightAmber
        PredictionStatus.VOIDED, PredictionStatus.REJECTED -> if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
    }

    Box(
        modifier = modifier
            .testTag("status_badge_${status.name.lowercase()}")
            .clip(EugeneShapes.pill)
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun Sparkline(
    predictionId: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Generate stable points based on the predictionId hash
    val points = remember(predictionId) {
        val seed = predictionId.hashCode().toLong()
        val random = Random(seed)
        List(7) { random.nextFloat() * 0.8f + 0.1f } // Keep within [0.1, 0.9] range
    }

    val trimProgress = remember { Animatable(0f) }
    LaunchedEffect(predictionId) {
        trimProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = EugeneAnimationTokens.Emphasis,
                easing = EugeneAnimationTokens.DecelerateEasing
            )
        )
    }

    Canvas(modifier = modifier.testTag("sparkline")) {
        if (points.isNotEmpty()) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)
            val path = Path()

            val maxRenderedIndex = (trimProgress.value * (points.size - 1)).toInt()
            val fraction = (trimProgress.value * (points.size - 1)) - maxRenderedIndex

            // Start path
            path.moveTo(0f, height * (1f - points[0]))

            for (i in 1..maxRenderedIndex) {
                path.lineTo(i * stepX, height * (1f - points[i]))
            }

            // Interpolate last segment
            if (maxRenderedIndex < points.size - 1 && fraction > 0f) {
                val startX = maxRenderedIndex * stepX
                val startY = height * (1f - points[maxRenderedIndex])
                val endX = (maxRenderedIndex + 1) * stepX
                val endY = height * (1f - points[maxRenderedIndex + 1])

                val currentX = startX + (endX - startX) * fraction
                val currentY = startY + (endY - startY) * fraction
                path.lineTo(currentX, currentY)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

private fun getCategoryIcon(category: PredictionCategory): ImageVector {
    return when (category) {
        PredictionCategory.POLITICS -> Icons.Default.Gavel
        PredictionCategory.SPORTS -> Icons.Default.SportsBasketball
        PredictionCategory.ECONOMY -> Icons.AutoMirrored.Filled.TrendingUp
        PredictionCategory.CULTURE -> Icons.Default.Palette
        PredictionCategory.TECHNOLOGY -> Icons.Default.Memory
        PredictionCategory.BUSINESS -> Icons.Default.BusinessCenter
        PredictionCategory.ENTERTAINMENT -> Icons.Default.Movie
        PredictionCategory.SCIENCE -> Icons.Default.Science
    }
}

private fun formatDate(epochMs: Long): String {
    // Simple custom date formatter
    val date = java.util.Date(epochMs)
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(date)
}
