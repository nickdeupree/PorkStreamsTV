package com.ncwd.porkstreams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.ncwd.porkstreams.navigation.AppNavigation
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.LocalFirstContentFocus
import com.ncwd.porkstreams.ui.components.SettingsDrawer
import com.ncwd.porkstreams.ui.components.TopNavBar
import com.ncwd.porkstreams.ui.theme.PorkStreamsTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as PorkStreamsApplication

        setContent {
            PorkStreamsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.HOME

                    var drawerOpen by remember { mutableStateOf(false) }

                    val isPlayerScreen = currentRoute.startsWith("player/")
                    val firstContentFocusRequester = remember { FocusRequester() }

                    CompositionLocalProvider(LocalFirstContentFocus provides firstContentFocusRequester) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .focusProperties { canFocus = !drawerOpen }
                        ) {
                            // Top nav bar — hidden on player screen
                            if (!isPlayerScreen) {
                                TopNavBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(AppRoutes.HOME) { saveState = true }
                                        }
                                    },
                                    onOpenSettings = { drawerOpen = true },
                                    onRefresh = { app.container.refresh() },
                                    // Intercept D-pad DOWN: jump focus to first body card
                                    modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.DirectionDown &&
                                            keyEvent.type == KeyEventType.KeyDown) {
                                            try { firstContentFocusRequester.requestFocus() }
                                            catch (_: Exception) {}
                                            true
                                        } else false
                                    }
                                )
                            }

                            // Main content
                            AppNavigation(
                                navController = navController,
                                container = app.container
                            )
                        }

                        // Settings drawer slide-in.
                        // BackHandler is inside AnimatedVisibility so it is composed AFTER
                        // AppNavigation's NavHost back handling, giving it higher priority.
                        AnimatedVisibility(
                            visible = drawerOpen,
                            enter = slideInHorizontally { -it },
                            exit = slideOutHorizontally { -it }
                        ) {
                            BackHandler { drawerOpen = false }
                            SettingsDrawer(
                                container = app.container,
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(AppRoutes.HOME) { saveState = true }
                                    }
                                },
                                onClose = { drawerOpen = false }
                            )
                        }
                    }
                    } // CompositionLocalProvider
                }
            }
        }
    }
}
