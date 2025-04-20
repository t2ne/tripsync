package com.example.tripsync

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tripsync.adapters.ViagemAdapter
import com.example.tripsync.models.Viagem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViagemAdapterInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testItemClick() {
        val viagem = Viagem(id = "1", nome = "Lisboa", data = "2025-04-10", descricao = "", classificacao = "", fotoUrl = "")
        var clickedViagem: Viagem? = null
        val adapter = ViagemAdapter(listOf(viagem), { clickedViagem = it }, { })

        // novo viewHolder e bind dos dados
        val viewHolder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(viewHolder, 0)

        // para simular o click
        viewHolder.itemView.performClick()

        // verify se o it é o mesmo
        assertEquals(viagem, clickedViagem)
    }

    @Test
    fun testShareButtonClick() {
        val viagem = Viagem(id = "1", nome = "Lisboa", data = "2025-04-10", descricao = "", classificacao = "", fotoUrl = "")
        var sharedPosition: Int? = null
        val adapter = ViagemAdapter(listOf(viagem), { }, { position -> sharedPosition = position })

        // again
        val viewHolder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(viewHolder, 0)

        viewHolder.btnShare.performClick()

        // vê se o indiice foi passado corretamente
        assertEquals(0, sharedPosition)
    }
}
