package com.example.tripsync

import com.example.tripsync.database.ImageDao
import com.example.tripsync.database.ImageEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ImageDaoUnitTest {

    private lateinit var dao: ImageDao

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
    }

    @Test
    fun testInsertImage() = runBlocking {
        val image = ImageEntity("id1", "path.jpg", "viagem1")
        coEvery { dao.insertImage(image) } just Runs

        dao.insertImage(image)

        coVerify { dao.insertImage(image) }
    }
}
