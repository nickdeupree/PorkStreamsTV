package com.topstreams.firetv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onCloseDrawer: () -> Unit,
    isDrawerOpen: Boolean = true
) {
    // Only handle back button press when drawer is actually open
    if (isDrawerOpen) {
        BackHandler(onBack = onCloseDrawer)
    }
    
    val backgroundColor = if (isDarkTheme) AppColors.drawerBackgroundDark else AppColors.drawerBackgroundLight
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    // Create a focus requester to automatically focus the drawer when opened
    val focusRequester = remember { FocusRequester() }
    
    // Only request focus when drawer is actually open
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            // Only make the drawer focusable when it's actually open
            .then(
                if (isDrawerOpen) {
                    Modifier
                        .focusRequester(focusRequester)
                        .focusable()
                } else {
                    Modifier
                }
            ),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // App Logo and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.app_logo_trans),
                    contentDescription = "PorkStreams Logo",
                    modifier = Modifier
                        .size(48.dp),
                    tint = Color.Unspecified
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "PorkStreams",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(color = if (isDarkTheme) Color.DarkGray else Color.LightGray, thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Section
            Text(
                text = "Settings",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Theme Toggle - only make it interactive when drawer is open
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDrawerOpen) {
                            Modifier.clickable { onToggleTheme() }
                                .focusable()
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = if (isDarkTheme) Color.White else Color.Black
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                    color = textColor,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // App Version
            Text(
                text = "Version 1.5",
                color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}