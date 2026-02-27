package com.example.android.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun FileViewerDialog(
    url: String,
    fileName: String, // e.g. "document.pdf"
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Box(Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                        .zIndex(1f)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fileName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }
                
                // Content
                FileViewerContent(url)
            }
        }
    }
}

@Composable
fun FileViewerContent(url: String) {
    val context = LocalContext.current
    var localFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Download logic
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                // Create a unique filename based on URL hash to allow caching
                val name = "view_${url.hashCode()}.${url.substringAfterLast('.', "tmp")}"
                val file = File(cacheDir, name)
                
                if (!file.exists()) {
                    URL(url).openStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                localFile = file
            } catch (e: Exception) {
                error = "Failed to load: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (error != null) {
            Text(error!!, color = Color(0xFFF57C00))
        } else if (localFile != null) {
            val extension = localFile!!.extension.lowercase()
            when {
                extension == "pdf" -> PdfViewer(localFile!!)
                extension in listOf("jpg", "jpeg", "png", "bmp") -> ImageViewer(localFile!!)
                else -> Text("Unsupported format: $extension", color = Color.White)
            }
        }
    }
}

@Composable
fun ImageViewer(file: File) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            bitmap = BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    if (bitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
        ) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun PdfViewer(file: File) {
    val renderer = remember(file) {
        val input = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(input)
    }
    val pageCount = renderer.pageCount
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for floating buttons if any
    ) {
        items(pageCount) { index ->
            PdfPage(renderer, index)
        }
    }
    
    DisposableEffect(file) {
        onDispose {
            try { renderer.close() } catch(e:Exception){}
        }
    }
}

@Composable
fun PdfPage(renderer: PdfRenderer, index: Int) {
    // Render page to bitmap
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(index) {
        withContext(Dispatchers.IO) {
            synchronized(renderer) {
                val page = renderer.openPage(index)
                val w = page.width * 2 // High res
                val h = page.height * 2
                val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap = bm
            }
        }
    }
    
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Page ${index + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Box(Modifier.fillMaxWidth().height(400.dp).background(Color.Gray), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}


