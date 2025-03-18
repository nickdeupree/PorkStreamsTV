package com.topstreams.firetv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var longPressJob: Job? by remember { mutableStateOf(null) }
    var isLongPress by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                when (keyEvent.type) {
                    KeyEventType.KeyDown -> {
                        if (keyEvent.key == Key.Enter || keyEvent.key == Key.DirectionCenter) {
                            longPressJob = scope.launch {
                                delay(500) // Adjust the delay as needed
                                isLongPress = true
                                onFavoriteToggle()
                            }
                            true
                        } else {
                            false
                        }
                    }
                    KeyEventType.KeyUp -> {
                        if (keyEvent.key == Key.Enter || keyEvent.key == Key.DirectionCenter) {
                            longPressJob?.cancel()
                            if(!isLongPress) {
                                onTeamClick()
                            }
                            isLongPress = false
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
            .clickable(onClick = {
                if (!isLongPress) {
                    onTeamClick()
                }
            }),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) 
                MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
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
                    color = if (isFocused) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = team.city,
                    color = if (isFocused) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                
                if (isFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Long-press OK to ${if (isFavorite) "unfavorite" else "favorite"}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Favorite icon button
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) Color.Red else 
                       if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Request focus for the first item when the screen is displayed
    LaunchedEffect(team.id) {
        if (team == NbaTeam.getAllTeams().firstOrNull()) {
            focusRequester.requestFocus()
        }
    }
}