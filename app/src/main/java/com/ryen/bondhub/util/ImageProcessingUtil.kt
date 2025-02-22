package com.ryen.bondhub.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageProcessingUtils {
    private const val COMPRESSION_QUALITY = 80
    private const val MAX_DIMENSION = 1024
    private const val THUMB_DIMENSION = 150

    data class ProcessedImages(
        val mainImage: ByteArray,
        val thumbnail: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ProcessedImages

            if (!mainImage.contentEquals(other.mainImage)) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = mainImage.contentHashCode()
            result = 31 * result + thumbnail.contentHashCode()
            return result
        }
    }

    suspend fun processProfileImage(
        context: Context,
        imageUri: Uri
    ): Result<ProcessedImages> = withContext(Dispatchers.IO) {
        try {
            // Load bitmap
            val originalBitmap = context.contentResolver
                .openInputStream(imageUri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: throw IllegalStateException("Failed to load image")

            // Process main image
            val mainBitmap = resizeImage(originalBitmap, MAX_DIMENSION)
            val mainBytes = compressImage(mainBitmap)

            // Create thumbnail
            val thumbBitmap = resizeImage(originalBitmap, THUMB_DIMENSION)
            val thumbBytes = compressImage(thumbBitmap)

            // Cleanup
            mainBitmap.recycle()
            thumbBitmap.recycle()
            originalBitmap.recycle()

            Result.success(ProcessedImages(mainBytes, thumbBytes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resizeImage(original: Bitmap, maxDimension: Int): Bitmap {
        val width = original.width
        val height = original.height

        if (width <= maxDimension && height <= maxDimension) {
            return Bitmap.createBitmap(original)
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }

    private fun compressImage(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream)
            stream.toByteArray()
        }
    }
}