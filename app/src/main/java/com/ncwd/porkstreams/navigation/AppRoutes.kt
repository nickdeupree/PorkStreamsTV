package com.ncwd.porkstreams.navigation

import java.net.URLDecoder
import java.net.URLEncoder

object AppRoutes {
    const val HOME = "home"
    const val MOVIES = "movies"
    const val TV = "tv"
    const val SPORTS = "sports"
    const val IPTV = "iptv"
    const val SEARCH = "search"

    const val DETAIL = "detail/{mediaType}/{id}"
    fun detail(mediaType: String, id: Int) = "detail/$mediaType/$id"

    const val TV_DETAIL = "tvdetail/{showId}"
    fun tvDetail(showId: Int) = "tvdetail/$showId"

    const val PLAYER = "player/{url}"
    fun player(url: String) = "player/${URLEncoder.encode(url, "UTF-8")}"
    fun decodePlayerUrl(encoded: String): String = URLDecoder.decode(encoded, "UTF-8")
}
