package com.example.palette.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.palette.R

@Composable
fun PaletteScreen(
    imageBitmap: ImageBitmap?,
    colorHexes: List<String>,
    onSelectPhoto: () -> Unit,
    onCopyHex: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(onClick = onSelectPhoto, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.select_photo))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (imageBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = stringResource(id = R.string.image_content_description),
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.placeholder_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (colorHexes.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.colors_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                colorHexes.forEach { hex ->
                    ColorRow(hex = hex, onCopyHex = onCopyHex)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ColorRow(hex: String, onCopyHex: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(hex)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = hex, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Button(onClick = { onCopyHex(hex) }) {
                Text(text = stringResource(id = R.string.copy))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaletteScreenPreview() {
    PaletteTheme {
        PaletteScreen(
            imageBitmap = null,
            colorHexes = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF"),
            onSelectPhoto = {},
            onCopyHex = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
