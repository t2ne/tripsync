package com.example.tripsync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tripsync.database.AppDatabase
import com.example.tripsync.database.ImageDao
import com.example.tripsync.utils.ImageUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ImageUtilsInstrumentedTest {

    private lateinit var context: Context
    private lateinit var imageDao: ImageDao
    private lateinit var db: AppDatabase
    private lateinit var testImageFile: File
    private lateinit var testUri: Uri

    private val testId = "test-id"
    private val testType = "test-type"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = AppDatabase.getDatabase(context)
        imageDao = db.imageDao()

        // novo bitmap para teste
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawRGB(100, 200, 150)
        }

        // depois guardá-lo num ficheiro temporario
        testImageFile = File(context.filesDir, "test_image.jpg")
        FileOutputStream(testImageFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }

        // uri com base no ficheiro temporario (fileprovider)
        testUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            testImageFile
        )
    }

    @After
    // elimninar o teste e limpar a base de dados
    fun tearDown() {
        runBlocking {
            ImageUtils.deleteImage(context, testId, testType)
        }
        testImageFile.delete()
    }

    @Test
    fun testSaveAndRetrieveAndDeleteImage() = runBlocking {
        // save
        val savedPath = ImageUtils.saveImageToInternalStorage(context, testUri, testId, testType)
        Assert.assertTrue("A imagem tem que ser salva", File(savedPath).exists())

        // ir buscar
        val retrievedPath = ImageUtils.getImagePath(context, testId, testType)
        Assert.assertEquals("O path recebido tem que igualar o salvo", savedPath, retrievedPath)

        // URI from saved path
        val uriFromPath = ImageUtils.getImageUriFromPath(context, savedPath)
        Assert.assertNotEquals("URI do path não pode ser vazio", Uri.EMPTY, uriFromPath)

        // Delete
        ImageUtils.deleteImage(context, testId, testType)
        Assert.assertFalse("Imagem tem que ser eliminada", File(savedPath).exists())
        Assert.assertNull("ImageEntity tem que ser nula depois do delete", imageDao.getImage(testId, testType))
    }
}
