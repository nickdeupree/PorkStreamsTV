package com.ncwd.porkstreams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.ncwd.porkstreams.ui.theme.AccentRed
import com.ncwd.porkstreams.ui.theme.CardBackground
import com.ncwd.porkstreams.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    title: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    rating: Double? = null,
    onWatch: () -> Unit = {},
    onDetails: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val watchButtonFocus = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            try { watchButtonFocus.requestFocus() } catch (_: Exception) {}
        }
    }

    Card(
        onClick = { if (!isExpanded) isExpanded = true },
        modifier = modifier
            .width(160.dp)
            .height(240.dp)
            .onFocusChanged { if (!it.hasFocus) isExpanded = false }
            .onPreviewKeyEvent { keyEvent ->
                if (isExpanded && keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyDown) {
                    isExpanded = false
                    true
                } else false
            },
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = CardBackground,
            focusedContainerColor = CardBackground
        ),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient + title — always visible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (rating != null) {
                        Text(
                            text = "★ ${"%.1f".format(rating)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Focus outline — shown on hover, hidden when expanded
            if (isFocused && !isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, AccentRed, RoundedCornerShape(8.dp))
                )
            }

            // Expanded overlay — shown after click, buttons are auto-focused
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                isExpanded = false
                                onWatch()
                            },
                            colors = ButtonDefaults.colors(containerColor = AccentRed),
                            modifier = Modifier.focusRequester(watchButtonFocus)
                        ) {
                            Text("Watch", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                isExpanded = false
                                onDetails()
                            },
                            colors = ButtonDefaults.colors(containerColor = Color(0xFF333345))
                        ) {
                            Text("Details", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContinueWatchingCard(
    title: String,
    posterUrl: String?,
    progress: Float,
    modifier: Modifier = Modifier,
    onWatch: () -> Unit = {},
    onRestart: () -> Unit = {},
    onDetails: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val watchButtonFocus = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            try { watchButtonFocus.requestFocus() } catch (_: Exception) {}
        }
    }

    Card(
        onClick = { if (!isExpanded) isExpanded = true },
        modifier = modifier
            .width(160.dp)
            .height(240.dp)
            .onFocusChanged { if (!it.hasFocus) isExpanded = false }
            .onPreviewKeyEvent { keyEvent ->
                if (isExpanded && keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyDown) {
                    isExpanded = false
                    true
                } else false
            },
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(containerColor = CardBackground),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
            )

            // Progress bar background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x66FFFFFF))
            )
            // Progress bar fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .align(Alignment.BottomStart)
                    .background(AccentRed)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Focus outline — shown on hover, hidden when expanded
            if (isFocused && !isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, AccentRed, RoundedCornerShape(8.dp))
                )
            }

            // Expanded overlay — shown after click, buttons are auto-focused
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                isExpanded = false
                                onWatch()
                            },
                            colors = ButtonDefaults.colors(containerColor = AccentRed),
                            modifier = Modifier.focusRequester(watchButtonFocus)
                        ) {
                            Text("Continue", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                isExpanded = false
                                onRestart()
                            },
                            colors = ButtonDefaults.colors(containerColor = Color(0xFF1A1A2E))
                        ) {
                            Text("Restart", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                isExpanded = false
                                onDetails()
                            },
                            colors = ButtonDefaults.colors(containerColor = Color(0xFF333345))
                        ) {
                            Text("Details", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
