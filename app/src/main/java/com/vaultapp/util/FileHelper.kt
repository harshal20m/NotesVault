package com.vaultapp.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileHelper {
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    fun isSizeValid(context: Context, uri: Uri, maxSizeMb: Int): Boolean {
        val sizeInBytes = getFileSize(context, uri)
        return sizeInBytes <= maxSizeMb * 1024 * 1024
    }
}
