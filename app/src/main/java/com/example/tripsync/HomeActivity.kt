package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var viagensListView: ListView
    private val viagens = mutableListOf<Viagem>()
    private lateinit var adapter: ViagemAdapter
    private lateinit var btnFiltros: Button
    private lateinit var spinnerFiltro: Spinner

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        viagensListView = findViewById(R.id.viagensListView)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        btnFiltros = findViewById(R.id.btnFiltros)

        // Verificar se o usuário está logado
        if (auth.currentUser == null) {
            // Redirecionar para login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Carregar viagens do usuário do Firestore
        carregarViagens()

        // Adapter para lista de viagens
        adapter = ViagemAdapter()
        viagensListView.adapter = adapter

        // Botão CRIAR abre a CriarViagemActivity
        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            val intent = Intent(this, CriarViagemActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_TRIP)
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
                    0 -> sortViagensCrescente()
                    1 -> sortViagensDecrescente()
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

    private fun carregarViagens() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .get()
            .addOnSuccessListener { documents ->
                viagens.clear()
                for (document in documents) {
                    val viagem = document.toObject(Viagem::class.java)
                    viagem.id = document.id
                    viagens.add(viagem)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar viagens: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun sortViagensCrescente() {
        viagens.sortBy { it.nome }
        adapter.notifyDataSetChanged()
    }

    private fun sortViagensDecrescente() {
        viagens.sortByDescending { it.nome }
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_TRIP && resultCode == RESULT_OK) {
            // Recarregar viagens após criar uma nova
            carregarViagens()
        }
    }

    inner class ViagemAdapter : BaseAdapter() {
        override fun getCount(): Int = viagens.size
        override fun getItem(position: Int): Any = viagens[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(R.layout.item_viagem, parent, false)
            val nomeViagem = view.findViewById<TextView>(R.id.nomeViagem)
            val btnEditar = view.findViewById<ImageButton>(R.id.btnEditar)
            val btnApagar = view.findViewById<ImageButton>(R.id.btnApagar)

            nomeViagem.text = viagens[position].nome

            // Botão Editar: abre EditarViagemActivity com nome da viagem
            btnEditar.setOnClickListener {
                val intent = Intent(this@HomeActivity, EditarViagemActivity::class.java)
                intent.putExtra("nomeViagem", viagens[position].nome)
                intent.putExtra("viagemId", viagens[position].id)
                startActivity(intent)
            }

            // Botão Apagar: mostra confirmação com AlertDialog
            btnApagar.setOnClickListener {
                val alertDialog = android.app.AlertDialog.Builder(this@HomeActivity)
                    .setMessage("TEM A CERTEZA QUE DESEJA ELEMINAR?")
                    .setPositiveButton("SIM") { dialog, _ ->
                        // Remover do Firestore
                        removerViagem(viagens[position].id)
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

    private fun removerViagem(id: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Viagem eliminada", Toast.LENGTH_SHORT).show()
                carregarViagens() // Recarregar a lista
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao eliminar viagem: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_CREATE_TRIP = 100
    }
}

// Modelo de dados para a Viagem
data class Viagem(
    var id: String = "",
    val nome: String = "",
    val data: String = "",
    val descricao: String = "",
    val classificacao: String = "",
    val fotoUrl: String = ""
)