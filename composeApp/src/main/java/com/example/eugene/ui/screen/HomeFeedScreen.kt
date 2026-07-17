package com.example.eugene.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.HomeFeedTab
import com.example.di.viewmodel.HomeFeedUiState
import com.example.di.viewmodel.HomeFeedViewModel
import com.example.domain.model.Prediction
import com.example.domain.model.Session
import com.example.eugene.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel,
    onPredictionClick: (String) -> Unit,
    onProfileClick: (String?) -> Unit,
    onNotificationsClick: () -> Unit,
    onCreatePredictionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val session by viewModel.session.collectAsState()

    Scaffold(
        modifier = modifier.testTag("home_feed_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        var tapCount by remember { mutableStateOf(0) }
                        Text(
                            text = "Eugene",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            modifier = Modifier
                                .testTag("eugene_wordmark")
                                .clickable {
                                    tapCount++
                                    if (tapCount >= 5) {
                                        tapCount = 0
                                        com.example.eugene.ui.system.DebugMenuState.showDebugMenu = true
                                    }
                                }
                        )
                    }
                },
                navigationIcon = {
                    val avatarUrl = (session as? Session.Authenticated)?.avatarUrl ?: "https://example.com/avatar.png"
                    IconButton(
                        onClick = { onProfileClick(null) },
                        modifier = Modifier
                            .testTag("top_bar_avatar")
                            .size(40.dp)
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("notifications_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Tabs: For You / Activity
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty()) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                Tab(
                    selected = currentTab == HomeFeedTab.FOR_YOU,
                    onClick = { viewModel.selectTab(HomeFeedTab.FOR_YOU) },
                    text = {
                        Text(
                            text = "For You",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("tab_for_you")
                        )
                    }
                )
                Tab(
                    selected = currentTab == HomeFeedTab.ACTIVITY,
                    onClick = { viewModel.selectTab(HomeFeedTab.ACTIVITY) },
                    text = {
                        Text(
                            text = "Activity",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("tab_activity")
                        )
                    }
                )
            }

            // Tab Content
            when (currentTab) {
                HomeFeedTab.FOR_YOU -> {
                    ForYouTabContent(
                        uiState = uiState,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.pullToRefresh() },
                        onPredictionClick = onPredictionClick,
                        onAvatarClick = { uid -> onProfileClick(uid) }
                    )
                }
                HomeFeedTab.ACTIVITY -> {
                    ActivityTabContent(
                        onPredictionClick = onPredictionClick,
                        onAvatarClick = { uid -> onProfileClick(uid) }
                    )
                }
            }
        }
    }
}

@Composable
fun ForYouTabContent(
    uiState: HomeFeedUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPredictionClick: (String) -> Unit,
    onAvatarClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            HomeFeedUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Category chips skeleton
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(4) {
                                ShimmerPlaceholder(
                                    modifier = Modifier
                                        .size(80.dp, 32.dp),
                                    shape = EugeneShapes.pill
                                )
                            }
                        }
                    }
                    items(3) {
                        PredictionCardSkeleton()
                    }
                }
            }
            HomeFeedUiState.Empty -> {
                EmptyState(
                    title = "No Live Predictions",
                    description = "There are no live active predictions right now. Check back soon or create your own question!",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    ctaLabel = "Refresh",
                    onCtaClick = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is HomeFeedUiState.Error -> {
                EmptyState(
                    title = "Connection Issues",
                    description = uiState.message,
                    icon = Icons.Default.Refresh,
                    ctaLabel = "Try Again",
                    onCtaClick = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is HomeFeedUiState.Loaded -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Featured",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FeaturedRow(
                            featuredList = uiState.featured,
                            onPredictionClick = onPredictionClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Moving Now",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(uiState.predictions) { prediction ->
                        PredictionCard(
                            prediction = prediction,
                            notablePredictors = emptyList(), // Can supply actual predictors from usecase if wanted
                            onClick = { onPredictionClick(prediction.id) },
                            onAvatarClick = onAvatarClick
                        )
                    }
                }
            }
        }

        // Mini refresh spinner at top when loading/refreshing
        AnimatedVisibility(
            visible = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.padding(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun FeaturedRow(
    featuredList: List<Prediction>,
    onPredictionClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(featuredList) { prediction ->
            Card(
                onClick = { onPredictionClick(prediction.id) },
                modifier = Modifier
                    .width(200.dp)
                    .height(120.dp)
                    .testTag("featured_card_${prediction.id}"),
                shape = EugeneShapes.card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryTag(category = prediction.category)
                    Text(
                        text = prediction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${prediction.totalSeconds} Seconds cast",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Activity list mockup representing chronological peer-engagement
@Composable
fun ActivityTabContent(
    onPredictionClick: (String) -> Unit,
    onAvatarClick: (String) -> Unit
) {
    val mockActivities = remember {
        listOf(
            ActivityItem("user_alice", "Alice Cooper", "@alice", "placed a Second on 'Will tech stocks recover?'", "5m ago", "1"),
            ActivityItem("user_bob", "Bob Dylan", "@bob", "replied in Discourse on 'Who wins the match?'", "12m ago", "2"),
            ActivityItem("user_charlie", "Charlie Brown", "@charlie", "placed a Second on 'Interest rates fall in Q3?'", "34m ago", "3"),
            ActivityItem("user_admin", "Eugene Moderator", "@eugene", "resolved prediction 'Will Artemis II launch on time?'", "1h ago", "4")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        items(mockActivities) { item ->
            Card(
                onClick = { onPredictionClick(item.predictionId) },
                modifier = Modifier.fillMaxWidth(),
                shape = EugeneShapes.card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onAvatarClick(item.uid) }
                    ) {
                        AsyncImage(
                            model = "https://example.com/avatar/${item.uid}.png",
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class ActivityItem(
    val uid: String,
    val name: String,
    val handle: String,
    val text: String,
    val time: String,
    val predictionId: String
)
