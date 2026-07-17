package com.example.eugene.ui.system

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share

fun ToastState.showSecondCast() {
    showToast(
        message = "Second cast. This cannot be changed.",
        isError = false,
        icon = Icons.Default.Lock,
        isLockIcon = true
    )
}

fun ToastState.showMarkedHelpful() {
    showToast(
        message = "Marked helpful",
        isError = false,
        icon = Icons.Default.CheckCircle
    )
}

fun ToastState.showKeepingTab(handle: String, onUndo: () -> Unit) {
    showToast(
        message = "Keeping tab on @$handle",
        isError = false,
        actionLabel = "Undo",
        onAction = onUndo
    )
}

fun ToastState.showNoLongerKeepingTab(handle: String, onUndo: () -> Unit) {
    showToast(
        message = "No longer keeping tab on @$handle",
        isError = false,
        actionLabel = "Undo",
        onAction = onUndo
    )
}

fun ToastState.showSaved(onUndo: () -> Unit) {
    showToast(
        message = "Saved",
        isError = false,
        icon = Icons.Default.Bookmark,
        actionLabel = "Undo",
        onAction = onUndo
    )
}

fun ToastState.showRemoved(onUndo: () -> Unit) {
    showToast(
        message = "Removed",
        isError = false,
        icon = Icons.Default.Delete,
        actionLabel = "Undo",
        onAction = onUndo
    )
}

fun ToastState.showReported() {
    showToast(
        message = "Reported. Thank you for helping keep Eugene accurate.",
        isError = false,
        icon = Icons.Default.Info
    )
}

fun ToastState.showAppealSubmitted() {
    showToast(
        message = "Appeal submitted. We'll follow up once it's reviewed.",
        isError = false,
        icon = Icons.Default.Info
    )
}

fun ToastState.showLinkCopied() {
    showToast(
        message = "Link copied",
        isError = false,
        icon = Icons.Default.Share
    )
}

fun ToastState.showSomethingWentWrong(onRetry: () -> Unit) {
    showToast(
        message = "Something went wrong. Please try again.",
        isError = true,
        icon = Icons.Default.Warning,
        actionLabel = "Retry",
        onAction = onRetry
    )
}

fun ToastState.showOfflineActionQueued() {
    showToast(
        message = "You're offline. This will complete when you're back online.",
        isError = false,
        icon = Icons.Default.Info
    )
}

fun ToastState.showRateLimited() {
    showToast(
        message = "You're doing that too fast — please wait a moment.",
        isError = true,
        icon = Icons.Default.Warning
    )
}
