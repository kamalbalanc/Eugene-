package com.example.eugene.ui.system

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object SessionExpiryState {
    var isExpired by mutableStateOf(false)
}

@Composable
fun SessionExpiryDialog(
    coroutineScope: CoroutineScope,
    authRepository: AuthRepository = koinInject(),
    onSignOutComplete: () -> Unit
) {
    if (SessionExpiryState.isExpired) {
        AlertDialog(
            onDismissRequest = {}, // Non-dismissible
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = { Text("Session Expired") },
            text = { Text("Your session has expired. Please sign in again to continue.") },
            confirmButton = {
                Button(
                    onClick = {
                        SessionExpiryState.isExpired = false
                        coroutineScope.launch {
                            authRepository.signOut()
                            onSignOutComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sign In")
                }
            }
        )
    }
}
