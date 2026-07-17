package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.*
import com.example.domain.model.Comment
import com.example.domain.model.DiscourseEntry
import com.example.domain.model.PredictionOption
import com.example.domain.model.PredictionStatus
import com.example.domain.model.PredictionCategory
import com.example.domain.model.Prediction
import com.example.domain.model.SecondingSnapshot
import com.example.domain.model.TimeRange
import com.example.eugene.ui.components.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionDetailScreen(
    viewModel: PredictionDetailViewModel,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isCasting by viewModel.isCasting.collectAsState()
    val castError by viewModel.castError.collectAsState()

    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    var reasoningText by remember { mutableStateOf("") }
    var hasCasted by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("prediction_detail_screen"),
        topBar = {}
    ) { innerPadding ->
        when (val state = uiState) {
            PredictionDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PredictionDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    EmptyState(
                        title = "Prediction Not Found",
                        description = "The requested prediction could not be loaded. It may have been deleted or archived.",
                        icon = Icons.Default.Warning
                    )
                }
            }
            is PredictionDetailUiState.Success -> {
                val prediction = state.prediction

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Modern Hero Banner Box at the top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (prediction.heroImageUrl != null) {
                            AsyncImage(
                                model = prediction.heroImageUrl,
                                contentDescription = "Detail Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.45f
                            )
                        }

                        // Gradient overlay for visual polish & text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )

                        // Floating Navigation Back Icon
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(12.dp)
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                                .testTag("back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        // Floating Category and Title on top of Hero Image
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryTag(category = prediction.category)
                                StatusBadge(status = prediction.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prediction.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Description & Details in a content card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = prediction.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // If Resolved, show correct outcome banner
                            if (prediction.status == PredictionStatus.RESOLVED && prediction.resolvedOutcomeId != null) {
                                val resolvedOpt = prediction.options.firstOrNull { it.id == prediction.resolvedOutcomeId }
                                if (resolvedOpt != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        shape = EugeneShapes.card,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = "Resolved", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "RESOLVED OUTCOME: ${resolvedOpt.text}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5 Tab Rows
                    ScrollableTabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        DetailTab.entries.forEach { tab ->
                            Tab(
                                selected = currentTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }

                    // Tab Content view container
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            DetailTab.OVERVIEW -> TabPrediction(
                                options = prediction.options,
                                selectedOptionId = selectedOptionId,
                                hasCasted = hasCasted,
                                isCasting = isCasting,
                                isLive = prediction.status == PredictionStatus.LIVE,
                                reasoningText = reasoningText,
                                messageState = castError,
                                onOptionSelect = { selectedOptionId = it },
                                onReasoningChange = { reasoningText = it },
                                onCastSecond = {
                                    if (selectedOptionId != null) {
                                        viewModel.submitSecond(selectedOptionId!!, reasoningText) {
                                            hasCasted = true
                                        }
                                    }
                                }
                            )
                            DetailTab.TIMELINE -> TabCharts(prediction = prediction, viewModel = viewModel)
                            DetailTab.REASONING -> TabReasoning(comments = state.comments, onProfileClick = onProfileClick)
                            DetailTab.DISCOURSE -> TabDiscourse(
                                discourse = state.discourse,
                                onProfileClick = onProfileClick,
                                onPostDiscourse = { viewModel.postDiscourseMessage(it) }
                            )
                            DetailTab.RESOLUTION -> TabRules(prediction = prediction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabPrediction(
    options: List<PredictionOption>,
    selectedOptionId: String?,
    hasCasted: Boolean,
    isCasting: Boolean,
    isLive: Boolean,
    reasoningText: String,
    messageState: String?,
    onOptionSelect: (String) -> Unit,
    onReasoningChange: (String) -> Unit,
    onCastSecond: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Select Outcome to Second", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutcomeBarsList(
                options = options,
                selectedOptionId = selectedOptionId,
                onOptionSelect = { if (isLive && !hasCasted) onOptionSelect(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Second Composer Section
        if (isLive && !hasCasted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = EugeneShapes.card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add your Reasoning (Required)", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasoningText,
                        onValueChange = onReasoningChange,
                        placeholder = { Text("Explain why you are seconding this outcome...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("second_composer_input"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCastSecond,
                        enabled = selectedOptionId != null && reasoningText.isNotBlank() && !isCasting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_second_button"),
                        shape = CircleShape
                    ) {
                        if (isCasting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Confirm Second")
                        }
                    }
                }
            }
        } else if (hasCasted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = EugeneShapes.card,
                modifier = Modifier.fillMaxWidth().testTag("cast_success_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prediction Cast Permanently. Immutable by design.", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatInstant(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = when (localDateTime.monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
    return "$month $day, $hour:$minute"
}

@Composable
fun TabCharts(prediction: Prediction, viewModel: PredictionDetailViewModel) {
    val snapshots by viewModel.snapshots.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val isDark = isSystemInDarkTheme()

    var scrubbedSnapshot by remember { mutableStateOf<SecondingSnapshot?>(null) }
    var scrubbedIndex by remember { mutableStateOf<Int?>(null) }

    // Always fallback to the latest snapshot when not scrubbing, or if scrubbing is released
    val activeSnapshot = scrubbedSnapshot ?: snapshots.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timeframe selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timeframe:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))

            TimeRange.values().forEach { range ->
                val isSelected = selectedTimeRange == range
                val label = when (range) {
                    TimeRange.DAY -> "24H"
                    TimeRange.WEEK -> "1W"
                    TimeRange.ALL -> "ALL"
                }

                InputChip(
                    selected = isSelected,
                    onClick = { viewModel.selectTimeRange(range) },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("timeframe_${range.name.lowercase()}")
                )
            }
        }

        if (snapshots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(EugeneShapes.card)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "No data",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "History rollups are loading...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please check back shortly as database indexes synchronize.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            // Header: Display details of the active snapshot (scrubbed or current)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = EugeneShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (scrubbedSnapshot != null) "HISTORICAL SNAPSHOT" else "CURRENT DISTRIBUTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (scrubbedSnapshot != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                        
                        activeSnapshot?.let {
                            Text(
                                text = formatInstant(it.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Outcome values breakdown
                    prediction.options.forEach { option ->
                        val pct = activeSnapshot?.outcomes?.find { it.outcomeId == option.id }?.percentage ?: 0
                        val color = EugeneColors.getAccentColor(option.accent, isDark)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }

            // Interactive Chart Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(EugeneShapes.card)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        shape = EugeneShapes.card
                    )
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Background Grid labels overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("75%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("25%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }

                // Interactive Chart Canvas drawing paths and detecting gestures
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 24.dp)
                        .pointerInput(snapshots) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val index = ((offset.x / size.width) * (snapshots.size - 1))
                                        .roundToInt()
                                        .coerceIn(0, snapshots.size - 1)
                                    scrubbedIndex = index
                                    scrubbedSnapshot = snapshots[index]
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val index = ((change.position.x / size.width) * (snapshots.size - 1))
                                        .roundToInt()
                                        .coerceIn(0, snapshots.size - 1)
                                    scrubbedIndex = index
                                    scrubbedSnapshot = snapshots[index]
                                },
                                onDragEnd = {
                                    scrubbedSnapshot = null
                                    scrubbedIndex = null
                                },
                                onDragCancel = {
                                    scrubbedSnapshot = null
                                    scrubbedIndex = null
                                }
                            )
                        }
                        .pointerInput(snapshots) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val index = ((offset.x / size.width) * (snapshots.size - 1))
                                        .roundToInt()
                                        .coerceIn(0, snapshots.size - 1)
                                    scrubbedIndex = index
                                    scrubbedSnapshot = snapshots[index]
                                    tryAwaitRelease()
                                    scrubbedSnapshot = null
                                    scrubbedIndex = null
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw horizontal grid lines
                    val gridLines = listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f)
                    gridLines.forEach { frac ->
                        val y = height * frac
                        drawLine(
                            color = if (isDark) Color(0xFF332F28) else Color(0xFFEDE6DD),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. Draw historical paths for each option
                    prediction.options.forEach { option ->
                        val color = EugeneColors.getAccentColor(option.accent, isDark)
                        val path = Path()
                        
                        snapshots.forEachIndexed { index, snapshot ->
                            val pct = snapshot.outcomes.find { it.outcomeId == option.id }?.percentage ?: 0
                            val x = (index.toFloat() / (snapshots.size - 1)) * width
                            val y = height - ((pct / 100f) * height)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                join = StrokeJoin.Round,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 3. Draw vertical scrubbing guide line if user is active
                    scrubbedIndex?.let { index ->
                        val scrubbedX = (index.toFloat() / (snapshots.size - 1)) * width
                        
                        // Vertical guideline
                        drawLine(
                            color = if (isDark) Color(0xFFEDEAE4).copy(alpha = 0.5f) else Color(0xFF111111).copy(alpha = 0.4f),
                            start = androidx.compose.ui.geometry.Offset(scrubbedX, 0f),
                            end = androidx.compose.ui.geometry.Offset(scrubbedX, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Draw highlighting indicator nodes on each path line at the scrubbed intersection
                        prediction.options.forEach { option ->
                            val color = EugeneColors.getAccentColor(option.accent, isDark)
                            val snap = snapshots[index]
                            val pct = snap.outcomes.find { it.outcomeId == option.id }?.percentage ?: 0
                            val scrubbedY = height - ((pct / 100f) * height)

                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(scrubbedX, scrubbedY)
                            )
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(scrubbedX, scrubbedY)
                            )
                        }
                    }
                }
            }
            
            // Helpful instruction banner below
            Text(
                text = "💡 Tap or hold anywhere on the chart to scrub history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TabReasoning(comments: List<Comment>, onProfileClick: (String) -> Unit) {
    if (comments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reasons have been posted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(comments) { comment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EugeneShapes.card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onProfileClick(comment.authorUid) }
                            ) {
                                AsyncImage(
                                    model = comment.authorAvatarUrl,
                                    contentDescription = comment.authorName,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(comment.authorHandle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TabDiscourse(
    discourse: List<DiscourseEntry>,
    onProfileClick: (String) -> Unit,
    onPostDiscourse: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(discourse) { entry ->
                // Apply visual indentation based on nesting representation (Discourse entries support parenting)
                val indentation = if (entry.parentId != null) 24.dp else 0.dp
                Row(modifier = Modifier.fillMaxWidth().padding(start = indentation)) {
                    if (entry.parentId != null) {
                        Icon(
                            imageVector = Icons.Default.SubdirectoryArrowRight,
                            contentDescription = "Reply",
                            modifier = Modifier.size(16.dp).padding(top = 4.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = entry.authorAvatarUrl,
                                    contentDescription = entry.authorName,
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(entry.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(entry.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom discourse composer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Add to Discourse...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("discourse_composer_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (replyText.isNotBlank()) {
                        onPostDiscourse(replyText)
                        replyText = ""
                    }
                },
                modifier = Modifier.testTag("post_discourse_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Post")
            }
        }
    }
}

@Composable
fun TabRules(prediction: com.example.domain.model.Prediction) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = EugeneShapes.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resolution Rules & Conditions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(prediction.rulesDescription, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Link, contentDescription = "Source", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Resolution Source Expected:", fontWeight = FontWeight.SemiBold)
                Text(prediction.resolutionSource, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
