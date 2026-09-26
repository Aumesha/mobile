package com.example.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DownloadsFolderHelper {

    /**
     * Saves a processed video (.mp4) or code file (.yml / .py) directly into the mobile's
     * public Downloads folder (Environment.DIRECTORY_DOWNLOADS).
     */
    suspend fun saveFileToDownloads(
        context: Context,
        sourceFile: File,
        desiredFileName: String,
        mimeType: String = "video/mp4"
    ): SavedDownloadResult = withContext(Dispatchers.IO) {
        try {
            if (!sourceFile.exists() || sourceFile.length() == 0L) {
                return@withContext SavedDownloadResult(
                    success = false,
                    displayPath = "",
                    uriString = "",
                    messageKn = "ಫೈಲ್ ಕಂಡುಬಂದಿಲ್ಲ (Source file missing)"
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, desiredFileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/DhvaniFlow")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri: Uri? = resolver.insert(collection, contentValues)

                if (itemUri != null) {
                    resolver.openOutputStream(itemUri)?.use { outStream ->
                        FileInputStream(sourceFile).use { inStream ->
                            inStream.copyTo(outStream, bufferSize = 64 * 1024)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)

                    val displayPath = "/storage/emulated/0/Download/DhvaniFlow/$desiredFileName"
                    return@withContext SavedDownloadResult(
                        success = true,
                        displayPath = displayPath,
                        uriString = itemUri.toString(),
                        messageKn = "ನೇರವಾಗಿ ನಿಮ್ಮ ಮೊಬೈಲ್ Downloads/DhvaniFlow ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ!"
                    )
                }
            }

            // Fallback for API 24-28 or if MediaStore insert returned null
            @Suppress("DEPRECATION")
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dhvaniDir = File(publicDownloads, "DhvaniFlow").apply { mkdirs() }
            val destFile = File(dhvaniDir, desiredFileName)

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(destFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            SavedDownloadResult(
                success = true,
                displayPath = destFile.absolutePath,
                uriString = Uri.fromFile(destFile).toString(),
                messageKn = "ನೇರವಾಗಿ ನಿಮ್ಮ ಮೊಬೈಲ್ Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ!"
            )
        } catch (e: Exception) {
            // Secondary safe fallback into app external downloads dir if storage restricted
            try {
                val appDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val fallbackFile = File(appDownloads, desiredFileName)
                sourceFile.copyTo(fallbackFile, overwrite = true)
                SavedDownloadResult(
                    success = true,
                    displayPath = fallbackFile.absolutePath,
                    uriString = Uri.fromFile(fallbackFile).toString(),
                    messageKn = "Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ: ${fallbackFile.name}"
                )
            } catch (inner: Exception) {
                SavedDownloadResult(
                    success = false,
                    displayPath = "",
                    uriString = "",
                    messageKn = "ಡೌನ್‌ಲೋಡ್ ದೋಷ: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    suspend fun saveTextFileToDownloads(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String = "text/plain"
    ): SavedDownloadResult = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeText(content)
        saveFileToDownloads(context, tempFile, fileName, mimeType)
    }

    fun shareOrOpenSavedUri(context: Context, uriString: String, mimeType: String = "video/mp4") {
        if (uriString.isBlank()) return
        try {
            val uri = Uri.parse(uriString)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(viewIntent, "ವಿಡಿಯೋ ತೆರೆಯಿರಿ / Open Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // Ignore if no external viewer installed
        }
    }
}
