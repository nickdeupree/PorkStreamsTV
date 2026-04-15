package com.ncwd.porkstreams.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.ui.screens.DetailScreen
import com.ncwd.porkstreams.ui.screens.HomeScreen
import com.ncwd.porkstreams.ui.screens.IptvScreen
import com.ncwd.porkstreams.ui.screens.MoviesScreen
import com.ncwd.porkstreams.ui.screens.PlayerScreen
import com.ncwd.porkstreams.ui.screens.SearchScreen
import com.ncwd.porkstreams.ui.screens.SportsScreen
import com.ncwd.porkstreams.ui.screens.TvScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    container: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoutes.MOVIES) {
            MoviesScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoutes.TV) {
            TvScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoutes.SPORTS) {
            SportsScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoutes.IPTV) {
            IptvScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoutes.SEARCH) {
            SearchScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(
                container = container,
                mediaType = mediaType,
                id = id,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PLAYER,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            PlayerScreen(
                container = container,
                encodedUrl = encodedUrl,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
