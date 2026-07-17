package com.example.eugene.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.PredictorSummary
import kotlinx.coroutines.delay

@Composable
fun AvatarCluster(
    predictors: List<PredictorSummary>,
    totalCount: Int,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    onAvatarClick: ((String) -> Unit)? = null
) {
    val visiblePredictors = predictors.take(maxVisible)
    val overflowCount = totalCount - visiblePredictors.size

    Row(
        modifier = modifier.testTag("avatar_cluster"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visiblePredictors.forEachIndexed { index, predictor ->
            val scaleAnim = remember { Animatable(0f) }
            LaunchedEffect(predictor.uid) {
                delay(index * EugeneAnimationTokens.StaggerDense.toLong())
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = EugeneAnimationTokens.springLight()
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = (-8 * index).dp)
                    .scale(scaleAnim.value)
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable(enabled = onAvatarClick != null) {
                        onAvatarClick?.invoke(predictor.uid)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (predictor.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = predictor.avatarUrl,
                        contentDescription = "Avatar of ${predictor.uid}",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback letter avatar
                    val initial = predictor.uid.take(1).uppercase()
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        if (overflowCount > 0) {
            val scaleAnim = remember { Animatable(0f) }
            LaunchedEffect(overflowCount) {
                delay(visiblePredictors.size * EugeneAnimationTokens.StaggerDense.toLong())
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = EugeneAnimationTokens.springLight()
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = (-8 * visiblePredictors.size).dp)
                    .scale(scaleAnim.value)
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
