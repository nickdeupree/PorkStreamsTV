package com.topstreams.firetv

import java.text.SimpleDateFormat
import java.util.*

data class Game(
    val gameId: String = "",
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamFullName: String,
    val awayTeamFullName: String,
    val homeTeamAbbrev: String,
    val awayTeamAbbrev: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val gameTime: Date,
    val isLive: Boolean = false,
    val gameStatus: GameStatus = GameStatus.UPCOMING
) {
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return dateFormat.format(gameTime)
    }

    fun getFormattedTime(): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        return timeFormat.format(gameTime)
    }

    fun getMatchupTitle(): String {
        return "$awayTeamFullName @ $homeTeamFullName"
    }

    fun getStreamUrl(): String {
        // TopStreams uses the home team name in lowercase for the stream URL
        return "https://topstreams.info/nba/${homeTeam.lowercase()}"
    }
    
    // This function will be used to extract the direct stream URL
    suspend fun getDirectStreamUrl(): String? {
        // Default pattern for NBA streams on TopStreams
        val defaultStreamUrl = "https://nbalpng.akamaized.net/live-pz/a/hls-wv-sd/"
        
        // The actual extraction will happen in the VideoPlayerScreen
        // This is just a placeholder method that we'll expand on
        return null
    }

    fun isGameLive(): Boolean {
        val now = Date()
        val twoHoursThirtyMinutes = 2.5 * 60 * 60 * 1000 // in milliseconds
        
        // Game is live if current time is after game start time and within 2.5 hours
        return now.time >= gameTime.time && 
               now.time <= (gameTime.time + twoHoursThirtyMinutes)
    }

    fun getStatus(): String {
        return when {
            isGameLive() -> "LIVE"
            gameTime.after(Date()) -> "UPCOMING"
            else -> "FINISHED"
        }
    }
}