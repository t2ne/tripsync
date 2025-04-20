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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageDaoInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var imageDao: ImageDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        imageDao = db.imageDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertGetAndDeleteImage() = runBlocking {
        val image = ImageEntity(
            id = "test123",
            imagePath = "/storage/emulated/0/Download/image.jpg",
            type = "cover"
        )

        // insert
        imageDao.insertImage(image)

        // get
        val retrieved = imageDao.getImage("test123", "cover")

        // assert
        assertNotNull(retrieved)
        assertEquals(image.id, retrieved?.id)
        assertEquals(image.imagePath, retrieved?.imagePath)
        assertEquals(image.type, retrieved?.type)
        assertEquals(image.createdAt, retrieved?.createdAt)

        // rm
        imageDao.deleteImage("test123", "cover")

        // confirmar o delete
        val afterDelete = imageDao.getImage("test123", "cover")
        assertNull(afterDelete)
    }
}
