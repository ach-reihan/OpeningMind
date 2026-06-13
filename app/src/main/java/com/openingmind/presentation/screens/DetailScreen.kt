package com.openingmind.presentation.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openingmind.R
import com.openingmind.presentation.RepertoireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Int,
    isRemote: Boolean = false,
    viewModel: RepertoireViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val context = LocalContext.current
    val playClick = com.openingmind.LocalAudioPlayer.current
    val browserErrorMsg = stringResource(R.string.browser_error)
    
    val item by if (isRemote) {
        viewModel.selectedRemoteOpening.collectAsState()
    } else {
        val repertoires by viewModel.localRepertoires.collectAsState()
        remember(repertoires, id) { derivedStateOf { repertoires.find { it.id == id } } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.detail_title), 
                        color = MaterialTheme.colorScheme.onBackground 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        playClick()
                        onNavigateBack() 
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back), 
                            contentDescription = stringResource(R.string.back), 
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        item?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant, 
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.chess_board_placeholder), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "${it.ecoCode} - ${it.name}", 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.notation_label, it.notation), 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        it.description, 
                        fontSize = 16.sp, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isRemote) {
                    Button(
                        onClick = {
                            playClick()
                            // Format: "English Opening: Anglo-Indian Defense" 
                            // -> "English_Opening_Anglo-Indian_Defense"
                            val formattedName = it.name
                                .replace(":", "")
                                .replace(",", "")
                                .replace("'", "")
                                .replace(".", "")
                                .split(" ")
                                .filter { part -> part.isNotBlank() }
                                .joinToString("_")
                            
                            val url = "https://lichess.org/opening/$formattedName"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("DetailScreen", "Failed to open URL: $url", e)
                                Toast.makeText(context, browserErrorMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_explore), 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.open_in_browser))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                playClick()
                                viewModel.startEditing(it)
                                onNavigateToEdit()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit), 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.edit_button))
                        }
                        Button(
                            onClick = {
                                playClick()
                                viewModel.deleteRepertoire(it)
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete), 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_button))
                        }
                    }
                }
            }
        }
    }
}
