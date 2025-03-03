package com.topstreams.firetv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun GameSelectorScreen(onGameSelected: (Game) -> Unit) {
    val gameRepository = remember { GameRepository() }
    var games by remember { mutableStateOf(emptyList<Game>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            games = gameRepository.getUpcomingGames()
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
    val liveGames = sortedGames.filter { it.isLive }
    val upcomingGames = sortedGames.filter { it.gameStatus == GameStatus.UPCOMING || it.gameStatus == GameStatus.PREGAME }
    val finalGames = sortedGames.filter { it.gameStatus == GameStatus.FINAL }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1015))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PorkStreams",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Games
                if (liveGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Live",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    items(liveGames) { game ->
                        GameCard(game = game, onClick = { onGameSelected(game) })
                    }
                }

                // Upcoming Games
                if (upcomingGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    items(upcomingGames) { game ->
                        GameCard(game = game, onClick = { onGameSelected(game) })
                    }
                }

                // Final Games
                if (finalGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Final",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    items(finalGames) { game ->
                        GameCard(game = game, onClick = { onGameSelected(game) })
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (game.isLive) Color(0xFF8B0000) else Color(0xFF222632)
        )
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = game.getFormattedDate(),
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Text(
                        text = game.getFormattedTime(),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = game.gameStatus.toString(),
                        color = Color.LightGray,
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