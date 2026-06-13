package com.openingmind.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.openingmind.R
import com.openingmind.presentation.RepertoireViewModel
import com.openingmind.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: RepertoireViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToDetail: (Int, Boolean) -> Unit,
    onNavigateToForm: () -> Unit
) {
    val playClick = com.openingmind.LocalAudioPlayer.current

    var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_dashboard),
        stringResource(R.string.tab_kamus_eco),
        stringResource(R.string.tab_repertoire),
        stringResource(R.string.tab_ai_advisor),
        stringResource(R.string.tab_settings)
    )
    val iconResources = listOf(
        R.drawable.ic_dashboard,
        R.drawable.ic_explore,
        R.drawable.ic_book,
        R.drawable.ic_star_shine,
        R.drawable.ic_settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(id = iconResources[index]), 
                                contentDescription = title,
                                modifier = Modifier.size(24.dp),
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                title, 
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        selected = selectedTab == index,
                        onClick = { 
                            playClick()
                            selectedTab = index 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { fullWidth -> -fullWidth }
                            ) + fadeOut(animationSpec = tween(300))
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { fullWidth -> fullWidth }
                            ) + fadeOut(animationSpec = tween(300))
                        )
                    }
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(viewModel)
                    1 -> BrowseTab(
                        viewModel,
                        onNavigateToDetail = { id ->
                            playClick()
                            onNavigateToDetail(id, true)
                        }
                    )
                    2 -> RepertoireTab(
                        viewModel, 
                        onNavigateToDetail = { id ->
                            playClick()
                            viewModel.localRepertoires.value.find { it.id == id }?.let {
                                viewModel.selectLocalRepertoire(it)
                            }
                            onNavigateToDetail(id, false)
                        }, 
                        onNavigateToForm = {
                            playClick()
                            onNavigateToForm()
                        }
                    )
                    3 -> AiAdvisorTab(viewModel)
                    4 -> SettingsTab(settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardTab(viewModel: RepertoireViewModel) {
    val localCount by viewModel.localRepertoires.collectAsState()
    val lastLocal by viewModel.lastLocalRepertoire.collectAsState()
    val lastDict by viewModel.lastDictionaryOpening.collectAsState()
    val lastAi by viewModel.lastAiAdvice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            stringResource(R.string.welcome_message),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            stringResource(R.string.dashboard_subtitle),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.local_stats_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.total_notes, localCount.size),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.recent_activity),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (lastLocal == null && lastDict == null && lastAi == null) {
            Text(
                stringResource(R.string.no_recent_activity),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        lastLocal?.let {
            RecentActivityCard(stringResource(R.string.last_local), it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        lastDict?.let {
            RecentActivityCard(stringResource(R.string.last_dictionary), it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        lastAi?.let {
            RecentActivityCard(stringResource(R.string.last_ai_advice_label), it)
        }
    }
}

@Composable
fun RecentActivityCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BrowseTab(
    viewModel: RepertoireViewModel,
    onNavigateToDetail: (Int) -> Unit
) {
    val remoteOpenings by viewModel.remoteOpenings.collectAsState()
    val isLoading by viewModel.isRemoteLoading.collectAsState()
    val searchQuery by viewModel.searchQueryKamus.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            stringResource(R.string.lichess_kamus_title), 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQueryKamus(it) },
            placeholder = { Text(stringResource(R.string.search_opening_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(remoteOpenings) { opening ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ), 
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.selectRemoteOpening(opening)
                            onNavigateToDetail(opening.id)
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "${opening.ecoCode} - ${opening.name}", 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                opening.notation, 
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepertoireTab(
    viewModel: RepertoireViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToForm: () -> Unit
) {
    val localRepertoires by viewModel.localRepertoires.collectAsState()
    val searchQuery by viewModel.searchQueryRepertoar.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.my_repertoire_title), 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQueryRepertoar(it) },
                placeholder = { Text(stringResource(R.string.search_repertoire_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (localRepertoires.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.repertoire_empty_state),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(localRepertoires) { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToDetail(item.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "${item.ecoCode} - ${item.name}", 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    item.notation, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                viewModel.clearForm()
                onNavigateToForm()
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add), 
                contentDescription = stringResource(R.string.add_repertoire), 
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AiAdvisorTab(viewModel: RepertoireViewModel) {
    var query by remember { mutableStateOf("") }
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val playClick = com.openingmind.LocalAudioPlayer.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            stringResource(R.string.ai_advisor_title), 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            stringResource(R.string.ai_powered_by), 
            fontSize = 12.sp, 
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.ask_ai_hint)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        val aiSystemPrompt = stringResource(R.string.ai_system_prompt)
        val aiThinkingMsg = stringResource(R.string.ai_thinking)
        Button(
            onClick = { 
                playClick()
                viewModel.askAIChessAdvisor(
                    query, 
                    systemPrompt = aiSystemPrompt,
                    thinkingMsg = aiThinkingMsg
                )
            },
            enabled = query.isNotEmpty() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(stringResource(R.string.ask_ai_button))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Text(
                        text = aiResponse.ifEmpty { stringResource(R.string.ai_empty_state) },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsTab(settingsViewModel: SettingsViewModel) {
    val isDarkPreferred by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = isDarkPreferred ?: isSystemDark
    val playClick = com.openingmind.LocalAudioPlayer.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            stringResource(R.string.settings_title), 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.dark_mode), color = MaterialTheme.colorScheme.onBackground)
            Switch(
                checked = isDark, 
                onCheckedChange = { 
                    playClick()
                    settingsViewModel.toggleDarkMode(it) 
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.app_language), color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = { 
                playClick()
                val nextLang = if (language == "in") "en" else "in"
                settingsViewModel.setLanguage(nextLang)
            }) {
                Text(
                    stringResource(R.string.lang_label), 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
