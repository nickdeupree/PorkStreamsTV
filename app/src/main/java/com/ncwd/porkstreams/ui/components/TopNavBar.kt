package com.ncwd.porkstreams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.NavBar

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabRoutes = remember {
        listOf(AppRoutes.HOME, AppRoutes.MOVIES, AppRoutes.TV, AppRoutes.SPORTS, AppRoutes.IPTV, AppRoutes.SEARCH)
    }
    val tabRequesters = remember { tabRoutes.associateWith { FocusRequester() } }

    // When focus enters the Row from any direction, land on the currently selected tab.
    val selectedRequester = tabRequesters[currentRoute] ?: tabRequesters[AppRoutes.HOME]!!

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NavBar.copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .focusProperties { enter = { selectedRequester } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PorkStreams",
            style = MaterialTheme.typography.titleLarge,
            color = AccentRed,
            modifier = Modifier.padding(end = 32.dp)
        )

        NavTab("Home", AppRoutes.HOME, currentRoute, tabRequesters[AppRoutes.HOME]!!, onNavigate)
        Spacer(Modifier.width(4.dp))
        NavTab("Movies", AppRoutes.MOVIES, currentRoute, tabRequesters[AppRoutes.MOVIES]!!, onNavigate)
        Spacer(Modifier.width(4.dp))
        NavTab("TV", AppRoutes.TV, currentRoute, tabRequesters[AppRoutes.TV]!!, onNavigate)
        Spacer(Modifier.width(4.dp))
        NavTab("Sports", AppRoutes.SPORTS, currentRoute, tabRequesters[AppRoutes.SPORTS]!!, onNavigate)
        Spacer(Modifier.width(4.dp))
        NavTab("IPTV", AppRoutes.IPTV, currentRoute, tabRequesters[AppRoutes.IPTV]!!, onNavigate)
        Spacer(Modifier.width(4.dp))
        NavTab("Search", AppRoutes.SEARCH, currentRoute, tabRequesters[AppRoutes.SEARCH]!!, onNavigate)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.colors(containerColor = CardBackground),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text("\u21BB", style = MaterialTheme.typography.titleMedium)
        }

        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.colors(containerColor = CardBackground)
        ) {
            Text("⚙", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavTab(
    label: String,
    route: String,
    currentRoute: String,
    focusRequester: FocusRequester,
    onNavigate: (String) -> Unit
) {
    val selected = currentRoute == route
    Button(
        onClick = { onNavigate(route) },
        colors = ButtonDefaults.colors(
            containerColor = if (selected) AccentRed else CardBackground,
            focusedContainerColor = if (selected) AccentRed else CardBackground.copy(alpha = 0.8f)
        ),
        modifier = Modifier.focusRequester(focusRequester)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
    }
}
