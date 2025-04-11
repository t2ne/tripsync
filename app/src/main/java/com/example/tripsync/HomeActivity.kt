package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var viagensListView: ListView
    private val viagens = listOf("Vila do Carmo", "Viana do Castelo", "Braga", "Ponte de Lima")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viagensListView = findViewById(R.id.viagensListView)
        val adapter = ViagemAdapter()
        viagensListView.adapter = adapter

        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            Toast.makeText(this, "Criar nova viagem", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnFiltros).setOnClickListener {
            Toast.makeText(this, "Abrir filtros", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ViagemAdapter : BaseAdapter() {
        override fun getCount(): Int = viagens.size
        override fun getItem(position: Int): Any = viagens[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
            val view = layoutInflater.inflate(R.layout.item_viagem, parent, false)
            val nomeViagem = view.findViewById<TextView>(R.id.nomeViagem)
            val btnEditar = view.findViewById<ImageButton>(R.id.btnEditar)
            val btnApagar = view.findViewById<ImageButton>(R.id.btnApagar)

            nomeViagem.text = viagens[position]

            btnEditar.setOnClickListener {
                Toast.makeText(this@HomeActivity, "Editar: ${viagens[position]}", Toast.LENGTH_SHORT).show()
            }

            btnApagar.setOnClickListener {
                Toast.makeText(this@HomeActivity, "Apagar: ${viagens[position]}", Toast.LENGTH_SHORT).show()
            }

            return view
        }
    }
}
