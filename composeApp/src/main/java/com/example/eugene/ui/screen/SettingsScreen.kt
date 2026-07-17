package com.example.eugene.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.viewmodel.SettingsViewModel
import com.example.domain.model.Session
import com.example.eugene.ui.components.EugeneShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val session by viewModel.session.collectAsState()

    var preferencesOpen by remember { mutableStateOf(true) }
    var accountOpen by remember { mutableStateOf(false) }
    var privacyOpen by remember { mutableStateOf(false) }
    var helpOpen by remember { mutableStateOf(false) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (session is Session.Guest) {
            onSignOut()
        }
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Preferences Section
            item {
                SettingsAccordionHeader(
                    title = "Preferences",
                    isOpen = preferencesOpen,
                    onToggle = { preferencesOpen = !preferencesOpen },
                    tag = "preferences"
                )
            }
            if (preferencesOpen) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("preferences_card"),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Dark Cosmic Theme", fontWeight = FontWeight.Bold)
                                    Text("Toggle between light and eye-safe cosmic theme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isDarkTheme,
                                    onCheckedChange = { viewModel.toggleDarkTheme(it) },
                                    modifier = Modifier.testTag("dark_theme_switch")
                                )
                            }
                        }
                    }
                }
            }

            // 2. Account Information Section
            item {
                SettingsAccordionHeader(
                    title = "Account Information",
                    isOpen = accountOpen,
                    onToggle = { accountOpen = !accountOpen },
                    tag = "account"
                )
            }
            if (accountOpen) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("account_card"),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val user = session as? Session.Authenticated
                            if (user != null) {
                                AccountItemRow(label = "Name", value = user.name)
                                AccountItemRow(label = "Handle", value = user.handle)
                                AccountItemRow(label = "Email Address", value = user.email)
                                AccountItemRow(label = "Total Resolved", value = "${user.resolvedPredictionCount}")
                            } else {
                                Text("Signed in as Guest. Please sign in to view account details.", modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            // 3. Privacy & Disclaimers Section
            item {
                SettingsAccordionHeader(
                    title = "Security & Privacy",
                    isOpen = privacyOpen,
                    onToggle = { privacyOpen = !privacyOpen },
                    tag = "privacy"
                )
            }
            if (privacyOpen) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("privacy_card"),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Immutability Policy", fontWeight = FontWeight.Bold)
                            Text("All Seconds cast are final, immutable, and public. Once you lock in a prediction Second, it cannot be deleted, edited, or retracted.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Reciprocal Network Graph", fontWeight = FontWeight.Bold)
                            Text("Your connections are strictly one-way tracking. The list of who keeps tabs on you is hidden by design to prevent gaming or vanity metrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 4. Help & Support
            item {
                SettingsAccordionHeader(
                    title = "Help & Support",
                    isOpen = helpOpen,
                    onToggle = { helpOpen = !helpOpen },
                    tag = "help"
                )
            }
            if (helpOpen) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("help_card"),
                        shape = EugeneShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            HelpLinkRow(title = "Community Guidelines", desc = "Rules on discourse, forecasting, and community conduct")
                            HelpLinkRow(title = "Resolution Mechanism Rules", desc = "How predictions are settled and verified via official sources")
                            HelpLinkRow(title = "Reputation System", desc = "Learn how accuracy rates and resolved counts formulate your reputation score")
                        }
                    }
                }
            }

            // 5. Danger Zone Accordion / Action Items
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Danger Zone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("danger_zone_card"),
                    shape = EugeneShapes.card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sign Out", fontWeight = FontWeight.Bold)
                                Text("Disconnect current user session securely", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.signOut() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("settings_sign_out_button"),
                                shape = EugeneShapes.pill
                            ) {
                                Text("Sign Out")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Delete Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Permanently delete account. Your public prediction records will remain anonymized.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { showDeleteConfirmation = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
                                modifier = Modifier.testTag("settings_delete_account_button"),
                                shape = EugeneShapes.pill
                            ) {
                                Text("Delete Account")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Account Deletion", color = MaterialTheme.colorScheme.error) },
            text = { Text("Are you absolutely sure you want to delete your account? This action is permanent and cannot be undone. In accordance with the platform's public ledger guidelines, your past cast prediction Seconds will remain as anonymous public items.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel")
                }
            },
            shape = EugeneShapes.card
        )
    }
}

@Composable
fun SettingsAccordionHeader(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp)
            .testTag("settings_accordion_header_$tag"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Icon(
            imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isOpen) "Collapse" else "Expand"
        )
    }
}

@Composable
fun AccountItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HelpLinkRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Simulate link click */ }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = "Navigate")
    }
}
