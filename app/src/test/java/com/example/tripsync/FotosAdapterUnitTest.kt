package com.example.tripsync

import com.example.tripsync.adapters.FotosAdapter
import com.example.tripsync.models.FotoViagem
import org.junit.Assert.assertEquals
import org.junit.Test

class FotosAdapterUnitTest {

    @Test
    fun testItemCount() {
        val fotos = listOf(
            FotoViagem("1", "/path/img1.jpg", "desc", "2024-01-01", "5", "1", false),
            FotoViagem("2", "/path/img2.jpg", "desc", "2024-01-02", "4", "1", false)
        )

        val adapter = FotosAdapter(fotos) {}
        assertEquals(2, adapter.itemCount)
    }
}
