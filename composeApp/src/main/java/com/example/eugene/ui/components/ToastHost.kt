package com.example.eugene.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ToastMessage(
    val id: String,
    val message: String,
    val isError: Boolean = false
)

class ToastState {
    var currentToast by mutableStateOf<ToastMessage?>(null)
        private set

    fun showToast(message: String, isError: Boolean = false) {
        currentToast = ToastMessage(
            id = java.util.UUID.randomUUID().toString(),
            message = message,
            isError = isError
        )
    }

    fun dismiss() {
        currentToast = null
    }
}

@Composable
fun rememberToastState() = remember { ToastState() }

@Composable
fun ToastHost(
    hostState: ToastState,
    modifier: Modifier = Modifier
) {
    val currentToast = hostState.currentToast
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }

    // Auto-dismiss after 3 seconds
    LaunchedEffect(currentToast) {
        if (currentToast != null) {
            isVisible = true
            delay(3000)
            isVisible = false
            delay(EugeneAnimationTokens.Standard.toLong()) // wait for exit animation
            hostState.dismiss()
        } else {
            isVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 80.dp), // Height of typical bottom nav bar to sit above it
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && currentToast != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = EugeneAnimationTokens.Standard,
                    easing = EugeneAnimationTokens.DecelerateEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = EugeneAnimationTokens.Standard,
                    easing = EugeneAnimationTokens.AccelerateEasing
                )
            )
        ) {
            currentToast?.let { toast ->
                ToastCard(
                    toast = toast,
                    onDismiss = {
                        scope.launch {
                            isVisible = false
                            delay(EugeneAnimationTokens.Standard.toLong())
                            hostState.dismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ToastCard(
    toast: ToastMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // Color constraint: No red/deep-green. Error is Orange, Success is Sage
    val accentColor = if (toast.isError) {
        if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
    } else {
        if (isDark) EugeneColors.DarkSage else EugeneColors.LightSage
    }

    val icon = if (toast.isError) Icons.Default.Warning else Icons.Default.CheckCircle

    Card(
        modifier = modifier
            .testTag("toast_card")
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(toast.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value > 300f || offsetX.value < -300f) {
                            // Dismissed by swipe
                            onDismiss()
                        } else {
                            // Snap back to center
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = EugeneAnimationTokens.springSettle()
                                )
                            }
                        }
                    }
                )
            },
        shape = EugeneShapes.pill,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (toast.isError) "Warning" else "Success",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = toast.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
