package com.example.eugene.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eugene.ui.components.EugeneBottomSheet
import com.example.eugene.ui.components.EugeneShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealSheet(
    onDismissRequest: () -> Unit,
    onSubmitAppeal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var appealText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    // Pulsing animation for the submit button (only while not submitted and text is not empty)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonScale = if (!isSubmitted && appealText.isNotBlank()) pulseScale else 1f

    EugeneBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("appeal_bottom_sheet"),
        isLoading = isSubmitted
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Submit Moderation Appeal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "If you believe your reasoning, post, or account limitation was flagged in error, state your case clearly below. The administrative moderation team will review your appeal. You can only appeal each decision once.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = appealText,
                onValueChange = {
                    if (it.length <= 500 && !isSubmitted) {
                        appealText = it
                    }
                },
                placeholder = { Text("Describe why this flagging was an error...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("appeal_text_input"),
                shape = EugeneShapes.card,
                enabled = !isSubmitted,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Character count indicator 0/500
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${appealText.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (appealText.length >= 500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("appeal_char_count")
                )
            }

            // Submit CTA Button
            Button(
                onClick = {
                    if (appealText.isNotBlank() && !isSubmitted) {
                        isSubmitted = true
                        onSubmitAppeal(appealText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(buttonScale)
                    .testTag("submit_appeal_button"),
                shape = EugeneShapes.pill,
                enabled = !isSubmitted && appealText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isSubmitted) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit Appeal",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
