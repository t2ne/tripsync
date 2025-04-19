package com.example.tripsync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tripsync.database.AppDatabase
import com.example.tripsync.database.ImageDao
import com.example.tripsync.database.ImageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageDatabaseTest {
    private lateinit var imageDao: ImageDao
    private lateinit var db: AppDatabase

    // config da db em memória antes de cada teste
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        imageDao = db.imageDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetImage() = runBlocking {
        // tester para inserir e depois recuperar uma imagem
        val image = ImageEntity(id = "1", type = "profile", imagePath = "/data/imagem.jpg")
        imageDao.insertImage(image)

        val retrievedImage = imageDao.getImage("1", "profile")
        assertEquals(image.imagePath, retrievedImage?.imagePath)
    }

    @Test
    fun deleteImage() = runBlocking {
        // teste para exckuir uma imagem
        val image = ImageEntity(id = "2", type = "photo", imagePath = "/data/foto.jpg")
        imageDao.insertImage(image)

        imageDao.deleteImage("2", "photo")
        val retrievedImage = imageDao.getImage("2", "photo")
        assertNull(retrievedImage)
    }
}