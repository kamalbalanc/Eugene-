package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.*
import com.example.domain.model.Prediction
import com.example.domain.model.PredictionCategory
import com.example.eugene.ui.components.CategoryTag
import com.example.eugene.ui.components.PredictionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onPredictionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("explore_screen"),
        topBar = {
            TopAppBar(
                title = {
                    var tapCount by remember { mutableStateOf(0) }
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            tapCount++
                            if (tapCount >= 5) {
                                tapCount = 0
                                com.example.eugene.ui.system.DebugMenuState.showDebugMenu = true
                            }
                        }
                    )
                },
                actions = {
                    IconButton(
                        onClick = onLeaderboardClick,
                        modifier = Modifier.testTag("leaderboard_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Leaderboard",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Expanding Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search predictions, topics or predictors...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty() || isSearchActive) {
                                IconButton(onClick = { viewModel.setSearchActive(false) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 56.dp)
                            .testTag("explore_search_bar")
                            .clickable { viewModel.setSearchActive(true) },
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("filter_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters"
                        )
                    }
                }

                // Category Chips Horizontal Scroll
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Chip
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") },
                        modifier = Modifier.testTag("category_chip_all")
                    )

                    PredictionCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat.name.lowercase().capitalize()) },
                            modifier = Modifier.testTag("category_chip_${cat.name.lowercase()}")
                        )
                    }
                }

                // Main Explore Area or Results area
                when (val state = uiState) {
                    is ExploreUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is ExploreUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Something went wrong.")
                        }
                    }
                    is ExploreUiState.SearchActive -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Type to start searching predictions...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is ExploreUiState.Default -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Text(
                                    text = "Trending Now",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(state.trending) { prediction ->
                                PredictionCard(
                                    prediction = prediction,
                                    notablePredictors = emptyList(),
                                    onClick = { onPredictionClick(prediction.id) },
                                    onAvatarClick = onProfileClick
                                )
                            }
                        }
                    }
                    is ExploreUiState.SearchResults -> {
                        var searchTab by remember { mutableStateOf(0) } // 0 = Predictions, 1 = Predictors

                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(selectedTabIndex = searchTab) {
                                Tab(
                                    selected = searchTab == 0,
                                    onClick = { searchTab = 0 },
                                    text = { Text("Predictions (${state.predictions.size})") }
                                )
                                Tab(
                                    selected = searchTab == 1,
                                    onClick = { searchTab = 1 },
                                    text = { Text("Predictors") }
                                )
                            }

                            if (searchTab == 0) {
                                if (state.predictions.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No matching predictions found.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        items(state.predictions) { prediction ->
                                            PredictionCard(
                                                prediction = prediction,
                                                notablePredictors = emptyList(),
                                                onClick = { onPredictionClick(prediction.id) },
                                                onAvatarClick = onProfileClick
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Search Results - Predictors tab
                                val mockPredictors = remember {
                                    listOf(
                                        PredictorResultItem("user_alice", "Alice Cooper", "@alice", 88),
                                        PredictorResultItem("user_bob", "Bob Dylan", "@bob", 79),
                                        PredictorResultItem("user_charlie", "Charlie Brown", "@charlie", 84)
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(mockPredictors) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(CircleShape)
                                                .clickable { onProfileClick(item.uid) }
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = "https://example.com/avatar/${item.uid}.png",
                                                contentDescription = item.name,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, fontWeight = FontWeight.Bold)
                                                Text(item.handle, style = MaterialTheme.typography.labelSmall)
                                            }
                                            Text(
                                                text = "Accuracy ${item.accuracy}%",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Simple Overlay Sheet for Filters
            AnimatedVisibility(
                visible = showFilterSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            ) {
                // Backdrop click to close
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                        .clickable { showFilterSheet = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .testTag("filter_bottom_sheet")
                            .clickable(enabled = false) {}, // prevent click-through
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .navigationBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filters",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(onClick = { viewModel.resetFilters() }) {
                                    Text("Reset")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Sort by", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val tempSortBy by viewModel.tempSortBy.collectAsState()
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(SortOption.entries) { opt ->
                                    val isSelected = tempSortBy == opt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setTempSortBy(opt) },
                                        label = { Text(opt.name.replace("_", " ").lowercase().capitalize()) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Time Range", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            val tempTimeRange by viewModel.tempTimeRange.collectAsState()
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(TimeRangeOption.entries) { opt ->
                                    val isSelected = tempTimeRange == opt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setTempTimeRange(opt) },
                                        label = { Text(opt.name.replace("_", " ").lowercase().capitalize()) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    viewModel.applyFilters()
                                    showFilterSheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_filters_button"),
                                shape = CircleShape
                            ) {
                                Text("Apply Filters")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PredictorResultItem(
    val uid: String,
    val name: String,
    val handle: String,
    val accuracy: Int
)
