package com.ncwd.porkstreams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.navigation.AppRoutes
import com.ncwd.porkstreams.ui.components.IptvChannelCard
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.Background
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.TextSecondary
import com.ncwd.porkstreams.ui.viewmodel.IptvViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IptvScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val viewModel: IptvViewModel = viewModel(factory = IptvViewModel.Factory(container))
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentRed)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading channels...", color = TextSecondary)
                }
            }
            return@Column
        }

        if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load channels: ${state.error}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        if (state.groups.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(state.groups) { group ->
                    Button(
                        onClick = { viewModel.selectGroup(group) },
                        colors = ButtonDefaults.colors(
                            containerColor = if (state.selectedGroup == group) AccentRed else CardBackground
                        )
                    ) {
                        Text(group, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredChannels, key = { it.url }) { channel ->
                IptvChannelCard(
                    name = channel.name,
                    logoUrl = channel.logo,
                    onClick = { onNavigate(AppRoutes.player(channel.url)) }
                )
            }
        }
    }
}
