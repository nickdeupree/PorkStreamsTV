package com.topstreams.firetv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun GameSelectorScreen(
    onGameSelected: (Game) -> Unit,
    onTeamSelected: (NbaTeam) -> Unit,
    onViewAllTeams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository() }
    val favoritesRepository = remember { FavoritesRepository(context) }
    var games by remember { mutableStateOf(emptyList<Game>()) }
    var favoriteTeams by remember { mutableStateOf(emptyList<NbaTeam>()) }
    val scope = rememberCoroutineScope()
    var selectedGameId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            games = gameRepository.getUpcomingGames()
            favoriteTeams = favoritesRepository.getFavoriteTeams()
            Log.d("GameSelectorScreen", "Games loaded: $games")
            Log.d("GameSelectorScreen", "Favorite teams loaded: ${favoriteTeams.size}")
        }
    }

    // Sort the games
    val sortedGames = games.sortedWith(compareByDescending<Game> { it.isLive }
        .thenBy {
            when (it.gameStatus) {
                GameStatus.LIVE -> 0
                GameStatus.PREGAME, GameStatus.UPCOMING -> 1
                GameStatus.FINAL -> 2
                else -> 3
            }
        })

    // Group the games
    val liveGames = sortedGames.filter { it.gameStatus == GameStatus.LIVE }
    val upcomingGames = sortedGames.filter { it.gameStatus == GameStatus.UPCOMING || it.gameStatus == GameStatus.PREGAME }
    val finalGames = sortedGames.filter { it.gameStatus == GameStatus.FINAL }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // View All Teams Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NBA Teams",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Button(
                        onClick = onViewAllTeams,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View all teams"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View All Teams")
                    }
                }
            }

            // Favorites Section
            if (favoriteTeams.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorite Teams",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(favoriteTeams) { team ->
                            FavoriteTeamItem(team = team, onClick = { onTeamSelected(team) })
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Live Games
            if (liveGames.isNotEmpty()) {
                item {
                    Text(
                        text = "Live",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                items(liveGames) { game ->
                    GameCard(
                        game = game,
                        isSelected = game.gameId == selectedGameId,
                        onClick = { 
                            selectedGameId = game.gameId
                            onGameSelected(game)
                        }
                    )
                }
            }

            // Upcoming Games
            if (upcomingGames.isNotEmpty()) {
                item {
                    Text(
                        text = "Upcoming",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                items(upcomingGames) { game ->
                    GameCard(
                        game = game,
                        isSelected = game.gameId == selectedGameId,
                        onClick = { 
                            selectedGameId = game.gameId
                            onGameSelected(game)
                        }
                    )
                }
            }

            // Final Games
            if (finalGames.isNotEmpty()) {
                item {
                    Text(
                        text = "Final",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                items(finalGames) { game ->
                    GameCard(
                        game = game,
                        isSelected = game.gameId == selectedGameId,
                        onClick = { 
                            selectedGameId = game.gameId
                            onGameSelected(game)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteTeamItem(team: NbaTeam, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = team.abbreviation,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = team.name,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun GameCard(game: Game, isSelected: Boolean, onClick: () -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5
    
    val cardColor = if (game.isLive) {
        if (isDarkTheme) AppColors.liveGameDark else AppColors.liveGameLight
    } else {
        if (isDarkTheme) AppColors.cardDark else AppColors.cardLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) 
                    Modifier.border(width = 2.dp, color = Color.Red, shape = RoundedCornerShape(12.dp))
                else 
                    Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = game.getMatchupTitle(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = game.getFormattedDate(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )

                    Text(
                        text = game.getFormattedTime(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = game.gameStatus.toString(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                if (game.isLive) {
                    Box(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}