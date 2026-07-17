package com.example.eugene.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InlineSpinner(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier
            .testTag("inline_spinner")
            .size(16.dp),
        color = color,
        strokeWidth = 2.dp
    )
}

@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Smooth width morph from content width to circular spinner width on load
    val buttonWidth by animateDpAsState(
        targetValue = if (isLoading) 56.dp else 240.dp,
        animationSpec = EugeneAnimationTokens.springSettle(),
        label = "button_morph_width"
    )

    Button(
        onClick = { if (!isLoading && enabled) onClick() },
        modifier = modifier
            .testTag("loading_button")
            .width(buttonWidth)
            .height(48.dp),
        enabled = enabled && !isLoading,
        shape = EugeneShapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(EugeneAnimationTokens.Fast)) togetherWith
                fadeOut(animationSpec = tween(EugeneAnimationTokens.Fast)) using
                SizeTransform(clip = false)
            },
            label = "button_loading_content"
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PredictionCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("prediction_card_skeleton")
            .fillMaxWidth()
            .clip(EugeneShapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPlaceholder(
                    modifier = Modifier.size(40.dp),
                    shape = EugeneShapes.card
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp),
                        shape = EugeneShapes.pill
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(50.dp)
                            .height(10.dp),
                        shape = EugeneShapes.pill
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Option bars
            repeat(2) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShimmerPlaceholder(
                            modifier = Modifier
                                .width(120.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        ShimmerPlaceholder(
                            modifier = Modifier
                                .width(30.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        shape = EugeneShapes.pill
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(100.dp)
                            .height(10.dp),
                        shape = EugeneShapes.pill
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                            ) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                ShimmerPlaceholder(
                    modifier = Modifier
                        .size(60.dp, 24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}
