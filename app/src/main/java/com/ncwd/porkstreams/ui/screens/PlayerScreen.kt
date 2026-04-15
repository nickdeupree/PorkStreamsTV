package com.ncwd.porkstreams.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.ncwd.porkstreams.data.local.AppContainer
import com.ncwd.porkstreams.data.local.NowPlaying
import com.ncwd.porkstreams.data.model.WatchProgress
import com.ncwd.porkstreams.navigation.AppRoutes
import kotlinx.coroutines.launch

private val AUTO_PLAY_JS = """
(function() {
    // Force page and all embeds to fill the full viewport
    var style = document.createElement('style');
    style.textContent = 'html,body{margin:0!important;padding:0!important;width:100%!important;height:100%!important;background:#000!important;overflow:hidden!important}' +
        'iframe,video,object,embed{position:fixed!important;top:0!important;left:0!important;width:100vw!important;height:100vh!important;border:none!important;margin:0!important;padding:0!important}';
    document.head.appendChild(style);
    // Play any visible <video> elements
    document.querySelectorAll('video').forEach(function(v) {
        v.play().catch(function(){});
    });
    // Focus the first iframe so D-pad events reach it
    var f = document.querySelector('iframe');
    if (f) { f.focus(); }
})();
""".trimIndent()

/** JS injected on back-press: tries to read currentTime/duration from any reachable video. */
private const val VIDEO_TIME_JS = """
(function() {
    var v = document.querySelector('video');
    if (v && v.duration > 0) { return v.currentTime + ',' + v.duration; }
    var frames = document.querySelectorAll('iframe');
    for (var i = 0; i < frames.length; i++) {
        try {
            var iv = frames[i].contentDocument.querySelector('video');
            if (iv && iv.duration > 0) { return iv.currentTime + ',' + iv.duration; }
        } catch(e) {}
    }
    return '0,0';
})()
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    container: AppContainer,
    encodedUrl: String,
    onBack: () -> Unit
) {
    val url = remember(encodedUrl) { AppRoutes.decodePlayerUrl(encodedUrl) }
    val scope = rememberCoroutineScope()
    val nowPlaying = remember { container.nowPlaying.value }

    // Save a "started" entry as soon as the player opens
    LaunchedEffect(Unit) {
        nowPlaying?.let { np -> container.watchProgressDao.upsert(np.toProgress()) }
    }

    var webViewRef: WebView? = null

    BackHandler {
        // Try to read actual video position via JS; fall back to 0/0 on failure
        val wv = webViewRef
        if (wv != null && nowPlaying != null) {
            wv.evaluateJavascript(VIDEO_TIME_JS) { result ->
                val cleaned = result?.trim('"') ?: "0,0"
                val parts = cleaned.split(",")
                val current = parts.getOrNull(0)?.toFloatOrNull() ?: 0f
                val duration = parts.getOrNull(1)?.toFloatOrNull() ?: 0f
                scope.launch {
                    container.watchProgressDao.upsert(
                        nowPlaying.toProgress(
                            progressSeconds = current,
                            durationSeconds = duration
                        )
                    )
                    container.nowPlaying.value = null
                    onBack()
                }
            }
        } else {
            scope.launch {
                nowPlaying?.let { container.watchProgressDao.upsert(it.toProgress()) }
                container.nowPlaying.value = null
                onBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowContentAccess = true
                        allowFileAccess = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportMultipleWindows(true)
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                    }
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            super.onPageFinished(view, finishedUrl)
                            view?.evaluateJavascript(AUTO_PLAY_JS, null)
                        }
                    }
                    webChromeClient = WebChromeClient()
                    requestFocus()
                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { webViewRef = it },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun NowPlaying.toProgress(
    progressSeconds: Float = 0f,
    durationSeconds: Float = 0f
) = WatchProgress(
    tmdbId = tmdbId,
    type = mediaType,
    title = title,
    posterPath = posterPath,
    progressSeconds = progressSeconds,
    durationSeconds = durationSeconds,
    season = season,
    episode = episode,
    lastUpdated = System.currentTimeMillis()
)
