package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.viewmodel.NotificationCategoryTab
import com.example.di.viewmodel.NotificationItem
import com.example.di.viewmodel.NotificationType
import com.example.di.viewmodel.NotificationsViewModel
import com.example.eugene.ui.components.EugeneColors
import com.example.eugene.ui.components.EugeneShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit,
    onPredictionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Group items by date bucket
    val groupedNotifications = remember(notifications) {
        notifications.groupBy { it.timestamp }
    }

    Scaffold(
        modifier = modifier.testTag("notifications_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Notification Filter Tabs: All, Activity, Moderation
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                NotificationCategoryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.name.lowercase().capitalize(),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("notification_tab_${tab.name.lowercase()}")
                            )
                        }
                    )
                }
            }

            // Notification Items List
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications under this filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Display grouped notifications by timestamp buckets
                    listOf("Today", "Yesterday", "Older").forEach { bucket ->
                        val itemsInBucket = groupedNotifications[bucket] ?: emptyList()
                        if (itemsInBucket.isNotEmpty()) {
                            item {
                                Text(
                                    text = bucket,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(itemsInBucket, key = { it.id }) { item ->
                                NotificationRowCard(
                                    item = item,
                                    onClick = {
                                        item.predictionId?.let { onPredictionClick(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRowCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    // Map status colors strictly to Sage (correct/leading), Orange (incorrect/trailing), Amber (pending)
    val (iconColor, iconBgColor, icon) = when (item.type) {
        NotificationType.PREDICTION_APPROVED, NotificationType.PREDICTION_RESOLVED -> {
            // Sage: Success / Correct
            Triple(Color(0xFF81C784), Color(0xFFE8F5E9), Icons.Default.CheckCircle)
        }
        NotificationType.PREDICTION_REJECTED, NotificationType.PREDICTION_VOIDED,
        NotificationType.CONTENT_REMOVED, NotificationType.ACCOUNT_LIMITED -> {
            // Orange: Trailing / Rejected / Removed / Limited
            Triple(Color(0xFFFFB74D), Color(0xFFFFF3E0), Icons.Default.Warning)
        }
        NotificationType.FLAGGED_REASONING -> {
            // Amber: Warning / Pending flag
            Triple(Color(0xFFFFD54F), Color(0xFFFFFDE7), Icons.Default.Report)
        }
        NotificationType.PREDICTION_LIVE -> {
            Triple(Color(0xFF64B5F6), Color(0xFFE3F2FD), Icons.Default.PlayArrow)
        }
        NotificationType.PREDICTION_CLOSES_SOON -> {
            Triple(Color(0xFFBA68C8), Color(0xFFF3E5F5), Icons.Default.Timer)
        }
        NotificationType.TRACKED_ACTIVITY -> {
            Triple(Color(0xFF4DB6AC), Color(0xFFE0F2F1), Icons.Default.AccountCircle)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_row_${item.id}"),
        shape = EugeneShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Colored Status Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
