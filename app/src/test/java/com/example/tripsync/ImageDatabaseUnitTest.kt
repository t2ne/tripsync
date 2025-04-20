package com.example.tripsync

import com.example.tripsync.database.AppDatabase
import com.example.tripsync.database.ImageDao
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class ImageDatabaseUnitTest {

    @Test
    fun testImageDaoAccess() {
        val dao = mockk<ImageDao>()
        val db = mockk<AppDatabase>()

        every { db.imageDao() } returns dao

        val result = db.imageDao()
        assertNotNull(result)
    }
}
