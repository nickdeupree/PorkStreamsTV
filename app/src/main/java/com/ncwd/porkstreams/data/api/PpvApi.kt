package com.ncwd.porkstreams.data.api

import com.ncwd.porkstreams.data.model.PpvResponse
import retrofit2.http.GET

interface PpvApi {

    @GET("streams")
    suspend fun getStreams(): PpvResponse
}
