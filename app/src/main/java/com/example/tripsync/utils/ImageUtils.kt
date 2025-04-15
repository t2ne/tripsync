// app/src/main/java/com/example/tripsync/utils/ImageUtils.kt
package com.example.tripsync.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.tripsync.database.AppDatabase
import com.example.tripsync.database.ImageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    // Salvar imagem no armazenamento interno
    suspend fun saveImageToInternalStorage(
        context: Context,
        imageUri: Uri,
        id: String,
        type: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val filename = "$type-$id-${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Salvar caminho no banco Room
            val dao = AppDatabase.getDatabase(context).imageDao()
            dao.insertImage(ImageEntity(id, file.absolutePath, type))

            return@withContext file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    // Obter Uri para uma imagem a partir do caminho
    fun getImageUriFromPath(context: Context, path: String): Uri {
        val file = File(path)
        return if (file.exists()) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.EMPTY
        }
    }

    // Buscar caminho da imagem do Room
    suspend fun getImagePath(context: Context, id: String, type: String): String? = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).imageDao()
        return@withContext dao.getImage(id, type)?.imagePath
    }

    // Deletar imagem
    suspend fun deleteImage(context: Context, id: String, type: String) = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).imageDao()
        val image = dao.getImage(id, type)

        if (image != null) {
            val file = File(image.imagePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deleteImage(id, type)
        }
    }
}