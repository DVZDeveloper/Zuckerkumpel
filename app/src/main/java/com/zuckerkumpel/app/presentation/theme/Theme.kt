package com.zuckerkumpel.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme


@Composable
fun DynamicTheme(
    content: @Composable () -> Unit
) {

    val context = LocalContext.current
    val fallback = MaterialTheme.colorScheme
    val colorScheme = remember(context) {
        dynamicColorScheme(context)
    } ?: fallback


    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )

}