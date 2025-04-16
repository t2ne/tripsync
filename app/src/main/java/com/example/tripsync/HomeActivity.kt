package com.example.tripsync

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
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
            // Redirecionar para tela de login
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
        adapter = ViagemAdapter(viagens, { viagem ->
            // Abrir tela de edição quando clicar em uma viagem
            val intent = Intent(this, EditarViagemActivity::class.java)
            intent.putExtra("viagemId", viagem.id)
            intent.putExtra("nomeViagem", viagem.nome)
            startActivity(intent)
        }, { position ->
            compartilharViagem(position)
        })

        viagensRecyclerView.layoutManager = LinearLayoutManager(this)
        viagensRecyclerView.adapter = adapter

        // Configurar o ItemTouchHelper para suportar swipe
        val itemTouchHelper = ItemTouchHelper(SwipeCallback())
        itemTouchHelper.attachToRecyclerView(viagensRecyclerView)
    }

    private fun setupFiltros() {
        btnFiltros.setOnClickListener {
            val filtroOptions = arrayOf("Nome (Z-A)", "Nome (A-Z)", "Data (Mais Antiga)", "Data (Mais Recente)")
            val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtroOptions)
            adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFiltro.adapter = adapterFiltro

            // Alterna a visibilidade do spinner
            if (spinnerFiltro.visibility == View.GONE) {
                spinnerFiltro.visibility = View.VISIBLE
            } else {
                spinnerFiltro.visibility = View.GONE
            }
        }

        // Configurar o spinner para ordenar as viagens
        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortViagensByNomeCrescente()
                    1 -> sortViagensByNomeDecrescente()
                    2 -> sortViagensByDataAscendente()
                    3 -> sortViagensByDataDescendente()
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
                    val viagem = Viagem(
                        id = document.id,
                        nome = document.getString("nome") ?: "",
                        data = document.getString("data") ?: "",
                        descricao = document.getString("descricao") ?: "",
                        classificacao = document.getString("classificacao") ?: "",
                        fotoUrl = document.getString("fotoUrl") ?: ""
                    )
                    viagens.add(viagem)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar viagens: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sortViagensByNomeCrescente() {
        viagens.sortBy { it.nome }
        adapter.notifyDataSetChanged()
    }

    private fun sortViagensByNomeDecrescente() {
        viagens.sortByDescending { it.nome }
        adapter.notifyDataSetChanged()
    }

    private fun sortViagensByDataAscendente() {
        // Separar viagens com data e sem data
        val viagensComData = viagens.filter { it.data.isNotEmpty() }
        val viagensSemData = viagens.filter { it.data.isEmpty() }

        // Ordenar apenas as viagens com data
        val viagensOrdenadasPorData = viagensComData.sortedBy { it.data }

        // Limpar a lista atual e adicionar na ordem desejada
        viagens.clear()
        viagens.addAll(viagensOrdenadasPorData)
        viagens.addAll(viagensSemData)

        adapter.notifyDataSetChanged()
    }

    private fun sortViagensByDataDescendente() {
        // Separar viagens com data e sem data
        val viagensComData = viagens.filter { it.data.isNotEmpty() }
        val viagensSemData = viagens.filter { it.data.isEmpty() }

        // Ordenar apenas as viagens com data
        val viagensOrdenadasPorData = viagensComData.sortedByDescending { it.data }

        // Limpar a lista atual e adicionar na ordem desejada
        viagens.clear()
        viagens.addAll(viagensOrdenadasPorData)
        viagens.addAll(viagensSemData)

        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_TRIP && resultCode == RESULT_OK) {
            // Recarregar viagens após criar uma nova
            carregarViagens()
        }
    }

    private fun compartilharViagem(position: Int) {
        val viagem = viagens[position]
        val shareText = "Olá! Quero compartilhar esta viagem contigo:\n" +
                "Nome: ${viagem.nome}\n" +
                "Data: ${viagem.data}\n" +
                "Classificação: ${viagem.classificacao}\n" +
                "Descrição: ${viagem.descricao}\n" +
                "Partilhado através do TripSync!"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
    }

    inner class SwipeCallback : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        private val deleteIcon = getDrawable(R.drawable.ic_delete)
        private val shareIcon = getDrawable(R.drawable.ic_share)
        private val deleteBackground = ColorDrawable(Color.RED)
        private val shareBackground = ColorDrawable(Color.rgb(0, 150, 0)) // Verde

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            when (direction) {
                ItemTouchHelper.LEFT -> {
                    // Excluir viagem
                    val viagemParaExcluir = viagens[position]
                    excluirViagem(viagemParaExcluir, position)
                }
                ItemTouchHelper.RIGHT -> {
                    // Compartilhar viagem
                    compartilharViagem(position)
                    adapter.notifyItemChanged(position) // Restaurar item após compartilhar
                }
            }
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
            val itemHeight = itemView.height

            // Limpar canvas para evitar sobreposição de fundo
            if (dX == 0f) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            if (dX < 0) { // Deslizando para a esquerda (excluir)
                // Desenhar fundo vermelho
                deleteBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteBackground.draw(c)

                // Desenhar ícone de lixeira
                val iconMargin = (itemHeight - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (deleteIcon?.intrinsicHeight ?: 0)
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - (deleteIcon?.intrinsicWidth ?: 0)

                deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon?.draw(c)
            } else if (dX > 0) { // Deslizando para a direita (compartilhar)
                // Desenhar fundo verde
                shareBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                shareBackground.draw(c)

                // Desenhar ícone de compartilhamento
                val iconMargin = (itemHeight - (shareIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (shareIcon?.intrinsicHeight ?: 0)
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + (shareIcon?.intrinsicWidth ?: 0)

                shareIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                shareIcon?.draw(c)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun excluirViagem(viagem: Viagem, position: Int) {
        // Mostrar diálogo de confirmação
        AlertDialog.Builder(this)
            .setTitle("Excluir Viagem")
            .setMessage("Deseja realmente excluir a viagem '${viagem.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                val userId = auth.currentUser?.uid ?: return@setPositiveButton

                db.collection("usuarios")
                    .document(userId)
                    .collection("viagens")
                    .document(viagem.id)
                    .delete()
                    .addOnSuccessListener {
                        // Remover da lista e notificar adapter
                        val removedViagem = viagens.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        // Mostrar opção de desfazer
                        Snackbar.make(viagensRecyclerView, "Viagem excluída", Snackbar.LENGTH_LONG)
                            .setAction("DESFAZER") {
                                // Restaurar a viagem no Firestore
                                val viagemData = hashMapOf(
                                    "nome" to removedViagem.nome,
                                    "data" to removedViagem.data,
                                    "descricao" to removedViagem.descricao,
                                    "classificacao" to removedViagem.classificacao,
                                    "fotoUrl" to removedViagem.fotoUrl
                                )

                                db.collection("usuarios")
                                    .document(userId)
                                    .collection("viagens")
                                    .add(viagemData)
                                    .addOnSuccessListener { documentReference ->
                                        removedViagem.id = documentReference.id
                                        viagens.add(position, removedViagem)
                                        adapter.notifyItemInserted(position)
                                    }
                            }
                            .show()
                    }
                    .addOnFailureListener { e ->
                        // Informar erro e manter item na lista
                        Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(position)
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                // Cancelar exclusão e restaurar item
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        private const val REQUEST_CREATE_TRIP = 100
    }
}