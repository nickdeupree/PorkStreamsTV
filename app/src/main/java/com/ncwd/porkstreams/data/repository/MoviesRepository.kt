package com.ncwd.porkstreams.data.repository

import com.ncwd.porkstreams.BuildConfig
import com.ncwd.porkstreams.data.api.TmdbApi
import com.ncwd.porkstreams.data.model.CreditsResponse
import com.ncwd.porkstreams.data.model.Movie
import com.ncwd.porkstreams.data.model.MovieDetails
import com.ncwd.porkstreams.data.model.MultiSearchResult
import com.ncwd.porkstreams.data.model.TvSeason
import com.ncwd.porkstreams.data.model.TvShow

class MoviesRepository(private val api: TmdbApi) {

    private val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun getTrendingMovies(): Result<List<Movie>> = runCatching {
        api.getTrendingMovies(apiKey).results
    }

    suspend fun getTopRatedMovies(): Result<List<Movie>> = runCatching {
        api.getTopRatedMovies(apiKey).results
    }

    suspend fun getPopularMovies(): Result<List<Movie>> = runCatching {
        api.getPopularMovies(apiKey).results
    }

    suspend fun getPopularTvShows(): Result<List<TvShow>> = runCatching {
        api.getPopularTvShows(apiKey).results
    }

    suspend fun getTopRatedTvShows(): Result<List<TvShow>> = runCatching {
        api.getTopRatedTvShows(apiKey).results
    }

    suspend fun getOnAirTvShows(): Result<List<TvShow>> = runCatching {
        api.getOnAirTvShows(apiKey).results
    }

    suspend fun getTrendingTvShows(): Result<List<TvShow>> = runCatching {
        api.getTrendingTvShows(apiKey).results
    }

    suspend fun searchMulti(query: String): Result<List<MultiSearchResult>> = runCatching {
        api.searchMulti(apiKey, query).results
    }

    suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = runCatching {
        api.getMovieDetails(movieId, apiKey)
    }

    suspend fun getTvDetails(seriesId: Int): Result<MovieDetails> = runCatching {
        api.getTvDetails(seriesId, apiKey)
    }

    suspend fun getTvSeason(seriesId: Int, seasonNumber: Int): Result<TvSeason> = runCatching {
        api.getTvSeason(seriesId, seasonNumber, apiKey)
    }

    suspend fun getMovieCredits(movieId: Int): Result<CreditsResponse> = runCatching {
        api.getMovieCredits(movieId, apiKey)
    }

    suspend fun getTvCredits(seriesId: Int): Result<CreditsResponse> = runCatching {
        api.getTvCredits(seriesId, apiKey)
    }
}
