package com.ncwd.porkstreams.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.focus.FocusRequester

/**
 * Shared FocusRequester pointing to the first focusable card in the current screen's body.
 * The TopNavBar requests this when D-pad DOWN is pressed so focus always enters the content.
 */
val LocalFirstContentFocus = compositionLocalOf { FocusRequester() }
