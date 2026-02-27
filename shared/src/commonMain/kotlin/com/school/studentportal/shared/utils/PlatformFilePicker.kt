package com.school.studentportal.shared.utils

import androidx.compose.runtime.Composable

expect class PlatformFile {
    val name: String
    suspend fun readBytes(): ByteArray
}

interface FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePicker(onResult: (PlatformFile?) -> Unit): FilePickerLauncher
