package com.school.studentportal.shared.utils

import androidx.compose.runtime.Composable

interface BrowserLauncher {
    fun openUrl(url: String)
}

@Composable
expect fun rememberBrowserLauncher(): BrowserLauncher
