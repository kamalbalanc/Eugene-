package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.*
import com.example.domain.model.Session
import kotlinx.coroutines.delay
import com.example.eugene.ui.components.EmptyState
import com.example.eugene.ui.components.EugeneAnimationTokens
import com.example.eugene.ui.components.EugeneShapes
import com.example.eugene.ui.components.PredictionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onPredictionClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onBack: (() -> Unit)? = null, // Optional if it's the main profile tab
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()

    var showTrackedListDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Predictor Profile") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!isCurrentUser) {
                        // Keep Tab toggle (eye icon morph / active ring indicator)
                        IconButton(
                            onClick = { viewModel.toggleKeepForTarget() },
                            modifier = Modifier.testTag("keep_tab_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isTracking) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Keep Tab",
                                tint = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        // Current user's actions: Keep Tabs List and Settings!
                        IconButton(
                            onClick = { showTrackedListDialog = true },
                            modifier = Modifier.testTag("profile_keep_tabs_button")
                        ) {
                            Icon(Icons.Default.Group, contentDescription = "Keep Tab Connections")
                        }
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.testTag("profile_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ProfileUiState.Error -> {
                EmptyState(
                    title = "Profile Error",
                    description = "Could not load profile details. Check your connection or try again.",
                    icon = Icons.Default.Error
                )
            }
            is ProfileUiState.Loaded -> {
                val profile = state.session

                // Smooth dual-odometer logic
                val reputationAnim = remember { Animatable(0f) }
                val accuracyAnim = remember { Animatable(0f) }

                LaunchedEffect(profile) {
                    // Reputation odometer starts 50ms before Accuracy odometer
                    reputationAnim.animateTo(
                        targetValue = profile.reputation.toFloat(),
                        animationSpec = tween(
                            durationMillis = EugeneAnimationTokens.Emphasis,
                            easing = EugeneAnimationTokens.DecelerateEasing
                        )
                    )
                }

                LaunchedEffect(profile) {
                    delay(50) // Dual-odometer delay
                    accuracyAnim.animateTo(
                        targetValue = profile.accuracy.toFloat(),
                        animationSpec = tween(
                            durationMillis = EugeneAnimationTokens.Emphasis,
                            easing = EugeneAnimationTokens.DecelerateEasing
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Header card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = profile.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = profile.handle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Dual Odometer stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${reputationAnim.value.toInt()}",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("odometer_reputation")
                                    )
                                    Text(
                                        "REPUTATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${accuracyAnim.value.toInt()}%",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.testTag("odometer_accuracy")
                                    )
                                    Text(
                                        "ACCURACY RATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Collapsible accordion sections
                    var predictionHistoryOpen by remember { mutableStateOf(false) }
                    var activeSecondsOpen by remember { mutableStateOf(false) }
                    var badgesOpen by remember { mutableStateOf(true) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Prediction History
                        item {
                            AccordionHeader(
                                title = "Prediction Submissions (${state.predictions.size})",
                                isOpen = predictionHistoryOpen,
                                onToggle = { predictionHistoryOpen = !predictionHistoryOpen }
                            )
                        }
                        if (predictionHistoryOpen) {
                            if (state.predictions.isEmpty()) {
                                item {
                                    Text("No prediction questions submitted yet.", modifier = Modifier.padding(16.dp))
                                }
                            } else {
                                items(state.predictions) { p ->
                                    PredictionCard(
                                        prediction = p,
                                        notablePredictors = emptyList(),
                                        onClick = { onPredictionClick(p.id) },
                                        onAvatarClick = {}
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        // 2. Active Seconds
                        item {
                            AccordionHeader(
                                title = "Active Seconds Cast (${state.seconds.size})",
                                isOpen = activeSecondsOpen,
                                onToggle = { activeSecondsOpen = !activeSecondsOpen }
                            )
                        }
                        if (activeSecondsOpen) {
                            if (state.seconds.isEmpty()) {
                                item {
                                    Text("No active seconds placed.", modifier = Modifier.padding(16.dp))
                                }
                            } else {
                                items(state.seconds) { sec ->
                                    Card(
                                        onClick = { onPredictionClick(sec.predictionId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = EugeneShapes.card,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Seconded Outcome ID: ${sec.optionId}", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(sec.reasoning, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        // 3. Badges (Strictly non-gamified milestone representations)
                        item {
                            AccordionHeader(
                                title = "Milestone Achievements",
                                isOpen = badgesOpen,
                                onToggle = { badgesOpen = !badgesOpen }
                            )
                        }
                        if (badgesOpen) {
                            val mockBadges = listOf(
                                MilestoneBadge("First Second", "Placed your initial prediction second.", Icons.Default.Lock),
                                MilestoneBadge("Socrates", "Posted five reasoning descriptions on predictions.", Icons.Default.MenuBook),
                                MilestoneBadge("Oracle", "Achieved an accuracy score greater than 80%.", Icons.Default.Lightbulb)
                            )
                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    mockBadges.forEach { badge ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(badge.icon, contentDescription = badge.name, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(badge.name, fontWeight = FontWeight.Bold)
                                                Text(badge.desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTrackedListDialog && uiState is ProfileUiState.Loaded) {
        val loadedState = uiState as ProfileUiState.Loaded
        val trackedPredictors = loadedState.keepTabs.map { kt ->
            LeaderboardEntry(
                uid = kt.trackedUid,
                name = "Top Predictor ${kt.trackedUid.take(4)}",
                handle = "@predictor_${kt.trackedUid.take(4)}",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde",
                accuracy = 84,
                reputation = 1250,
                resolvedCount = 12,
                isTracking = true,
                rank = null
            )
        }
        KeepSheet(
            trackedPredictors = trackedPredictors,
            onUntrackClick = { uid -> viewModel.toggleKeep(uid) },
            onProfileClick = { uid -> viewModel.setTargetUid(uid) },
            onDismissRequest = { showTrackedListDialog = false }
        )
    }
}

@Composable
fun AccordionHeader(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Icon(
            imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "Expand"
        )
    }
}

data class MilestoneBadge(
    val name: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
