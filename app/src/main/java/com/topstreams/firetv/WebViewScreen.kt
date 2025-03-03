package com.topstreams.firetv

import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

@Composable
fun WebViewScreen(streamUrl: String, onBackToSelection: () -> Unit = {}) {
    BackHandler {
        onBackToSelection()
    }
    
    val isLoading = remember { mutableStateOf(true) }
    
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLoading.value) Color.White else Color.Transparent),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    // We need JavaScript for the streaming site to work - security risk is acknowledged
                    // but necessary for this streaming app
                    javaScriptEnabled = true
                    
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    // Additional settings to help with playback
                    blockNetworkImage = false
                    blockNetworkLoads = false
                    displayZoomControls = false
                    allowContentAccess = true
                    allowFileAccess = true
                    
                    // Removed deprecated databaseEnabled setting
                }
                
                // Common ad-related domains to block, but exclude streaming domains
                val adDomains = listOf(
                    "googleadservices", "googlesyndication", "g.doubleclick",
                    "googleads", "google-analytics", "googletagmanager",
                    "facebook", "scorecardresearch", "amazon-adsystem",
                    "adnxs", "casalemedia", "everesttech", "serving-sys",
                    "pubmatic", "doubleclick", "adroll", "taboola", 
                    "outbrain", "adserver", "bidswitch", "adtech", 
                    "advertising", "clickbank", "clicksor"
                )
                
                // Streaming domains that should NOT be blocked
                val streamingDomains = listOf(
                    "akamaized.net", "m3u8", "mpd", "hls", "stream", 
                    "video", "media", "content", "cdn", "player",
                    "topstreams.info", "tstreams.info",
                    "nbalpng"  // Allow NBA streaming content
                )
                
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val url = request?.url?.toString() ?: return null
                        
                        // Always allow streaming content
                        if (url.contains(".m3u8") || url.contains(".mpd") || url.contains("stream")) {
                            Log.d("StreamingURL", "Allowing streaming URL: $url")
                            return null
                        }
                        
                        // Then allow known streaming domains
                        for (streamingDomain in streamingDomains) {
                            if (url.contains(streamingDomain, ignoreCase = true)) {
                                Log.d("StreamingURL", "Allowing streaming domain URL: $url")
                                return null
                            }
                        }
                        
                        // Block known ad domains
                        if (adDomains.any { ad -> url.contains(ad, ignoreCase = true) }) {
                            Log.d("AdBlock", "Blocked ad URL: $url")
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                ByteArrayInputStream("".toByteArray())
                            )
                        }
                        
                        // Allow everything else by default
                        return null
                    }
                    
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        
                        // Always allow streaming content
                        if (url.contains(".m3u8") || url.contains(".mpd") || url.contains("stream")) {
                            return false
                        }
                        
                        // Allow topstreams.info and known streaming domains
                        for (streamingDomain in streamingDomains) {
                            if (url.contains(streamingDomain, ignoreCase = true)) {
                                return false
                            }
                        }
                        
                        // Block all other redirects
                        Log.d("AdBlock", "Blocked redirect to: $url")
                        return true
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading.value = false
                        injectAdBlockingJS(view)
                        makeVideoFullscreen(view)
                        enableCORSForVideoContent(view)
                    }
                    
                    private fun injectAdBlockingJS(view: WebView?) {
                        val adBlockingJs = """
                            (function() {
                                // Function to remove ad elements
                                function removeAds() {
                                    // Common ad selectors
                                    const adSelectors = [
                                        '[class*="ad-"]', '[id*="ad-"]',
                                        '[class*="banner"]', '[id*="banner"]',
                                        'iframe:not([src*="topstreams"]):not([src*="akamaized"]):not([src*="player"]):not([src*="video"])',
                                        '[class*="popup"]', '[id*="popup"]',
                                        'a[target="_blank"]',
                                        'div[style*="position: fixed"]',
                                        'div[style*="z-index: 9999"]'
                                    ];
                                    
                                    // Remove elements that match ad selectors
                                    adSelectors.forEach(selector => {
                                        document.querySelectorAll(selector).forEach(el => {
                                            // Don't remove if it contains the main video player
                                            if (!el.querySelector('video') && 
                                                !el.querySelector('iframe[src*="topstreams"]') &&
                                                !el.querySelector('iframe[src*="player"]') &&
                                                !el.querySelector('iframe[src*="video"]') &&
                                                !el.querySelector('.video-js') &&
                                                !el.querySelector('.player-container')) {
                                                el.style.display = 'none';
                                            }
                                        });
                                    });
                                    
                                    // Remove all onclick attributes from elements
                                    document.querySelectorAll('*[onclick]').forEach(el => {
                                        el.removeAttribute('onclick');
                                    });
                                    
                                    // Block automatic redirects
                                    window._open = window.open;
                                    window.open = function() { 
                                        console.log('Popup blocked');
                                        return null; 
                                    };
                                    
                                    // Clean up timers that might trigger ads
                                    for (let i = 0; i < 1000; i++) {
                                        clearTimeout(i);
                                        clearInterval(i);
                                    }
                                }
                                
                                // Run ad removal immediately and periodically
                                removeAds();
                                setInterval(removeAds, 1000);
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(adBlockingJs, null)
                    }
                    
                    private fun enableCORSForVideoContent(view: WebView?) {
                        // JavaScript to handle CORS issues
                        val corsFixJs = """
                            (function() {
                                // Create a hook for fetch to modify CORS mode
                                const originalFetch = window.fetch;
                                window.fetch = function(url, options) {
                                    // Check if this is a streaming URL
                                    if (typeof url === 'string' && (
                                        url.includes('m3u8') || 
                                        url.includes('mpd') || 
                                        url.includes('akamaized') || 
                                        url.includes('stream') ||
                                        url.includes('hls') ||
                                        url.includes('video')
                                    )) {
                                        // Create new options with no-cors mode
                                        const newOptions = options || {};
                                        newOptions.mode = 'no-cors';
                                        console.log('Modified fetch with no-cors for:', url);
                                        return originalFetch(url, newOptions);
                                    }
                                    return originalFetch(url, options);
                                };
                                
                                // Force autoplay
                                setInterval(function() {
                                    const videos = document.querySelectorAll('video');
                                    if (videos.length > 0) {
                                        videos.forEach(function(video) {
                                            if (video.paused) {
                                                video.play().then(() => {
                                                    console.log('Video is now playing!');
                                                    // Make video full screen
                                                    video.style.position = 'fixed';
                                                    video.style.top = '0';
                                                    video.style.left = '0';
                                                    video.style.width = '100%';
                                                    video.style.height = '100%';
                                                    video.style.zIndex = '9999';
                                                }).catch(e => {
                                                    console.log('Auto-play still failed:', e);
                                                    // Try clicking any play buttons
                                                    document.querySelectorAll('.play-button, .vjs-big-play-button, [class*="play"]').forEach(btn => {
                                                        btn.click();
                                                    });
                                                });
                                            }
                                        });
                                    }
                                }, 1000);
                                
                                // Set up a MutationObserver to catch newly added videos or iframes
                                const observer = new MutationObserver(function(mutations) {
                                    mutations.forEach(function(mutation) {
                                        if (mutation.addedNodes && mutation.addedNodes.length > 0) {
                                            for (let i = 0; i < mutation.addedNodes.length; i++) {
                                                const node = mutation.addedNodes[i];
                                                if (node.tagName === 'VIDEO' || node.tagName === 'IFRAME') {
                                                    console.log('New video/iframe detected. Trying to play...');
                                                    setTimeout(function() {
                                                        if (node.tagName === 'VIDEO' && node.paused) {
                                                            node.play().catch(e => console.log('Could not autoplay new video:', e));
                                                        }
                                                    }, 1000);
                                                }
                                            }
                                        }
                                    });
                                });
                                observer.observe(document.body, {
                                    childList: true,
                                    subtree: true
                                });
                                
                                console.log('CORS and autoplay enhancement active');
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(corsFixJs, null)
                    }
                    
                    private fun makeVideoFullscreen(view: WebView?) {
                        // Inject JavaScript to hide everything except the video
                        val hideNonVideoElementsJs = """
                            (function() {
                                function attemptFullscreen() {
                                    // Keep only the video container visible
                                    var videoElements = document.querySelectorAll('video, iframe[src*="player"], iframe[src*="video"], .video-js, .player-container');
                                    if (videoElements.length > 0) {
                                        console.log('Video elements found:', videoElements.length);
                                        
                                        // First ensure the video container is visible and positioned correctly
                                        videoElements.forEach(function(video) {
                                            // Ensure the video stays in the document flow
                                            if (video.parentElement) {
                                                video.parentElement.style.display = 'block';
                                            }
                                            
                                            if (video.style) {
                                                video.style.position = 'fixed';
                                                video.style.top = '0';
                                                video.style.left = '0';
                                                video.style.width = '100%';
                                                video.style.height = '100%';
                                                video.style.zIndex = '9999';
                                                video.style.display = 'block';
                                            }
                                            
                                            // If video is in an iframe, make the iframe fullscreen
                                            if (video.tagName.toLowerCase() === 'iframe') {
                                                video.setAttribute('width', '100%');
                                                video.setAttribute('height', '100%');
                                            }
                                            
                                            // Try to start video playback
                                            if (video.tagName.toLowerCase() === 'video' && video.paused) {
                                                var playPromise = video.play();
                                                
                                                if (playPromise !== undefined) {
                                                    playPromise.then(function() {
                                                        console.log('The video has now been loaded!');
                                                    }).catch(function(error) {
                                                        console.log('Reloading!');
                                                        // Try again after a delay
                                                        setTimeout(function() {
                                                            video.play().catch(e => console.log('Auto-play failed after reload:', e));
                                                        }, 1000);
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        console.log('No video elements found. Will try again...');
                                    }
                                }
                                
                                // Run multiple times to make sure it catches the video
                                attemptFullscreen();
                                setTimeout(attemptFullscreen, 1000);
                                setTimeout(attemptFullscreen, 3000);
                                setTimeout(attemptFullscreen, 5000);
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(hideNonVideoElementsJs, null)
                    }
                }
                
                // Enable media playback
                webChromeClient = WebChromeClient()
                
                // Load the TopStreams website with the selected game
                loadUrl(streamUrl)
            }
        }
    )
}