package com.example.palette

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import com.example.palette.ui.PaletteScreen
import com.example.palette.ui.PaletteTheme
import com.example.palette.util.HexUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaletteTheme {
                PaletteApp()
            }
        }
    }
}

@Composable
private fun PaletteApp() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var colorHexes by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                imageUriString = uri.toString()
                scope.launch {
                    val bitmap = loadBitmap(context, uri)
                    previewBitmap = bitmap
                    val hexes = bitmap?.let { HexUtils.extractDominantHexColors(it) }.orEmpty()
                    colorHexes = hexes
                }
            }
        }
    )

    LaunchedEffect(imageUriString) {
        if (imageUriString != null && previewBitmap == null) {
            scope.launch {
                previewBitmap = loadBitmap(context, Uri.parse(imageUriString))
            }
        }
    }

    PaletteScreen(
        imageBitmap = previewBitmap?.asImageBitmap(),
        colorHexes = colorHexes,
        onSelectPhoto = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onCopyHex = { hex ->
            copyToClipboard(context, hex)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.copied_message),
                    duration = SnackbarDuration.Short
                )
            }
        },
        snackbarHostState = snackbarHostState
    )
}

private suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    return@withContext try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("hex", text))
    Toast.makeText(context, context.getString(R.string.copied_message), Toast.LENGTH_SHORT).show()
}
