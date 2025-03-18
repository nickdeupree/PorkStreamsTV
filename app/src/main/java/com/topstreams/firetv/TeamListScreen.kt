package com.topstreams.firetv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler

@Composable
fun TeamListScreen(
    onTeamSelected: (NbaTeam) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Add BackHandler to handle back button presses
    BackHandler {
        onBackPressed()
    }
    
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(context) }
    val scope = rememberCoroutineScope()
    
    val teams by remember { mutableStateOf(NbaTeam.getAllTeams()) }
    var favoriteTeamIds by remember { mutableStateOf(setOf<String>()) }
    
    // Load favorite teams
    LaunchedEffect(Unit) {
        scope.launch {
            val favorites = favoritesRepository.getFavoriteTeams()
            favoriteTeamIds = favorites.map { it.id }.toSet()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("Back", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "NBA Teams",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Divider()
            
            // Team List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    TeamListItem(
                        team = team,
                        isFavorite = favoriteTeamIds.contains(team.id),
                        onTeamClick = { onTeamSelected(team) },
                        onFavoriteToggle = { 
                            scope.launch {
                                val isFavorite = favoritesRepository.toggleFavorite(team.id)
                                favoriteTeamIds = if (isFavorite) {
                                    favoriteTeamIds + team.id
                                } else {
                                    favoriteTeamIds - team.id
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamListItem(
    team: NbaTeam,
    isFavorite: Boolean,
    onTeamClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTeamClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = team.fullName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = team.city,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // Favorite icon button
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}