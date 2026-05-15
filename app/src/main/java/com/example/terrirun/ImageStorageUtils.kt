package com.example.terrirun

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun saveProfileImageToInternalStorage(
    context: Context,
    imageUri: Uri,
    userId: String
): String {
    val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: return ""

    val file = File(context.filesDir, "profile_$userId.jpg")

    FileOutputStream(file).use { outputStream ->
        inputStream.copyTo(outputStream)
    }

    inputStream.close()

    return file.absolutePath
}