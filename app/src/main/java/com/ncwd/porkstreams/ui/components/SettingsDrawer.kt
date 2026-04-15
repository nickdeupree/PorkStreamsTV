package com.ncwd.porkstreams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.providers.StreamUrlBuilder
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.NavBar
import com.ncwd.porkstreams.ui.theme.TextSecondary
import com.ncwd.porkstreams.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsDrawer(
    container: AppContainer,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(container)
    )
    val settings by viewModel.state.collectAsState()

    val firstItemFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        firstItemFocus.requestFocus()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Drawer panel
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(NavBar)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PorkStreams",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Navigation
            // Text(
            //     text = "NAVIGATION",
            //     style = MaterialTheme.typography.labelSmall,
            //     color = TextSecondary,
            //     modifier = Modifier.padding(bottom = 4.dp)
            // )
            // NavItem("Home", AppRoutes.HOME, currentRoute, onNavigate, onClose, firstItemFocus)
            // NavItem("Sports", AppRoutes.SPORTS, currentRoute, onNavigate, onClose)
            // NavItem("IPTV", AppRoutes.IPTV, currentRoute, onNavigate, onClose)
            // NavItem("Search", AppRoutes.SEARCH, currentRoute, onNavigate, onClose)

            // Spacer(Modifier.height(24.dp))

            // Movies & TV provider
            Text(
                text = "MOVIES & TV PROVIDER",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            StreamUrlBuilder.movieProviders.forEach { provider ->
                ProviderButton(
                    label = provider.displayName,
                    selected = settings.movieProvider == provider.id,
                    onClick = { viewModel.setMovieProvider(provider.id) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sports provider
            Text(
                text = "SPORTS PROVIDER",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            StreamUrlBuilder.sportsProviders.forEach { provider ->
                ProviderButton(
                    label = provider.displayName,
                    selected = settings.sportsProvider == provider.id,
                    onClick = { viewModel.setSportsProvider(provider.id) }
                )
            }
        }

        // Scrim - clicking outside closes the drawer
        // (handled by BackHandler in MainActivity)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavItem(
    label: String,
    route: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    val isSelected = currentRoute == route
    val modifier = if (focusRequester != null) {
        Modifier.width(252.dp).focusRequester(focusRequester)
    } else {
        Modifier.width(252.dp)
    }
    Button(
        onClick = {
            onNavigate(route)
            onClose()
        },
        colors = ButtonDefaults.colors(
            containerColor = if (isSelected) AccentRed else CardBackground,
            focusedContainerColor = if (isSelected) AccentRed else Color(0xFF2A2A3C)
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProviderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = if (selected) AccentRed.copy(alpha = 0.3f) else CardBackground,
            focusedContainerColor = if (selected) AccentRed else Color(0xFF2A2A3C)
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
        modifier = Modifier.width(252.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            if (selected) Text("✓  ", color = AccentRed)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
