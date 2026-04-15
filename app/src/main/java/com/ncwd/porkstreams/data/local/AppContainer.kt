package com.ncwd.porkstreams.data.local

import android.content.Context
import com.ncwd.porkstreams.data.api.FootyApi
import com.ncwd.porkstreams.data.api.PpvApi
import com.ncwd.porkstreams.data.api.TmdbApi
import com.ncwd.porkstreams.data.repository.IptvRepository
import com.ncwd.porkstreams.data.repository.MoviesRepository
import com.ncwd.porkstreams.data.repository.SportsRepository
import com.ncwd.porkstreams.data.model.FlexibleIntAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/** Metadata set by any screen before navigating to PlayerScreen, cleared on exit. */
data class NowPlaying(
    val tmdbId: String,
    val mediaType: String,   // "movie" or "tv"
    val title: String,
    val posterPath: String?,
    val season: Int? = null,
    val episode: Int? = null
)

class AppContainer(context: Context) {

    val moshi: Moshi = Moshi.Builder()
        .add(FlexibleIntAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val httpCache = Cache(
        directory = File(context.cacheDir, "http_cache"),
        maxSize = 50L * 1024L * 1024L // 50 MB
    )

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(httpCache)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // Force successful API responses to be cached for 2 hours
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.isSuccessful) {
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "max-age=7200")
                    .build()
            } else {
                response
            }
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val tmdbRetrofit = retrofit("https://api.themoviedb.org/3/")
    private val footyRetrofit = retrofit("https://api.watchfooty.st/")

    // PPV base URLs tried in order — first success wins
    private val ppvApis: List<PpvApi> = listOf(
        "https://api.ppv.to/api/",
        "https://api.ppv.st/api/",
        "https://api.ppv.cx/api/",
        "https://old.ppv.to/api/"
    ).map { baseUrl -> retrofit(baseUrl).create(PpvApi::class.java) }

    val tmdbApi: TmdbApi = tmdbRetrofit.create(TmdbApi::class.java)
    val footyApi: FootyApi = footyRetrofit.create(FootyApi::class.java)

    val database: AppDatabase = AppDatabase.getInstance(context)
    val watchProgressDao: WatchProgressDao = database.watchProgressDao()
    val watchLaterDao: WatchLaterDao = database.watchLaterDao()
    val preferencesManager: PreferencesManager = PreferencesManager(context)

    val moviesRepository = MoviesRepository(tmdbApi)
    val sportsRepository = SportsRepository(footyApi, ppvApis)
    val iptvRepository = IptvRepository(okHttpClient)

    // Set by screens before navigating to the player; cleared by PlayerScreen on exit.
    val nowPlaying = MutableStateFlow<NowPlaying?>(null)

    // Refresh trigger — ViewModels collect this to reload their data
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val refreshTrigger: SharedFlow<Unit> = _refreshTrigger.asSharedFlow()

    fun refresh() {
        try { httpCache.evictAll() } catch (_: Exception) {}
        _refreshTrigger.tryEmit(Unit)
    }
}
