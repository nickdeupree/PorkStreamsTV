package com.ncwd.porkstreams.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PpvResponse(
    val success: Boolean,
    val timestamp: Long? = null,
    val streams: List<PpvCategory> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PpvCategory(
    val category: String,
    val id: Int,
    @FlexibleInt val always_live: Int = 0,
    val streams: List<PpvStream> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PpvStream(
    val id: Int,
    val name: String,
    val tag: String? = null,
    val poster: String? = null,
    val uri_name: String? = null,
    val starts_at: Long = 0L,
    val ends_at: Long = 0L,
    @FlexibleInt val always_live: Int = 0,
    val category_name: String,
    val iframe: String? = null,
    val allowpaststreams: Int = 0
) {
    val isLive: Boolean get() {
        val now = System.currentTimeMillis() / 1000
        return always_live == 1 || (starts_at > 0 && ends_at > 0 && now >= starts_at && now <= ends_at)
    }
    val isUpcoming: Boolean get() {
        val now = System.currentTimeMillis() / 1000
        return starts_at > now
    }
}
