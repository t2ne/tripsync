package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var viagensListView: ListView
    private val viagens = mutableListOf("Vila do Carmo", "Viana do Castelo", "Braga", "Ponte de Lima")
    private lateinit var adapter: ViagemAdapter
    private lateinit var btnFiltros: Button
    private lateinit var spinnerFiltro: Spinner // Declaração do Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viagensListView = findViewById(R.id.viagensListView)
        spinnerFiltro = findViewById(R.id.spinnerFiltro) // Atribuição do Spinner
        btnFiltros = findViewById(R.id.btnFiltros)

        // Adapter para lista de viagens
        adapter = ViagemAdapter()
        viagensListView.adapter = adapter

        // Botão CRIAR abre a CriarViagemActivity
        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            val intent = Intent(this, CriarViagemActivity::class.java)
            startActivity(intent)
        }

        // Botão FILTROS
        btnFiltros.setOnClickListener {
            val filtroOptions = arrayOf("Crescente", "Descrescente")
            val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtroOptions)
            adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFiltro.adapter = adapterFiltro

            // Alterna a visibilidade do spinner
            spinnerFiltro.visibility = if (spinnerFiltro.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Configurar o spinner para ordenar as viagens
        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortViagensCrescente() // Ordenar Crescente
                    1 -> sortViagensDecrescente() // Ordenar Decrescente
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Ícone de perfil abre EditarPerfilActivity
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sortViagensCrescente() {
        // Ordenar em ordem crescente (A-Z)
        viagens.sort()
        adapter.notifyDataSetChanged() // Notifica o adapter para atualizar a lista
    }

    private fun sortViagensDecrescente() {
        // Ordenar em ordem decrescente (Z-A)
        viagens.sortDescending()
        adapter.notifyDataSetChanged() // Notifica o adapter para atualizar a lista
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

            // Botão Editar: abre EditarViagemActivity com nome da viagem
            btnEditar.setOnClickListener {
                val intent = Intent(this@HomeActivity, EditarViagemActivity::class.java)
                intent.putExtra("nomeViagem", viagens[position])
                startActivity(intent)
            }

            // Botão Apagar: mostra confirmação com AlertDialog
            btnApagar.setOnClickListener {
                val alertDialog = android.app.AlertDialog.Builder(this@HomeActivity)
                    .setMessage("TEM A CERTEZA QUE DESEJA ELEMINAR?")
                    .setPositiveButton("SIM") { dialog, _ ->
                        viagens.removeAt(position)
                        notifyDataSetChanged()
                        Toast.makeText(this@HomeActivity, "Viagem eliminada", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("NÃO") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                alertDialog.show()

                // Estilo dos botões (opcional)
                alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setAllCaps(true)
                alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setAllCaps(true)
            }

            return view
        }
    }
}
