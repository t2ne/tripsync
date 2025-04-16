package com.example.tripsync

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Viagem(
    var id: String = "",
    val nome: String = "",
    val data: String = "",
    val descricao: String = "",
    val classificacao: String = "",
    val fotoUrl: String = ""
)

class HomeActivity : AppCompatActivity() {

    private lateinit var viagensRecyclerView: RecyclerView
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

        viagensRecyclerView = findViewById(R.id.viagensRecyclerView)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        btnFiltros = findViewById(R.id.btnFiltros)

        // Verificar se o usuário está logado
        if (auth.currentUser == null) {
            // Redirecionar para login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Configurar RecyclerView
        setupRecyclerView()

        // Carregar viagens do usuário do Firestore
        carregarViagens()

        // Botão CRIAR abre a CriarViagemActivity
        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            val intent = Intent(this, CriarViagemActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_TRIP)
        }

        // Botão FILTROS
        setupFiltros()

        // Ícone de perfil abre EditarPerfilActivity
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = ViagemAdapter(viagens) { viagem ->
            // Clique no item abre a tela de edição
            val intent = Intent(this@HomeActivity, EditarViagemActivity::class.java)
            intent.putExtra("nomeViagem", viagem.nome)
            intent.putExtra("viagemId", viagem.id)
            startActivity(intent)
        }

        viagensRecyclerView.layoutManager = LinearLayoutManager(this)
        viagensRecyclerView.adapter = adapter

        // Configurar o ItemTouchHelper para suportar swipe
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(viagensRecyclerView)
    }

    private fun setupFiltros() {
        btnFiltros.setOnClickListener {
            val filtroOptions = arrayOf("Crescente", "Descrescente")
            val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtroOptions)
            adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFiltro.adapter = adapterFiltro

            // Alterna a visibilidade do spinner
            if (spinnerFiltro.visibility == View.GONE) {
                spinnerFiltro.visibility = View.VISIBLE
                btnFiltros.text = "FILTROS v"
            } else {
                spinnerFiltro.visibility = View.GONE
                btnFiltros.text = "FILTROS +"
            }
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
                    val viagemMap = document.data
                    val viagem = Viagem(
                        id = document.id,
                        nome = viagemMap["nome"] as? String ?: "",
                        data = viagemMap["data"] as? String ?: "",
                        descricao = viagemMap["descricao"] as? String ?: "",
                        classificacao = viagemMap["classificacao"] as? String ?: "",
                        fotoUrl = viagemMap["fotoUrl"] as? String ?: ""
                    )
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

    private fun removerViagem(position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val viagemId = viagens[position].id
        val viagemRemovida = viagens[position]

        // Remover da lista local primeiro para atualização imediata da UI
        viagens.removeAt(position)
        adapter.notifyItemRemoved(position)

        // Remover do Firestore
        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .delete()
            .addOnSuccessListener {
                Snackbar.make(viagensRecyclerView, "Viagem removida com sucesso", Snackbar.LENGTH_LONG)
                    .setAction("DESFAZER") {
                        // Restaurar viagem na posição
                        viagens.add(position, viagemRemovida)
                        adapter.notifyItemInserted(position)

                        // Re-inserir no Firestore
                        db.collection("usuarios")
                            .document(userId)
                            .collection("viagens")
                            .document(viagemId)
                            .set(viagemRemovida)
                    }
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao remover viagem: ${e.message}", Toast.LENGTH_SHORT).show()
                // Restaurar na UI se falhar no Firestore
                viagens.add(position, viagemRemovida)
                adapter.notifyItemInserted(position)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_TRIP && resultCode == RESULT_OK) {
            // Recarregar viagens após criar uma nova
            carregarViagens()
        }
    }

    // Adapter do RecyclerView
    inner class ViagemAdapter(
        private val viagens: List<Viagem>,
        private val onItemClick: (Viagem) -> Unit
    ) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

        inner class ViagemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nomeViagem: TextView = view.findViewById(R.id.nomeViagem)
            val dataViagem: TextView = view.findViewById(R.id.dataViagem)
            val container: View = view.findViewById(R.id.itemContainer)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_viagem_swipe, parent, false)
            return ViagemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
            val viagem = viagens[position]

            holder.nomeViagem.text = viagem.nome
            holder.dataViagem.text = viagem.data

            // Configurar o clique no item para editar
            holder.container.setOnClickListener {
                onItemClick(viagem)
            }
        }

        override fun getItemCount() = viagens.size
    }

    // Implementação do callback para suportar swipe para excluir
    inner class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT
    ) {
        private val deleteIcon = getDrawable(R.drawable.ic_delete)
        private val background = ColorDrawable(Color.RED)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // Não suportamos arrastar/reordenar
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            // Confirmar antes de remover
            AlertDialog.Builder(this@HomeActivity)
                .setTitle("Remover Viagem")
                .setMessage("Tem certeza que deseja remover esta viagem?")
                .setPositiveButton("Sim") { _, _ ->
                    removerViagem(position)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    // Se cancelar, restaurar o item na posição
                    adapter.notifyItemChanged(position)
                }
                .show()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2

            // Desenhar fundo vermelho
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // Calcular posição do ícone
            deleteIcon?.let {
                val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + it.intrinsicHeight

                // Definir limites do ícone
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                it.draw(c)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    companion object {
        private const val REQUEST_CREATE_TRIP = 100
    }
}