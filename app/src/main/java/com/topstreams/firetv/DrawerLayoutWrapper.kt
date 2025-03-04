package com.topstreams.firetv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch

/**
 * A wrapper component that handles drawer state and provides proper focus management
 * for TV remote controls when a drawer is opened and closed.
 */
@Composable
fun DrawerLayoutWrapper(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var previousFocusState by remember { mutableStateOf<FocusManagerState?>(null) }

    // Drawer state controls
    val isDrawerOpen = drawerState.isOpen
    
    // Open drawer and save focus state
    fun openDrawer() {
        previousFocusState = FocusManagerState(focusManager)
        scope.launch {
            drawerState.open()
        }
    }
    
    // Close drawer and restore focus
    fun closeDrawer() {
        scope.launch {
            drawerState.close()
            // Restore previous focus after drawer animation completes
            previousFocusState?.restoreFocus(focusManager)
            previousFocusState = null
        }
    }
    
    // Cleanup focus state when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            previousFocusState = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onCloseDrawer = ::closeDrawer
            )
        },
        gesturesEnabled = false // Disable gestures for TV interface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            content()
            
            // Example of how to add a button to open drawer
            // You can add this to your main UI where appropriate
            /*
            IconButton(
                onClick = ::openDrawer,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Open Menu")
            }
            */
        }
    }
}

/**
 * Helper class to save and restore focus state
 */
private class FocusManagerState(focusManager: FocusManager) {
    // In a real implementation, you might want to store more information
    // about the current focus state if needed
    
    fun restoreFocus(focusManager: FocusManager) {
        // Clear focus to reset the focus state
        focusManager.clearFocus(true)
        
        // Additional focus restoration logic could be added here
        // if you need to return focus to a specific composable
    }
}
