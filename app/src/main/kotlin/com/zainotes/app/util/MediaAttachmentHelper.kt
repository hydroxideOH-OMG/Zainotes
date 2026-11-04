/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.zainotes.app.data.model.BlockType
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImportedNoteMedia(
    val blockType: BlockType,
    val mediaUri: String,
    val displayName: String,
    val mimeType: String,
    val durationMs: Long,
    val sizeBytes: Long
)

object MediaAttachmentHelper {

    private val bitmapCache = object : LruCache<String, ImageBitmap>(36) {}

    /**
     * Imports a user-selected [Uri] into the app's persistent internal storage directory
     * (`filesDir/note_media`) when possible, or retains a persistable content URI fallback,
     * and extracts file metadata (display name, MIME type, file size, and audio/video duration).
     */
    suspend fun importMediaUri(
        context: Context,
        sourceUri: Uri,
        fallbackType: BlockType
    ): ImportedNoteMedia? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver

            // Best-effort persistable URI permission in case fallback URI is needed
            try {
                resolver.takePersistableUriPermission(
                    sourceUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some providers do not support persistable permissions; internal copy handles this.
            }

            val detectedMime = resolver.getType(sourceUri)?.lowercase(Locale.ROOT).orEmpty()
            val resolvedType = when {
                detectedMime.startsWith("image/") -> BlockType.IMAGE
                detectedMime.startsWith("audio/") -> BlockType.AUDIO
                detectedMime.startsWith("video/") -> BlockType.VIDEO
                else -> fallbackType
            }

            var displayName = ""
            var sizeBytes = 0L

            resolver.query(
                sourceUri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0 && !cursor.isNull(nameIdx)) {
                        displayName = cursor.getString(nameIdx).orEmpty()
                    }
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx).coerceAtLeast(0L)
                    }
                }
            }

            if (displayName.isBlank()) {
                val defaultPrefix = when (resolvedType) {
                    BlockType.IMAGE -> "Image"
                    BlockType.AUDIO -> "Audio"
                    BlockType.VIDEO -> "Video"
                    else -> "Attachment"
                }
                val defaultExt = when (resolvedType) {
                    BlockType.IMAGE -> "jpg"
                    BlockType.AUDIO -> "mp3"
                    BlockType.VIDEO -> "mp4"
                    else -> "bin"
                }
                displayName = "${defaultPrefix}_${System.currentTimeMillis() % 100000}.$defaultExt"
            }

            val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                .takeIf { it.length in 2..5 && it.all { ch -> ch.isLetterOrDigit() } }
                ?: when (resolvedType) {
                    BlockType.IMAGE -> "jpg"
                    BlockType.AUDIO -> "mp3"
                    BlockType.VIDEO -> "mp4"
                    else -> "dat"
                }

            val mediaDir = File(context.filesDir, "note_media").apply {
                if (!exists()) mkdirs()
            }
            val destFile = File(mediaDir, "media_${UUID.randomUUID()}.$extension")

            var finalUriString = sourceUri.toString()
            var copiedSuccessfully = false

            // Copy up to 120 MB into app-private storage so media is always accessible offline
            if (sizeBytes <= 120L * 1024L * 1024L) {
                try {
                    resolver.openInputStream(sourceUri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output, bufferSize = 16 * 1024)
                        }
                    }
                    if (destFile.exists() && destFile.length() > 0L) {
                        copiedSuccessfully = true
                        sizeBytes = destFile.length()
                        finalUriString = Uri.fromFile(destFile).toString()
                    }
                } catch (_: Exception) {
                    destFile.delete()
                }
            }

            val durationMs = if (resolvedType == BlockType.AUDIO || resolvedType == BlockType.VIDEO) {
                extractDurationMs(
                    context = context,
                    file = if (copiedSuccessfully) destFile else null,
                    uri = sourceUri
                )
            } else {
                0L
            }

            val finalMime = detectedMime.ifBlank {
                when (resolvedType) {
                    BlockType.IMAGE -> "image/jpeg"
                    BlockType.AUDIO -> "audio/mpeg"
                    BlockType.VIDEO -> "video/mp4"
                    else -> "application/octet-stream"
                }
            }

            ImportedNoteMedia(
                blockType = resolvedType,
                mediaUri = finalUriString,
                displayName = displayName,
                mimeType = finalMime,
                durationMs = durationMs,
                sizeBytes = sizeBytes
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDurationMs(
        context: Context,
        file: File?,
        uri: Uri
    ): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            if (file != null && file.exists()) {
                retriever.setDataSource(file.absolutePath)
            } else {
                retriever.setDataSource(context, uri)
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.coerceAtLeast(0L)
                ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Loads and caches a downsampled [ImageBitmap] for an image block.
     */
    suspend fun loadImageBitmap(
        context: Context,
        mediaUriString: String,
        maxDimensionPx: Int = 1440
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        if (mediaUriString.isBlank()) return@withContext null
        val cacheKey = "img_${mediaUriString}_$maxDimensionPx"
        bitmapCache.get(cacheKey)?.let { return@withContext it }

        try {
            val uri = Uri.parse(mediaUriString)
            val localFile = resolveLocalFile(uri, mediaUriString)

            // 1. Decode bounds
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            if (localFile != null && localFile.exists()) {
                BitmapFactory.decodeFile(localFile.absolutePath, boundsOptions)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@withContext null
            }

            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                maxDimension = maxDimensionPx
            )

            // 2. Decode downsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = if (localFile != null && localFile.exists()) {
                BitmapFactory.decodeFile(localFile.absolutePath, decodeOptions)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } ?: return@withContext null

            // 3. Apply EXIF rotation if needed
            val rotationDegrees = readExifRotationDegrees(context, localFile, uri)
            val finalBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(
                    rawBitmap,
                    0,
                    0,
                    rawBitmap.width,
                    rawBitmap.height,
                    matrix,
                    true
                )
            } else {
                rawBitmap
            }

            val imageBitmap = finalBitmap.asImageBitmap()
            bitmapCache.put(cacheKey, imageBitmap)
            imageBitmap
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts and caches a representative frame thumbnail [ImageBitmap] for a video block.
     */
    suspend fun loadVideoThumbnail(
        context: Context,
        mediaUriString: String,
        maxDimensionPx: Int = 960
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        if (mediaUriString.isBlank()) return@withContext null
        val cacheKey = "vid_${mediaUriString}_$maxDimensionPx"
        bitmapCache.get(cacheKey)?.let { return@withContext it }

        val retriever = MediaMetadataRetriever()
        try {
            val uri = Uri.parse(mediaUriString)
            val localFile = resolveLocalFile(uri, mediaUriString)
            if (localFile != null && localFile.exists()) {
                retriever.setDataSource(localFile.absolutePath)
            } else {
                retriever.setDataSource(context, uri)
            }

            val rawFrame = retriever.getFrameAtTime(
                1_000_000L, // 1s mark (or nearest keyframe)
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) ?: retriever.getFrameAtTime() ?: return@withContext null

            val maxSide = maxOf(rawFrame.width, rawFrame.height).coerceAtLeast(1)
            val scaledFrame = if (maxSide > maxDimensionPx) {
                val ratio = maxDimensionPx.toFloat() / maxSide.toFloat()
                val targetW = (rawFrame.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (rawFrame.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rawFrame, targetW, targetH, true)
            } else {
                rawFrame
            }

            val imageBitmap = scaledFrame.asImageBitmap()
            bitmapCache.put(cacheKey, imageBitmap)
            imageBitmap
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    fun resolveLocalFile(uri: Uri, rawUriString: String): File? {
        return when {
            uri.scheme == "file" && !uri.path.isNullOrBlank() -> File(uri.path!!)
            rawUriString.startsWith("/") -> File(rawUriString)
            else -> null
        }
    }

    private fun readExifRotationDegrees(
        context: Context,
        localFile: File?,
        uri: Uri
    ): Int {
        return try {
            val exif = if (localFile != null && localFile.exists()) {
                ExifInterface(localFile.absolutePath)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream)
                }
            } ?: return 0

            when (
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int
    ): Int {
        var inSampleSize = 1
        val longestSide = maxOf(width, height)
        if (longestSide > maxDimension) {
            val halfSide = longestSide / 2
            while ((halfSide / inSampleSize) >= maxDimension) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0L) return ""
        val kb = sizeBytes / 1024.0
        if (kb < 1024.0) {
            return String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
        }
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
