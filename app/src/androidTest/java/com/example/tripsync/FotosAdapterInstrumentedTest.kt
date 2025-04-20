package com.example.tripsync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tripsync.adapters.FotosAdapter
import com.example.tripsync.models.FotoViagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class FotosAdapterInstrumentedTest {

    private lateinit var context: Context
    private lateinit var testImageFile: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // criar uma imagem temp
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLUE)

        testImageFile = File(context.filesDir, "test_image.jpg")
        FileOutputStream(testImageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    }

    @Test
    // verifica se o adapter recebe a lista de fotos corretamente
    fun testFotoClick() {
        val foto = FotoViagem(
            id = "1",
            fotoUrl = testImageFile.absolutePath,
            descricao = "Foto de Lisboa",
            data = "2025-04-10",
            classificacao = "5",
            localId = "1",
            isDeleted = false
        )

        var clickedFoto: FotoViagem? = null
        val adapter = FotosAdapter(listOf(foto)) { clickedFoto = it }

        val viewHolder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performClick()

        assertEquals(foto, clickedFoto)
    }

    @Test
    // verifica se a imagem é exibida corretamente
    fun testImageDisplay() {
        val foto = FotoViagem(
            id = "1",
            fotoUrl = testImageFile.absolutePath,
            descricao = "Foto de Lisboa",
            data = "2025-04-10",
            classificacao = "5",
            localId = "1",
            isDeleted = false
        )

        val adapter = FotosAdapter(listOf(foto)) { }
        val viewHolder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(viewHolder, 0)

        // vê se o ImageView não é nulo
        assertNotNull("A imagem não foi carregada", viewHolder.imgFoto.drawable)
    }
}
