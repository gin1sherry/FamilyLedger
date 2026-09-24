package com.example.familyledger.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.familyledger.data.model.ImageType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

data class CompressedImage(
    val file: File,
    val width: Int,
    val height: Int,
    val quality: Int,
    val sizeBytes: Long
)

/**
 * 图片压缩管线 — PRD 4.2.4
 * EXIF 矫正 → 等比缩放 → JPEG → 质量阶梯至目标大小
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun compress(uri: Uri, type: ImageType): Result<CompressedImage> =
        withContext(Dispatchers.IO) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, type.longEdge * 2)
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                var bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                } ?: return@withContext Result.failure(IllegalStateException("无法读取图片"))

                bitmap = fixOrientation(uri, bitmap)
                bitmap = scaleToLongEdge(bitmap, type.longEdge)

                var quality = type.initialQuality
                val tmp = File(context.cacheDir, "compress_${System.currentTimeMillis()}.jpg")
                var bytes: ByteArray
                do {
                    ByteArrayOutputStream().use { baos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                        bytes = baos.toByteArray()
                    }
                    if (bytes.size <= type.targetKb * 1024 || quality <= 30) break
                    quality -= 10
                } while (quality >= 30)

                FileOutputStream(tmp).use { it.write(bytes) }
                if (bitmap != null && !bitmap.isRecycled) bitmap.recycle()

                Result.success(
                    CompressedImage(
                        file = tmp,
                        width = 0,
                        height = 0,
                        quality = quality,
                        sizeBytes = bytes.size.toLong()
                    )
                )
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    fun toBase64(file: File): String =
        Base64.getEncoder().encodeToString(file.readBytes())

    private fun calculateInSampleSize(w: Int, h: Int, req: Int): Int {
        var sample = 1
        var max = maxOf(w, h)
        while (max / 2 >= req) {
            sample *= 2
            max /= 2
        }
        return sample
    }

    private fun scaleToLongEdge(src: Bitmap, longEdge: Int): Bitmap {
        val max = maxOf(src.width, src.height)
        if (max <= longEdge) return src
        val scale = longEdge.toFloat() / max
        val nw = (src.width * scale).toInt().coerceAtLeast(1)
        val nh = (src.height * scale).toInt().coerceAtLeast(1)
        val out = Bitmap.createScaledBitmap(src, nw, nh, true)
        if (out != src) src.recycle()
        return out
    }

    private fun fixOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> return bitmap
                }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) bitmap.recycle()
                rotated
            } ?: bitmap
        } catch (_: Exception) {
            bitmap
        }
    }
}
