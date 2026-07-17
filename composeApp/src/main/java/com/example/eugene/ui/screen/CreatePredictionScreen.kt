package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.di.viewmodel.*
import com.example.domain.model.PredictionCategory
import com.example.eugene.ui.components.CategoryTag
import com.example.eugene.ui.components.EugeneShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePredictionScreen(
    viewModel: CreatePredictionViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val title by viewModel.title.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val category by viewModel.category.collectAsState()
    val outcomes by viewModel.outcomes.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()

    Scaffold(
        modifier = modifier.testTag("create_prediction_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Prediction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (submissionState != SubmissionAnimState.IDLE) {
                // Submission Animation Sequence Showcase
                SubmissionStateChoreography(
                    submissionState = submissionState,
                    onFinish = {
                        viewModel.reset()
                        onDismiss()
                    }
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    // Step progress line
                    StepProgressBar(currentStep = currentStep)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (validationError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("validation_error_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = validationError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Content based on step
                    AnimatedContent(
                        targetState = currentStep,
                        label = "create_step_animation"
                    ) { step ->
                        when (step) {
                            CreateStep.TITLE_RULES -> StepTitleRules(
                                title = title,
                                onTitleChange = { viewModel.title.value = it },
                                rules = rules,
                                onRulesChange = { viewModel.rules.value = it }
                            )
                            CreateStep.CATEGORY -> StepCategory(
                                selected = category,
                                onSelect = { viewModel.category.value = it }
                            )
                            CreateStep.OUTCOMES -> StepOutcomes(
                                outcomes = outcomes,
                                onAdd = { viewModel.addOutcome() },
                                onRemove = { viewModel.removeOutcome(it) },
                                onUpdate = { idx, txt -> viewModel.updateOutcome(idx, txt) }
                            )
                            CreateStep.DATES -> StepDates(
                                viewModel = viewModel
                            )
                            CreateStep.REVIEW -> StepReview(
                                title = title,
                                rules = rules,
                                category = category,
                                outcomes = outcomes
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep != CreateStep.TITLE_RULES) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier.testTag("create_prev_button")
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStep != CreateStep.REVIEW) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.testTag("create_next_button")
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submit() },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("create_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Create Prediction")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(currentStep: CreateStep) {
    val progress = when (currentStep) {
        CreateStep.TITLE_RULES -> 0.2f
        CreateStep.CATEGORY -> 0.4f
        CreateStep.OUTCOMES -> 0.6f
        CreateStep.DATES -> 0.8f
        CreateStep.REVIEW -> 1.0f
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Step ${currentStep.ordinal + 1} of 5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentStep.name.replace("_", " ").lowercase().capitalize(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StepTitleRules(
    title: String,
    onTitleChange: (String) -> Unit,
    rules: String,
    onRulesChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Your Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("e.g. Will tech stocks recover in Q4 2026?") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_title"),
            maxLines = 3,
            supportingText = {
                Text(
                    text = "${title.length}/120 characters",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Rules & Criteria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = rules,
            onValueChange = onRulesChange,
            placeholder = { Text("Specify exactly how this prediction resolves...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("input_rules"),
            supportingText = {
                Text(
                    text = "${rules.length}/300 characters",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        )
    }
}

@Composable
fun StepCategory(
    selected: PredictionCategory?,
    onSelect: (PredictionCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Select Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PredictionCategory.entries) { cat ->
                val isSelected = selected == cat
                Card(
                    onClick = { onSelect(cat) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_select_${cat.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = EugeneShapes.card
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onSelect(cat) })
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(cat.name.lowercase().capitalize(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StepOutcomes(
    outcomes: List<String>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Outcome Options (2 to 6)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (outcomes.size < 6) {
                TextButton(onClick = onAdd, modifier = Modifier.testTag("add_outcome_button")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Option")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(outcomes) { index, outcome ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag Handle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = outcome,
                        onValueChange = { onUpdate(index, it) },
                        placeholder = { Text("Outcome option ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("outcome_input_$index"),
                        singleLine = true
                    )
                    if (outcomes.size > 2) {
                        IconButton(
                            onClick = { onRemove(index) },
                            modifier = Modifier.testTag("remove_outcome_$index")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDates(
    viewModel: CreatePredictionViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Target Dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Prediction Closes On", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "Closes in 3 days (Simulated)",
            onValueChange = {},
            enabled = false,
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Closes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Resolution Source Expectation", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        val source by viewModel.source.collectAsState()
        OutlinedTextField(
            value = source,
            onValueChange = { viewModel.source.value = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepReview(
    title: String,
    rules: String,
    category: PredictionCategory?,
    outcomes: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Review and Submit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Live card preview (thumbnail scale)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = EugeneShapes.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(EugeneShapes.card)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Help, contentDescription = "Help")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (category != null) {
                            CategoryTag(category = category)
                        }
                        Text("Draft Preview", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title.ifBlank { "Unspecified Question" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                outcomes.filter { it.isNotBlank() }.forEach { opt ->
                    Text("- $opt", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Rules description", fontWeight = FontWeight.Bold)
        Text(rules.ifBlank { "None" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SubmissionStateChoreography(
    submissionState: SubmissionAnimState,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (submissionState) {
            SubmissionAnimState.SUBMITTING -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submitting prediction...", style = MaterialTheme.typography.bodyLarge)
            }
            SubmissionAnimState.SUBMITTED -> {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submitted successfully!", style = MaterialTheme.typography.titleLarge)
            }
            SubmissionAnimState.UNDER_REVIEW -> {
                Icon(Icons.Default.History, contentDescription = "Clock", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Moderator review pending...", style = MaterialTheme.typography.titleLarge)
            }
            SubmissionAnimState.APPROVED -> {
                Icon(Icons.Default.Verified, contentDescription = "Approved", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Approved and Live!", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onFinish) {
                    Text("Done")
                }
            }
            SubmissionAnimState.REJECTED -> {
                Icon(Icons.Default.Cancel, contentDescription = "Rejected", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submission Rejected", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onFinish) {
                    Text("Go Back")
                }
            }
            else -> {}
        }
    }
}
