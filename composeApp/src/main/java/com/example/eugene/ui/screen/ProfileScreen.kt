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
import com.example.domain.model.SecondStatus
import kotlinx.coroutines.delay
import com.example.eugene.ui.components.EmptyState
import com.example.eugene.ui.components.EugeneAnimationTokens
import com.example.eugene.ui.components.EugeneShapes
import com.example.eugene.ui.components.PredictionCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

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
            ProfileUiState.Guest -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Sign In Required",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sign In Required",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Anonymous predictors are not supported. Sign in to view your profile and participate in the Eugene prediction market.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.signInAs("ESTABLISHED") },
                                modifier = Modifier.fillMaxWidth().testTag("guest_sign_in_established"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Sign In as Established Fan")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.signInAs("NEW") },
                                modifier = Modifier.fillMaxWidth().testTag("guest_sign_in_new")
                            ) {
                                Text("Sign In as New Predictor")
                            }
                        }
                    }
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

                val resolvedSecondsSorted = remember(state.seconds) {
                    state.seconds
                        .filter { it.status == SecondStatus.CORRECT || it.status == SecondStatus.INCORRECT }
                        .sortedBy { it.castAt }
                }

                val accuracyTrendPoints = remember(resolvedSecondsSorted) {
                    val points = mutableListOf<Float>()
                    var correctCount = 0
                    resolvedSecondsSorted.forEachIndexed { index, sec ->
                        if (sec.status == SecondStatus.CORRECT) {
                            correctCount++
                        }
                        val cumulativeAccuracy = (correctCount.toFloat() / (index + 1)) * 100f
                        points.add(cumulativeAccuracy)
                    }
                    if (points.isEmpty()) {
                        listOf(50f, 60f, 75f, 70f, 85f)
                    } else {
                        points
                    }
                }

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

                            // Reordered and gated stats row: Reputation (headline) | Accuracy | Correct | Total Seconds
                            if (state.stats.isAccuracyEligible) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${reputationAnim.value.toInt()}",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.testTag("odometer_reputation")
                                            )
                                            Text(
                                                "REPUTATION",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${accuracyAnim.value.toInt()}%",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.testTag("odometer_accuracy")
                                            )
                                            Text(
                                                "ACCURACY RATE",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${state.stats.correctSeconds}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                            Text(
                                                "CORRECT SECONDS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${state.stats.totalSeconds}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Text(
                                                "TOTAL SECONDS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked Stats",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Accuracy shown after 5 resolved predictions",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${state.stats.correctSeconds}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                            Text(
                                                "CORRECT SECONDS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${state.stats.totalSeconds}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                 )
                                            )
                                            Text(
                                                "TOTAL SECONDS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
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
                        // Accuracy Trend Chart
                        item {
                            AccuracyTrendChart(
                                points = accuracyTrendPoints,
                                isEligible = state.stats.isAccuracyEligible,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

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
                                title = "Milestone Achievements & Reputation Tiers",
                                isOpen = badgesOpen,
                                onToggle = { badgesOpen = !badgesOpen }
                            )
                        }
                        if (badgesOpen) {
                            item {
                                val stats = state.stats
                                val accuracy = stats.accuracy
                                val reputation = stats.reputation
                                val correct = stats.correctSeconds
                                val total = stats.totalSeconds
                                val isEligible = stats.isAccuracyEligible

                                val isNewcomerUnlocked = true
                                val isRisingUnlocked = isEligible && reputation >= 100
                                val isAnalystUnlocked = isEligible && reputation >= 500
                                val isExpertUnlocked = isEligible && reputation >= 1000

                                val isTop10PercentUnlocked = isEligible && reputation >= 1000
                                val isAccuracyMasterUnlocked = isEligible && accuracy >= 80
                                val isTrendSpotterUnlocked = correct >= 5
                                val isEarlyBirdUnlocked = total >= 3

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = EugeneShapes.card,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Reputation Tiers Group
                                        Text(
                                            text = "REPUTATION TIERS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        MilestoneBadgeItem(
                                            name = "Newcomer Tier",
                                            desc = "Starting point of every predictor. Unlocked on profile creation.",
                                            icon = Icons.Default.Person,
                                            isEarned = isNewcomerUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Rising Tier",
                                            desc = "Recognized predictor. Unlocks at 100+ Reputation.",
                                            icon = Icons.Default.TrendingUp,
                                            isEarned = isRisingUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Analyst Tier",
                                            desc = "Accurate analytical mind. Unlocks at 500+ Reputation.",
                                            icon = Icons.Default.Star,
                                            isEarned = isAnalystUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Expert Tier",
                                            desc = "Venerated forecasting authority. Unlocks at 1,000+ Reputation.",
                                            icon = Icons.Default.ThumbUp,
                                            isEarned = isExpertUnlocked
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Achievement Badges Group
                                        Text(
                                            text = "ACHIEVEMENT BADGES",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        MilestoneBadgeItem(
                                            name = "Top 10%",
                                            desc = "Elite echelon of predictive accuracy. Unlocked at 1,000+ Reputation.",
                                            icon = Icons.Default.Star,
                                            isEarned = isTop10PercentUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Accuracy Master",
                                            desc = "Sustained high precision. Unlocked at 80%+ Accuracy rate.",
                                            icon = Icons.Default.Check,
                                            isEarned = isAccuracyMasterUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Trend Spotter",
                                            desc = "Keen eye for crowd patterns. Unlocked after 5 Correct Seconds.",
                                            icon = Icons.Default.Lightbulb,
                                            isEarned = isTrendSpotterUnlocked
                                        )
                                        MilestoneBadgeItem(
                                            name = "Early Bird",
                                            desc = "Rapid early stance on predictions. Unlocked after placing 3 Seconds.",
                                            icon = Icons.Default.Schedule,
                                            isEarned = isEarlyBirdUnlocked
                                        )
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

@Composable
fun MilestoneBadgeItem(
    name: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEarned: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEarned) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Badge",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isEarned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AccuracyTrendChart(
    points: List<Float>,
    isEligible: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = EugeneShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Accuracy Over Time",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tracks historical accuracy on resolved predictions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (!isEligible) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Chart",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unlock trend chart at 5 resolved predictions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw gridlines
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = height * i / gridCount
                            drawLine(
                                color = gridColor,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        if (points.size >= 2) {
                            val path = Path()
                            val fillPath = Path()

                            val xStep = width / (points.size - 1)
                            val yMin = 0f
                            val yMax = 100f
                            val yRange = yMax - yMin

                            points.forEachIndexed { i, accuracy ->
                                val x = i * xStep
                                val yNormalized = 1f - ((accuracy - yMin) / yRange)
                                val y = yNormalized * height

                                if (i == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, height)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }

                                if (i == points.size - 1) {
                                    fillPath.lineTo(x, height)
                                    fillPath.close()
                                }
                            }

                            // Draw gradient fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )

                            // Draw main trend line
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Draw points
                            points.forEachIndexed { i, accuracy ->
                                val x = i * xStep
                                val yNormalized = 1f - ((accuracy - yMin) / yRange)
                                val y = yNormalized * height

                                drawCircle(
                                    color = secondaryColor,
                                    radius = 4.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
