package com.ncwd.porkstreams.data.api

import com.ncwd.porkstreams.data.model.Match
import com.ncwd.porkstreams.data.model.Sport
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FootyApi {

    @GET("api/v1/sports")
    suspend fun getSports(): List<Sport>

    @GET("api/v1/matches/{sport}")
    suspend fun getMatchesBySport(
        @Path("sport") sport: String,
        @Query("date") date: String? = null
    ): List<Match>

    @GET("api/v1/matches/{sport}/live")
    suspend fun getLiveMatchesBySport(
        @Path("sport") sport: String
    ): List<Match>

    @GET("api/v1/matches/live")
    suspend fun getAllLiveMatches(): List<Match>

    @GET("api/v1/matches/all")
    suspend fun getAllMatches(
        @Query("date") date: String? = null
    ): List<Match>

    @GET("api/v1/matches/popular")
    suspend fun getPopularMatches(
        @Query("date") date: String? = null
    ): List<Match>

    @GET("api/v1/matches/popular/live")
    suspend fun getPopularLiveMatches(): List<Match>
}
