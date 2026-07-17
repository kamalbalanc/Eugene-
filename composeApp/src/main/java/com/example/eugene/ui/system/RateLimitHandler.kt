package com.example.eugene.ui.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RateLimitHandler(
    private val toastState: ToastState,
    private val scope: CoroutineScope,
    private val cooldownMs: Long = 3000L
) {
    var isCoolingDown by mutableStateOf(false)
        private set

    private var cooldownJob: Job? = null

    fun triggerAction(onAction: () -> Unit) {
        if (!isCoolingDown) {
            isCoolingDown = true
            onAction()
            cooldownJob?.cancel()
            cooldownJob = scope.launch {
                delay(cooldownMs)
                isCoolingDown = false
            }
        } else {
            toastState.showRateLimited()
        }
    }
}

@Composable
fun rememberRateLimitHandler(
    toastState: ToastState = LocalToastState.current,
    cooldownMs: Long = 3000L
): RateLimitHandler {
    val scope = rememberCoroutineScope()
    return remember(toastState, cooldownMs, scope) {
        RateLimitHandler(toastState, scope, cooldownMs)
    }
}
