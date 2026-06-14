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
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import android.media.SoundPool
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

import com.openingmind.presentation.RepertoireViewModel
import com.openingmind.presentation.SettingsViewModel
import com.openingmind.presentation.screens.DetailScreen
import com.openingmind.presentation.screens.FormScreen
import com.openingmind.presentation.screens.MainScreen
import com.openingmind.presentation.screens.OnboardingScreen
import com.openingmind.presentation.theme.OpeningMindTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

val LocalAudioPlayer = staticCompositionLocalOf<() -> Unit> { {} }

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
            val currentConfig = androidx.compose.ui.platform.LocalConfiguration.current
            val localizedContext = remember(language, currentConfig) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                val newConfig = Configuration(currentConfig)
                newConfig.setLocale(locale)
                context.createConfigurationContext(newConfig)
            }

            // Initialize viewModel here where LocalContext is still the Activity context
            val viewModel: RepertoireViewModel = hiltViewModel()

            // Initialize SoundPool globally
            val contextForSound = LocalContext.current
            val soundPool = remember { SoundPool.Builder().setMaxStreams(2).build() }
            val clickSoundId = remember { soundPool.load(contextForSound, R.raw.interface_click_sfx, 1) }

            DisposableEffect(Unit) {
                onDispose {
                    soundPool.release()
                }
            }

            val playClick: () -> Unit = {
                soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalAudioPlayer provides playClick
            ) {
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
    val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()

    NavHost(
        navController = navController, 
        startDestination = if (isOnboardingCompleted) "main" else "onboarding"
    ) {

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToMain = {
                    settingsViewModel.setOnboardingCompleted(true)
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
                onNavigateToDetail = { id, isRemote -> 
                    val route = if (isRemote) "detail/$id?isRemote=true" else "detail/$id"
                    navController.navigate(route) 
                },
                onNavigateToForm = { navController.navigate("form") }
            )
        }

        composable(
            route = "detail/{id}?isRemote={isRemote}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("isRemote") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            ),
            enterTransition = { scaleIn(initialScale = 0.9f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            exitTransition = { scaleOut(targetScale = 0.9f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { scaleIn(initialScale = 0.9f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { scaleOut(targetScale = 0.9f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val isRemote = backStackEntry.arguments?.getBoolean("isRemote") ?: false
            DetailScreen(
                id = id,
                isRemote = isRemote,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("form") }
            )
        }

        composable(
            route = "form",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
        ) {
            FormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
