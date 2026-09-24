package com.example.familyledger.data.repository

import android.net.Uri
import com.example.familyledger.data.model.ImageType
import com.example.familyledger.data.model.RecognitionResult
import com.example.familyledger.data.remote.AiRecognitionService
import com.example.familyledger.domain.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val service: AiRecognitionService,
    private val compressor: ImageCompressor
) {
    suspend fun recognize(uri: Uri, type: ImageType): Result<RecognitionResult> =
        withContext(Dispatchers.IO) {
            try {
                val compressed = compressor.compress(uri, type).getOrElse { return@withContext Result.failure(it) }
                try {
                    val base64 = compressor.toBase64(compressed.file)
                    val result = service.recognize(base64, type.apiValue, type != ImageType.PRODUCT)
                    Result.success(result)
                } finally {
                    compressed.file.delete()
                }
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
}
