package com.openingmind.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openingmind.R
import com.openingmind.presentation.RepertoireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: RepertoireViewModel,
    onNavigateBack: () -> Unit
) {
    val eco by viewModel.formEco.collectAsState()
    val name by viewModel.formName.collectAsState()
    val notation by viewModel.formNotation.collectAsState()
    val desc by viewModel.formDescription.collectAsState()
    val editingId by viewModel.editingId.collectAsState()
    val playClick = com.openingmind.LocalAudioPlayer.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (editingId == null) stringResource(R.string.add_title) else stringResource(R.string.edit_title), 
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = eco, 
                onValueChange = { viewModel.formEco.value = it }, 
                label = { Text(stringResource(R.string.eco_hint)) }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            OutlinedTextField(
                value = name, 
                onValueChange = { viewModel.formName.value = it }, 
                label = { Text(stringResource(R.string.opening_name_hint)) }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            OutlinedTextField(
                value = notation, 
                onValueChange = { viewModel.formNotation.value = it }, 
                label = { Text(stringResource(R.string.notation_hint)) }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            OutlinedTextField(
                value = desc, 
                onValueChange = { viewModel.formDescription.value = it }, 
                label = { Text(stringResource(R.string.analysis_hint)) }, 
                modifier = Modifier.fillMaxWidth(), 
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    playClick()
                    viewModel.saveRepertoire()
                    onNavigateBack()
                },
                enabled = eco.isNotEmpty() && name.isNotEmpty() && notation.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
