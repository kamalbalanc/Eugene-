package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.*
import com.example.domain.model.*
import com.example.eugene.ui.components.*
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
    var hasCastedLocal by remember { mutableStateOf(false) }
    var showSecondSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("prediction_detail_screen"),
        topBar = {}
    ) { innerPadding ->
        when (val state = uiState) {
            PredictionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is PredictionDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    EmptyState(
                        title = "Prediction Not Found",
                        description = "The requested prediction could not be loaded. It may have been deleted or archived.",
                        icon = Icons.Default.Warning
                    )
                }
            }
            is PredictionDetailUiState.Success -> {
                val prediction = state.prediction
                val comments = state.comments
                val discourse = state.discourse
                val notableSeconders = state.notableSeconders
                val session = state.session

                // Derive whether the user has already cast a second
                val hasAlreadyCasted = remember(session, comments) {
                    if (session is Session.Authenticated) {
                        comments.any { it.authorUid == session.uid }
                    } else {
                        false
                    }
                }
                val isCasted = hasCastedLocal || hasAlreadyCasted

                // Monitor scroll state of the unified lazy column list
                val listState = rememberLazyListState()
                val isScrollingDown = remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 150
                    }
                }

                // Smoothly animate height and padding adjustments based on list scroll position
                val bottomBarHeight by animateDpAsState(
                    targetValue = if (isScrollingDown.value) 64.dp else 92.dp,
                    animationSpec = tween(EugeneAnimationTokens.Standard)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Unified Main List Scroll Area
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // 1. Premium Backdrop Header Banner Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Rich background image (with Unsplash abstract fallback if null/blank)
                                val bannerUrl = prediction.heroImageUrl?.ifBlank { null }
                                    ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe"
                                
                                AsyncImage(
                                    model = bannerUrl,
                                    contentDescription = "Detail Banner Backdrop",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Dark overlay for text contrast and premium cinematic styling
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.35f),
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                // Floating upper navigation row overlay
                                Row(
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .align(Alignment.TopCenter),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = onBack,
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                                            .testTag("back_button")
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { /* bookmark */ },
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                                        ) {
                                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { /* options */ },
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                                        ) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                                        }
                                    }
                                }

                                // Header texts and metadata stacked above bottom gradient
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
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = prediction.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            lineHeight = 28.sp
                                        ),
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ENDS ${formatInstant(prediction.closesAt).uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.7f),
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // 2. Overview / Prediction Description Card (Removed description as requested)
                        item {
                            if (prediction.status == PredictionStatus.RESOLVED && prediction.resolvedOutcomeId != null) {
                                val resolvedOpt = prediction.options.firstOrNull { it.id == prediction.resolvedOutcomeId }
                                if (resolvedOpt != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        shape = EugeneShapes.card,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
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

                        // 3. Outcomes & Total Seconds statistics (Read-only representation)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                OutcomeBarsList(
                                    options = prediction.options,
                                    selectedOptionId = null,
                                    onOptionSelect = null // Clicking outcomes triggers no-op on details screen. Click "Second" below.
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL SECONDS CAST",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    val formattedSeconds = if (prediction.totalSeconds >= 1000) {
                                        "${(prediction.totalSeconds / 1000.0).toString().take(4)}K"
                                    } else {
                                        "${prediction.totalSeconds}"
                                    }
                                    Text(
                                        text = formattedSeconds,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // 4. Sticky/Flat Tabs (Timeline, Reasoning, Discourse)
                        item {
                            TabRow(
                                selectedTabIndex = when (currentTab) {
                                    DetailTab.TIMELINE -> 0
                                    DetailTab.REASONING -> 1
                                    DetailTab.DISCOURSE -> 2
                                },
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                val tabsList = listOf(DetailTab.TIMELINE, DetailTab.REASONING, DetailTab.DISCOURSE)
                                tabsList.forEach { tab ->
                                    val tabLabel = when (tab) {
                                        DetailTab.TIMELINE -> "Timeline"
                                        DetailTab.REASONING -> "Reasoning"
                                        DetailTab.DISCOURSE -> "Discourse"
                                    }
                                    Tab(
                                        selected = currentTab == tab,
                                        onClick = { viewModel.selectTab(tab) },
                                        text = {
                                            Text(
                                                text = tabLabel,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // 5. Dynamic Tab-specific content items
                        when (currentTab) {
                            DetailTab.TIMELINE -> {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        // Timeline visual elements card block
                                        TimelineContent(
                                            prediction = prediction,
                                            viewModel = viewModel,
                                            snapshots = viewModel.snapshots.collectAsState().value
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Top Predictors row using AvatarCluster
                                        if (notableSeconders.isNotEmpty()) {
                                            Text(
                                                text = "Top Predictors",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(EugeneShapes.card)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                AvatarCluster(
                                                    predictors = notableSeconders,
                                                    totalCount = notableSeconders.size,
                                                    onAvatarClick = onProfileClick
                                                )
                                                TextButton(onClick = { /* noop */ }) {
                                                    Text("View all", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        // Resolution Details block embedded at the very bottom of the timeline
                                        Text(
                                            text = "Resolution Conditions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Card(
                                            shape = EugeneShapes.card,
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = prediction.rulesDescription,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 20.sp
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Link,
                                                        contentDescription = "Source",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("Expected Verification Source:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                                        Text(
                                                            text = prediction.resolutionSource,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            DetailTab.REASONING -> {
                                if (comments.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No reasoning has been posted yet.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                } else {
                                    items(comments) { comment ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 6.dp),
                                            shape = EugeneShapes.card,
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    
                                                    // Display selected outcome tag with color highlight
                                                    val matchedOpt = prediction.options.find { it.id == comment.secondedOptionId }
                                                    if (matchedOpt != null) {
                                                        val isDark = isSystemInDarkTheme()
                                                        val optionColor = EugeneColors.getAccentColor(matchedOpt.accent, isDark)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(optionColor.copy(alpha = 0.12f))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = matchedOpt.text,
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = optionColor
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = comment.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            DetailTab.DISCOURSE -> {
                                if (discourse.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No discourse messages yet. Start the conversation!",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                } else {
                                    items(discourse) { entry ->
                                        val indentation = if (entry.parentId != null) 24.dp else 0.dp
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp + indentation, end = 16.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            if (entry.parentId != null) {
                                                Icon(
                                                    imageVector = Icons.Default.SubdirectoryArrowRight,
                                                    contentDescription = "Reply",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .padding(top = 4.dp),
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                shape = EugeneShapes.card,
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        AsyncImage(
                                                            model = entry.authorAvatarUrl,
                                                            contentDescription = entry.authorName,
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .clickable { onProfileClick(entry.authorUid) }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(entry.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        Text(
                                                            text = formatInstant(entry.postedAt),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = entry.text,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive inline Discourse composer
                                item {
                                    var replyText by remember { mutableStateOf("") }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Add to Discourse...") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("discourse_composer_input"),
                                            singleLine = true,
                                            shape = EugeneShapes.card,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                if (replyText.isNotBlank()) {
                                                    viewModel.postDiscourseMessage(replyText)
                                                    replyText = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                                .testTag("post_discourse_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Post Message",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom-padding spacer item to prevent collision/overlapping with the permanent bottom bar
                        item {
                            Spacer(modifier = Modifier.height(110.dp))
                        }
                    }

                    // 6. Permanently Visible Bottom Bar (Grows/Collapses based on scroll state)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bottomBarHeight)
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                            .padding(horizontal = 16.dp, vertical = if (isScrollingDown.value) 8.dp else 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (prediction.status != PredictionStatus.LIVE) {
                            Text(
                                text = "Prediction closed. Seconding is disabled.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (isCasted) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cast_success_card"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked Choice", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Prediction Cast Permanently. Immutable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            // Expandable / Collapsible button action
                            Button(
                                onClick = { showSecondSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .testTag("second_action_button"),
                                shape = EugeneShapes.pill,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Second Icon")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isScrollingDown.value) "Second" else "Place Second",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (isScrollingDown.value) 13.sp else 15.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Toast host for displaying operation feedbacks
                    if (castError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text(
                                    text = castError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Modal bottom sheet presented when clicking "Second" button
                if (showSecondSheet) {
                    SecondSheet(
                        options = prediction.options,
                        selectedOptionId = selectedOptionId,
                        reasoningText = reasoningText,
                        isCasting = isCasting,
                        onDismissRequest = { showSecondSheet = false },
                        onOptionSelect = { selectedOptionId = it },
                        onReasoningChange = { reasoningText = it },
                        onCastSecond = {
                            if (selectedOptionId != null && reasoningText.isNotBlank()) {
                                viewModel.submitSecond(selectedOptionId!!, reasoningText) {
                                    hasCastedLocal = true
                                    showSecondSheet = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondSheet(
    options: List<PredictionOption>,
    selectedOptionId: String?,
    reasoningText: String,
    isCasting: Boolean,
    onDismissRequest: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onReasoningChange: (String) -> Unit,
    onCastSecond: () -> Unit,
    modifier: Modifier = Modifier
) {
    EugeneBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("second_bottom_sheet"),
        isLoading = isCasting
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Place Second",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Select an outcome and explain your reasoning. Once cast, your decision is permanent and immutable.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Select Outcome (Required)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Selectable outcomes list
            OutcomeBarsList(
                options = options,
                selectedOptionId = selectedOptionId,
                onOptionSelect = onOptionSelect
            )

            Text(
                text = "Add your Reasoning (Required)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = reasoningText,
                onValueChange = onReasoningChange,
                placeholder = { Text("Explain why you are seconding this outcome...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("second_composer_input"),
                shape = EugeneShapes.card,
                enabled = !isCasting,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // CTA Submit button
            Button(
                onClick = onCastSecond,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_second_button"),
                shape = EugeneShapes.pill,
                enabled = selectedOptionId != null && reasoningText.isNotBlank() && !isCasting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isCasting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Confirm Second",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun cleanOptionText(text: String): String {
    return text.replace(Regex("\\s*\\([^)]*\\)\\s*"), "").trim()
}

@Composable
fun TimelineContent(
    prediction: Prediction,
    viewModel: PredictionDetailViewModel,
    snapshots: List<SecondingSnapshot>
) {
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val isDark = isSystemInDarkTheme()

    var scrubbedSnapshot by remember { mutableStateOf<SecondingSnapshot?>(null) }
    var scrubbedIndex by remember { mutableStateOf<Int?>(null) }

    val isScrubbing = scrubbedSnapshot != null
    // Always fallback to the latest snapshot when not scrubbing, or if scrubbing is released
    val activeSnapshot = scrubbedSnapshot ?: snapshots.lastOrNull()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (snapshots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(EugeneShapes.card)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "No data",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "History rollups are loading...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 1. Chart first
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(EugeneShapes.card)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("75%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("25%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                }

                // Interactive Canvas Chart with Gestures
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

                    // Draw horizontal grid lines
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

                    // Draw historical paths for each option
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

                    // Draw vertical scrubbing guide line if user is active
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

                        // Highlight circles
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Time range second (Timeframe selector row, without "Timeframe:" label)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(modifier = Modifier.weight(1f))

                activeSnapshot?.let {
                    Text(
                        text = formatInstant(it.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 3. Distribution third (Horizontal layout of options vertically stacked)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = EugeneShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        prediction.options.forEach { option ->
                            val pct = activeSnapshot?.outcomes?.find { it.outcomeId == option.id }?.percentage ?: 0
                            val color = EugeneColors.getAccentColor(option.accent, isDark)
                            
                            // Dynamic truncation limit based on options count:
                            // 6 options -> 6 chars max, 5 options -> 8 chars max, 4 options -> 10 chars max, etc.
                            val maxChars = 6 + (6 - prediction.options.size) * 2
                            val baseCleanText = cleanOptionText(option.text)
                            val truncatedText = if (baseCleanText.length > maxChars) {
                                baseCleanText.take(maxChars) + "…"
                            } else {
                                baseCleanText
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp).weight(1f)
                            ) {
                                Text(
                                    text = truncatedText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                    
                                    // Change delta since the start of selected timeframe
                                    if (snapshots.size > 1 && !isScrubbing) {
                                        val firstSnapshot = snapshots.firstOrNull()
                                        val firstPct = firstSnapshot?.outcomes?.find { it.outcomeId == option.id }?.percentage ?: pct
                                        val diff = pct - firstPct
                                        val diffText = if (diff > 0) "+$diff" else "$diff"
                                        val diffColor = if (diff > 0) {
                                            if (isDark) EugeneColors.DarkSage else EugeneColors.LightSage
                                        } else if (diff < 0) {
                                            if (isDark) EugeneColors.DarkOrange else EugeneColors.LightOrange
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                        
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "($diffText%)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = diffColor
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
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
