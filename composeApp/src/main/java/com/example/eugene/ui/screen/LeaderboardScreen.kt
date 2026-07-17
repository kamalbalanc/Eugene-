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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.viewmodel.LeaderboardEntry
import com.example.di.viewmodel.LeaderboardViewModel
import com.example.domain.model.PredictionCategory
import com.example.domain.model.Session
import com.example.eugene.ui.components.EugeneShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val predictors by viewModel.predictors.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val session by viewModel.session.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showTimeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("leaderboard_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("leaderboard_back_button")) {
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
            // Filters bar: Category dropdown and Time filter dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Filter Button
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showCategoryMenu = true },
                        modifier = Modifier.fillMaxWidth().testTag("leaderboard_category_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = EugeneShapes.pill
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedCategory?.name?.lowercase()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "All Categories",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                        }
                    }
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.selectCategory(null)
                                showCategoryMenu = false
                            },
                            modifier = Modifier.testTag("leaderboard_category_all")
                        )
                        PredictionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                                onClick = {
                                    viewModel.selectCategory(cat)
                                    showCategoryMenu = false
                                },
                                modifier = Modifier.testTag("leaderboard_category_${cat.name.lowercase()}")
                            )
                        }
                    }
                }

                // Time Filter Button
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showTimeMenu = true },
                        modifier = Modifier.fillMaxWidth().testTag("leaderboard_time_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = EugeneShapes.pill
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedTimeFilter,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Timeframe")
                        }
                    }
                    DropdownMenu(
                        expanded = showTimeMenu,
                        onDismissRequest = { showTimeMenu = false }
                    ) {
                        listOf("Weekly", "Monthly", "All-Time").forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    viewModel.selectTimeFilter(filter)
                                    showTimeMenu = false
                                },
                                modifier = Modifier.testTag("leaderboard_time_$filter")
                            )
                        }
                    }
                }
            }

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("Search predictors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("leaderboard_search_input"),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Table headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Predictor", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Accuracy", modifier = Modifier.width(72.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                Text("Rep", modifier = Modifier.width(72.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Main ranked list
            Box(modifier = Modifier.weight(1f)) {
                if (predictors.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No predictors found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // Leave space for Your Rank sticky card
                    ) {
                        items(predictors, key = { it.uid }) { predictor ->
                            LeaderboardRow(
                                entry = predictor,
                                onRowClick = { onProfileClick(predictor.uid) },
                                onKeepToggle = { viewModel.toggleTracking(predictor.uid) }
                            )
                        }
                    }
                }

                // Sticky "Your Rank" card
                val currentUser = session as? Session.Authenticated
                if (currentUser != null) {
                    val userEntry = predictors.find { it.uid == currentUser.uid }
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("leaderboard_sticky_user_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = EugeneShapes.card,
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val userRankStr = userEntry?.rank?.let { "#$it" } ?: "Pending"
                            Text(
                                text = userRankStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.width(48.dp)
                            )
                            AsyncImage(
                                model = currentUser.avatarUrl,
                                contentDescription = currentUser.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentUser.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(currentUser.handle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Display accuracy only if eligible
                            val accuracyStr = if (currentUser.resolvedPredictionCount >= 5) "${currentUser.accuracy}%" else "—"
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(64.dp)) {
                                Text(accuracyStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(64.dp)) {
                                Text("${currentUser.reputation}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Reputation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    onRowClick: () -> Unit,
    onKeepToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("leaderboard_row_${entry.uid}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        val rankText = entry.rank?.toString() ?: "—"
        Text(
            text = rankText,
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (entry.rank in 1..3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        // Avatar & Info
        AsyncImage(
            model = entry.avatarUrl,
            contentDescription = entry.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(entry.handle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // Inline Keep Tab Button (Eye icon toggle)
        IconButton(
            onClick = onKeepToggle,
            modifier = Modifier.testTag("leaderboard_keep_toggle_${entry.uid}")
        ) {
            Icon(
                imageVector = if (entry.isTracking) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (entry.isTracking) "Tracking" else "Untracked",
                tint = if (entry.isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        // Accuracy (Hidden if resolved < 5 per spec)
        val accuracyText = if (entry.resolvedCount >= 5) "${entry.accuracy}%" else "—"
        Text(
            text = accuracyText,
            modifier = Modifier.width(56.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = if (entry.resolvedCount < 5) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.secondary
        )

        // Reputation
        Text(
            text = "${entry.reputation}",
            modifier = Modifier.width(56.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
