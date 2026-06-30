package com.fixmystreet.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Drop-in replacement for AsyncImage that transparently handles:
 *  - Regular http/https URLs  → delegated to Coil's AsyncImage
 *  - Base64 images (prefix "b64:") → decoded in-memory and rendered as a Bitmap
 *  - Empty string → shows a grey placeholder
 */
@Composable
fun SmartAsyncImage(
    photoUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        photoUrl.startsWith("b64:") -> {
            // Decode Base64 → Bitmap → ImageBitmap
            val bitmap = remember(photoUrl) {
                try {
                    val raw = Base64.decode(photoUrl.removePrefix("b64:"), Base64.NO_WRAP)
                    BitmapFactory.decodeByteArray(raw, 0, raw.size)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                Box(modifier = modifier.background(Color.Gray))
            }
        }
        photoUrl.isNotEmpty() -> {
            // Normal http(s) URL — use Coil
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            // Empty — grey placeholder
            Box(modifier = modifier.background(Color.Gray))
        }
    }
}
