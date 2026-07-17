package com.example.eugene.ui.system

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eugene.ui.components.EugeneAnimationTokens
import com.example.eugene.ui.components.EugeneColors
import com.example.eugene.ui.components.EugeneShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ToastMessage(
    val id: String,
    val message: String,
    val isError: Boolean = false,
    val icon: ImageVector? = null,
    val actionLabel: String? = null,
    val isLockIcon: Boolean = false,
    val onAction: (() -> Unit)? = null
)

class ToastState {
    var currentToast by mutableStateOf<ToastMessage?>(null)
        private set

    fun showToast(
        message: String,
        isError: Boolean = false,
        icon: ImageVector? = null,
        actionLabel: String? = null,
        isLockIcon: Boolean = false,
        onAction: (() -> Unit)? = null
    ) {
        currentToast = ToastMessage(
            id = java.util.UUID.randomUUID().toString(),
            message = message,
            isError = isError,
            icon = icon,
            actionLabel = actionLabel,
            isLockIcon = isLockIcon,
            onAction = onAction
        )
    }

    fun dismiss() {
        currentToast = null
    }
}

val LocalToastState = staticCompositionLocalOf { ToastState() }

@Composable
fun ToastHost(
    hostState: ToastState,
    modifier: Modifier = Modifier,
    bottomPadding: Int = 80 // offset to sit above bottom nav
) {
    val currentToast = hostState.currentToast
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentToast) {
        if (currentToast != null) {
            isVisible = true
            val holdDuration = if (currentToast.actionLabel != null) 5000L else 3000L
            delay(holdDuration)
            isVisible = false
            delay(EugeneAnimationTokens.Standard.toLong()) // Wait for exit animation
            hostState.dismiss()
        } else {
            isVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = bottomPadding.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && currentToast != null,
            enter = slideInVertically(
                initialOffsetY = { 16.dp.value.roundToInt() }, // Entrance: slide up from translateY:+16dp
                animationSpec = EugeneAnimationTokens.springDefault() // Entrance: spring
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = EugeneAnimationTokens.Standard
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Exit: slide down
                animationSpec = tween(
                    durationMillis = EugeneAnimationTokens.Standard,
                    easing = EugeneAnimationTokens.AccelerateEasing // Exit: accelerate easing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = EugeneAnimationTokens.Standard
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

    val accentColor = if (toast.isError) {
        if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
    } else {
        if (isDark) EugeneColors.DarkSage else EugeneColors.LightSage
    }

    val icon = toast.icon ?: if (toast.isError) Icons.Default.Warning else Icons.Default.CheckCircle

    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = EugeneAnimationTokens.springSettle()
    )

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
                        // Swipe-to-dismiss threshold: 120dp
                        val thresholdPx = 120.dp.toPx()
                        if (offsetX.value > thresholdPx || offsetX.value < -thresholdPx) {
                            onDismiss()
                        } else {
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (toast.isError) "Warning" else "Success",
                tint = accentColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(if (toast.isLockIcon) animatedRotation else 0f)
                    .clickable(enabled = toast.isLockIcon) {
                        rotationAngle += 360f // Lock icon click rotation
                    }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = toast.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                ),
                modifier = Modifier.weight(1f)
            )

            if (toast.actionLabel != null && toast.onAction != null) {
                TextButton(
                    onClick = {
                        toast.onAction.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = accentColor
                    ),
                    modifier = Modifier.testTag("toast_action_button")
                ) {
                    Text(
                        text = toast.actionLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
