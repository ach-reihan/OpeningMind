package com.openingmind

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openingmind.presentation.RepertoireViewModel
import com.openingmind.presentation.SettingsViewModel
import com.openingmind.presentation.screens.DetailScreen
import com.openingmind.presentation.screens.FormScreen
import com.openingmind.presentation.screens.MainScreen
import com.openingmind.presentation.screens.OnboardingScreen
import com.openingmind.presentation.theme.OpeningMindTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkPreferred by settingsViewModel.isDarkMode.collectAsState()
            val language by settingsViewModel.language.collectAsState()

            // Create a localized context for immediate UI updates
            val context = LocalContext.current
            val localizedContext = remember(language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                val configuration = context.resources.configuration
                val newConfig = Configuration(configuration)
                newConfig.setLocale(locale)
                context.createConfigurationContext(newConfig)
            }

            // Initialize viewModel here where LocalContext is still the Activity context
            val viewModel: RepertoireViewModel = hiltViewModel()

            CompositionLocalProvider(LocalContext provides localizedContext) {
                OpeningMindTheme(darkTheme = isDarkPreferred ?: androidx.compose.foundation.isSystemInDarkTheme()) {
                    OpeningMindAppNavigation(settingsViewModel, viewModel)
                }
            }
        }
    }
}

@Composable
fun OpeningMindAppNavigation(
    settingsViewModel: SettingsViewModel,
    viewModel: RepertoireViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                onNavigateToForm = { navController.navigate("form") }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(
                id = id,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("form") }
            )
        }

        composable("form") {
            FormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
