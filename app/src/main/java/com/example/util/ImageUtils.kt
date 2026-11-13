package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {

    suspend fun encodeUriToBoundedDataUri(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val original = inputStream.use { BitmapFactory.decodeStream(it) } ?: return@withContext null

                val maxDim = 480
                val scale = minOf(
                    maxDim.toFloat() / original.width.coerceAtLeast(1),
                    maxDim.toFloat() / original.height.coerceAtLeast(1),
                    1f
                )
                val targetW = (original.width * scale).toInt().coerceAtLeast(1)
                val targetH = (original.height * scale).toInt().coerceAtLeast(1)
                val scaled = if (scale < 1f) {
                    Bitmap.createScaledBitmap(original, targetW, targetH, true)
                } else {
                    original
                }

                var quality = 70
                var bytes: ByteArray
                do {
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    bytes = out.toByteArray()
                    quality -= 15
                } while (bytes.size > 95_000 && quality >= 25)

                if (scaled !== original) scaled.recycle()
                original.recycle()

                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64"
            } catch (_: Exception) {
                null
            }
        }

    fun decodeDataUriToBitmap(dataUri: String): Bitmap? {
        return try {
            val base64Part = dataUri.substringAfter("base64,", "")
            if (base64Part.isBlank()) return null
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}
