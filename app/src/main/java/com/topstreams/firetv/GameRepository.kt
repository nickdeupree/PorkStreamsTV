package com.topstreams.firetv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log


enum class GameStatus {
    UPCOMING,
    PREGAME,
    LIVE,
    FINAL
}

class GameRepository {
    private val client = OkHttpClient()
    
    companion object {
        private const val BASE_URL = "https://topstreams.info/nba/warriors"
        private val UTC_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    suspend fun getUpcomingGames(): List<Game> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(BASE_URL)
                .build()
                
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext getDummyGames()
            
            // Parse HTML using JSoup
            val doc = Jsoup.parse(html)
            val upcomingGames = doc.select(".item.upcoming")
            
            upcomingGames.map { gameElement ->
                parseGameElement(gameElement)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getDummyGames()
        }
    }
    
    private fun parseGameElement(element: Element): Game {
        val gameId = element.attr("data-id")
        
        // Parse team information
        val awayTeamElement = element.select(".away-content .text")
        val homeTeamElement = element.select(".home-content .text")
        
        val awayTeamAbbrev = awayTeamElement.select(".code").text()
        val homeTeamAbbrev = homeTeamElement.select(".code").text()
        
        val awayTeamName = awayTeamElement.select(".name").text()
        val homeTeamName = homeTeamElement.select(".name").text()
        
        val awayScore = awayTeamElement.select(".score").text().toIntOrNull() ?: 0
        val homeScore = homeTeamElement.select(".score").text().toIntOrNull() ?: 0
        
        // Parse game status
        val statusElement = element.select(".liveevent-desc .sub-name")
        
        // Parse date/time
        val dateElement = element.select(".game-desc .date")
        val dateId = dateElement.attr("id")
        val dateScript = element.select("script").find { it.html().contains(dateId) }
        val gameDate = if (dateScript != null) {
            val dateStr = dateScript.html()
                .substringAfter("moment('")
                .substringBefore("')")
            try {
                // Parse the UTC date
                val utcDate = UTC_FORMAT.parse(dateStr)
                // Convert to local timezone
                val localCalendar = Calendar.getInstance().apply {
                    time = utcDate ?: Date()
                    timeZone = TimeZone.getDefault()
                }
                localCalendar.time
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

        // Determine game status based on time comparison
        val currentTime = Calendar.getInstance().time
        val gameStartTime = gameDate
        
        val twoHoursThirtyMinsInMillis = 2 * 60 * 60 * 1000 + 30 * 60 * 1000
        val gameEndTime = Date(gameStartTime.time + twoHoursThirtyMinsInMillis)

        Log.d("GameRepository", "Game Start Time: $gameStartTime")
        Log.d("GameRepository", "Game End Time: $gameEndTime")
        Log.d("GameRepository", "Current Time: $currentTime")
        Log.d("GameRepository", "Game Status: ${statusElement.text()}")
        
        
        val gameStatus = when {
            currentTime.before(gameStartTime) -> GameStatus.UPCOMING
            currentTime.after(gameEndTime) -> GameStatus.FINAL
            else -> GameStatus.LIVE  // Current time is between game start and end
        }
        
        return Game(
            gameId = gameId,
            homeTeam = homeTeamName.lowercase(),
            awayTeam = awayTeamName.lowercase(),
            homeTeamFullName = homeTeamName,
            awayTeamFullName = awayTeamName,
            homeTeamAbbrev = homeTeamAbbrev,
            awayTeamAbbrev = awayTeamAbbrev,
            homeScore = homeScore,
            awayScore = awayScore,
            gameTime = gameDate,
            gameStatus = gameStatus
        )
    }
    
    private fun getDummyGames(): List<Game> {
        val calendar = Calendar.getInstance()
        
        // Create a game that started 1 hour ago (should be live)
        val liveGameTime = Calendar.getInstance()
        liveGameTime.add(Calendar.HOUR, -1)
        
        // Create a game starting in 2 hours (upcoming)
        val game2Time = Calendar.getInstance()
        game2Time.add(Calendar.HOUR, 2)
        
        // Create a game starting in 3 hours (upcoming)
        val game3Time = Calendar.getInstance()
        game3Time.add(Calendar.HOUR, 3)
        
        return listOf(
            Game(
                homeTeam = "timberwolves",
                awayTeam = "lakers",
                gameTime = liveGameTime.time,
                homeTeamFullName = "Timberwolves",
                awayTeamFullName = "Lakers",
                homeTeamAbbrev = "MIN",
                awayTeamAbbrev = "LAL"
            ),
            Game(
                homeTeam = "warriors",
                awayTeam = "suns",
                gameTime = game2Time.time,
                homeTeamFullName = "Warriors",
                awayTeamFullName = "Suns",
                homeTeamAbbrev = "GSW", 
                awayTeamAbbrev = "PHX"
            ),
            Game(
                homeTeam = "mavericks",
                awayTeam = "celtics",
                gameTime = game3Time.time,
                homeTeamFullName = "Mavericks",
                awayTeamFullName = "Celtics",
                homeTeamAbbrev = "DAL",
                awayTeamAbbrev = "BOS"
            )
        )
    }
}