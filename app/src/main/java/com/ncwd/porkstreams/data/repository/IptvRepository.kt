package com.ncwd.porkstreams.data.repository

import com.ncwd.porkstreams.data.iptv.M3uParser
import com.ncwd.porkstreams.data.model.IptvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class IptvRepository(private val okHttpClient: OkHttpClient) {

    private val m3uUrl = "https://iptv-org.github.io/iptv/countries/us.m3u"
    private var cached: List<IptvChannel>? = null

    suspend fun getChannels(): Result<List<IptvChannel>> = runCatching {
        cached ?: withContext(Dispatchers.IO) {
            val request = Request.Builder().url(m3uUrl).build()
            val body = okHttpClient.newCall(request).execute().use { response ->
                response.body?.string() ?: throw Exception("Empty IPTV response")
            }
            M3uParser.parse(body).also { cached = it }
        }
    }

    fun clearCache() { cached = null }
}
