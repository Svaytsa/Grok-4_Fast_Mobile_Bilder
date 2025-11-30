package com.example.palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.palette.util.HexUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaletteScreen()
                }
            }
        }
    }
}

@Composable
fun PaletteScreen() {
    val context = LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var hexColors by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var bitmap by rememberSaveable { mutableStateOf<android.graphics.Bitmap?>(null) } // Not saveable directly, but we rebuild from URI if needed or just cache.
    // Actually, Bitmap is not Parcelable/Serializable effectively for state saving.
    // We should rely on imageUri to reload the bitmap if process death occurs,
    // but rememberSaveable only saves the URI and HEX list.
    // We need to trigger extraction again if bitmap is null but uri is not?
    // Or just store the hex list which IS saveable. The bitmap is for display.
    // If we rotate, we want to keep the bitmap. `remember` keeps it during rotation (configuration change) if not destroyed?
    // No, `remember` is reset on config change unless we use `rememberSaveable`.
    // Since we cannot easily save Bitmap, we will reload it from URI if it's missing but URI exists.

    // However, loading bitmap should be done in LaunchedEffect.

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            // Load bitmap and extract colors
            loadAndExtract(context, uri) { bmp, colors ->
                bitmap = bmp
                hexColors = colors
            }
        }
    }

    // Effect to reload bitmap if we have URI but no bitmap (e.g. after rotation if we didn't save bitmap)
    // But wait, if we rotate, `imageUri` is restored. `bitmap` is null.
    // We should reload.
    androidx.compose.runtime.LaunchedEffect(imageUri) {
        if (imageUri != null && bitmap == null) {
             loadAndExtract(context, imageUri!!) { bmp, colors ->
                bitmap = bmp
                // hexColors should be restored by rememberSaveable, so we might not need to re-extract
                // BUT the requirement says "State (URI, HEX list) survives rotation".
                // So if we already have hexColors (restored), we don't necessarily overwrite them,
                // but re-extracting guarantees consistency.
                if (hexColors.isEmpty()) {
                    hexColors = colors
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text(stringResource(R.string.select_image))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = stringResource(R.string.image_preview_desc),
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (hexColors.isNotEmpty()) {
            hexColors.forEach { hex ->
                ColorChip(hex = hex)
            }
        }
    }
}

@Composable
fun ColorChip(hex: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = hex, style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("HEX Color", hex)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(R.string.copy))
            }
        }
    }
}

fun loadAndExtract(context: Context, uri: Uri, onResult: (Bitmap, List<String>) -> Unit) {
    // Run in background
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            // Extract colors
            val colors = HexUtils.extractColors(bitmap)

            withContext(Dispatchers.Main) {
                onResult(bitmap, colors)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }
}
