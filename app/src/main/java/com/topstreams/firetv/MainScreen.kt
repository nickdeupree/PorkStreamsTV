package com.topstreams.firetv

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    useWebViewer: Boolean,
    onToggleTheme: () -> Unit,
    onGameSelected: (Game) -> Unit
) {
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Focus requester for the main content
    val mainContentFocusRequester = remember { FocusRequester() }
    
    // Request focus on the main content when drawer is closed
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            mainContentFocusRequester.requestFocus()
        }
    }
    
    // Also request focus on main content at initial composition
    LaunchedEffect(Unit) {
        mainContentFocusRequester.requestFocus()
    }
    
    // Main layout with drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                isDrawerOpen = drawerState.currentValue == DrawerValue.Open
            )
        }
    ) {
        // Main content
        Scaffold(
            modifier = Modifier.focusRequester(mainContentFocusRequester),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.app_logo_trans),
                                contentDescription = "PorkStreams Logo",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PorkStreams",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            // Main content - Game selector
            GameSelectorScreen(
                onGameSelected = onGameSelected,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}