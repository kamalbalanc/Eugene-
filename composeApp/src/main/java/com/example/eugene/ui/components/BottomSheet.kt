package com.example.eugene.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EugeneBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    // Custom sheet state that blocks manual drag-dismissal when loading is active
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (isLoading) {
                // Block gesture dismissals during sheet transactions
                false
            } else {
                true
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!isLoading) {
                onDismissRequest()
            }
        },
        sheetState = sheetState,
        shape = EugeneShapes.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        dragHandle = {
            // Drag handle: 32x4dp pill, subtle border color representation
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(EugeneShapes.pill)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            )
        },
        modifier = modifier.testTag("eugene_bottom_sheet")
    ) {
        // Safe container wrapping sheet contents using Column to supply ColumnScope
        Column(
            modifier = Modifier
                .padding(bottom = 16.dp) // standard safe spacing at the bottom
        ) {
            content()
        }
    }
}
