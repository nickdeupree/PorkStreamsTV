package com.ncwd.porkstreams.data.api

import com.ncwd.porkstreams.data.model.CreditsResponse
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.data.model.MovieDetails
import com.ncwd.porkstreams.data.model.MultiSearchResult
import com.ncwd.porkstreams.data.model.TmdbPagedResponse
import com.ncwd.porkstreams.data.model.TvSeason
import com.ncwd.porkstreams.data.model.TvShow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<Movie>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<Movie>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<Movie>

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShow>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShow>

    @GET("tv/on_the_air")
    suspend fun getOnAirTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TvShow>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MultiSearchResult>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): MovieDetails

    @GET("tv/{series_id}")
    suspend fun getTvDetails(
        @Path("series_id") seriesId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): MovieDetails

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getTvSeason(
        @Path("series_id") seriesId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TvSeason

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): CreditsResponse

    @GET("tv/{series_id}/credits")
    suspend fun getTvCredits(
        @Path("series_id") seriesId: Int,
        @Query("api_key") apiKey: String
    ): CreditsResponse

    @GET("trending/tv/week")
    suspend fun getTrendingTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TvShow>

    @GET("trending/all/week")
    suspend fun getTrendingAll(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<Movie>
}
