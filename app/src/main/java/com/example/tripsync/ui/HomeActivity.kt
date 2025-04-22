package com.example.tripsync.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.adapters.ViagemAdapter
import com.example.tripsync.models.Viagem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        viagensRecyclerView = findViewById(R.id.viagensRecyclerView)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        btnFiltros = findViewById(R.id.btnFiltros)

        // logged in? então passa para a main
        if (auth.currentUser == null) {
            // redirect
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // setup recycler para as viagens
        setupRecyclerView()

        // load das viagens do user
        carregarViagens()

        // btn CRIAR + para abrir a CriarViagemActivity
        findViewById<Button>(R.id.btnCriar).setOnClickListener {
            val intent = Intent(this, CriarViagemActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_TRIP)
        }

        // btn FILTROS +
        setupFiltros()

        // Ícone de profile abre EditarPerfilActivity
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = ViagemAdapter(viagens, { viagem ->
            // tela de edição do edit viagem
            val intent = Intent(this, EditarViagemActivity::class.java)
            // putExtra: passar a viagem selecionada
            intent.putExtra("viagemId", viagem.id)
            intent.putExtra("nomeViagem", viagem.nome)
            startActivity(intent)
        }, { position: Int ->
            compartilharViagem(position)
        })

        //linear layout manager para o recycler view
        viagensRecyclerView.layoutManager = LinearLayoutManager(this)
        viagensRecyclerView.adapter = adapter

        // suporte para swipe
        val itemTouchHelper = ItemTouchHelper(SwipeCallback())
        itemTouchHelper.attachToRecyclerView(viagensRecyclerView)
    }

    private fun setupFiltros() {
        btnFiltros.setOnClickListener {
            // adicionar os filtros em si,
            val filtroOptions = arrayOf(getString(R.string.nome_z_a),
                getString(R.string.nome_a_z),
                getString(R.string.data_mais_recente), getString(R.string.data_mais_antiga))
            val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtroOptions)
            adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFiltro.adapter = adapterFiltro

            // muda a visi do spinner
            if (spinnerFiltro.isGone) {
                spinnerFiltro.visibility = View.VISIBLE
            } else {
                spinnerFiltro.visibility = View.GONE
            }
        }

        // config do spinner para mudar a ordem das viagens
        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortViagensByNomeCrescente()
                    1 -> sortViagensByNomeDecrescente()
                    2 -> sortViagensByDataAscendente()
                    3 -> sortViagensByDataDescendente()
                }
            }

            // "deselecionar" o spinner
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun carregarViagens() {
        val userId = auth.currentUser?.uid ?: return

        // ir buscar tudo á firestore dentro das collections
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
                // notificar o adapter que os dados mudaram
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_carregar_viagens) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // métodos para o sorting das viagens
    private fun sortViagensByNomeCrescente() {
        viagens.sortBy { it.nome }
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortViagensByNomeDecrescente() {
        viagens.sortByDescending { it.nome }
        adapter.notifyDataSetChanged()
    }

    private fun sortViagensByDataAscendente() {
        // separar com data e sem data
        val viagensComData = viagens.filter { it.data.isNotEmpty() }
        val viagensSemData = viagens.filter { it.data.isEmpty() }

        // dá order das viagens com data only
        val viagensOrdenadasPorData = viagensComData.sortedBy { it.data }

        // clean da lista atual e adicionar na ordem q quiser
        viagens.clear()
        viagens.addAll(viagensOrdenadasPorData)
        viagens.addAll(viagensSemData)

        adapter.notifyDataSetChanged()
    }

    // mm coisa mas para a data decrescente ctrl c+v
    private fun sortViagensByDataDescendente() {
        val viagensComData = viagens.filter { it.data.isNotEmpty() }
        val viagensSemData = viagens.filter { it.data.isEmpty() }

        val viagensOrdenadasPorData = viagensComData.sortedByDescending { it.data }

        viagens.clear()
        viagens.addAll(viagensOrdenadasPorData)
        viagens.addAll(viagensSemData)

        adapter.notifyDataSetChanged()
    }

    // metodo para o resultado da activity de criar viagem
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_TRIP && resultCode == RESULT_OK) {
            // recarregar as trips após criar uma nova
            carregarViagens()
        }
    }

    // metodo para compartilhar a viagem por text, fixed rn
    private fun compartilharViagem(position: Int) {
        val viagem = viagens[position]
        val shareText = getString(R.string.quero_compartilhar) +
                getString(R.string.nome_2pontos) + " ${viagem.nome}\n" +
                getString(R.string.data_2pontos) + " ${viagem.data}\n" +
                getString(R.string.classificacao_2pontos) + " ${viagem.classificacao}\n" +
                getString(R.string.descricao_2pontos) + " ${viagem.descricao}\n" +
                getString(R.string.partilhado_atraves)

        // intent para o share, ty 4 stackoverflow
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.compartilhar_viagem)))
    }

    // swipe callback para o recycler view
    inner class SwipeCallback : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        private val deleteIcon = getDrawable(R.drawable.ic_delete)
        private val shareIcon = getDrawable(R.drawable.ic_share)
        private val deleteBackground = Color.RED.toDrawable()
        private val shareBackground = Color.rgb(0, 150, 0).toDrawable() // verde

        // não permite mover os itens (?)
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        // swipe para a esquerda ou direita
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            when (direction) {
                ItemTouchHelper.LEFT -> {
                    // del da viagem
                    val viagemParaExcluir = viagens[position]
                    excluirViagem(viagemParaExcluir, position)
                }
                ItemTouchHelper.RIGHT -> {
                    // share da viagem, vai buscar a info e mete no intent-> text plain
                    compartilharViagem(position)
                    adapter.notifyItemChanged(position) // voltar el item para a pos og
                }
            }
        }

        // desenhar o background e os icones
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

            // clean do canvas para evitar overlap
            if (dX == 0f) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            if (dX < 0) { // deslizando para a esq
                // fundo vermelho
                deleteBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteBackground.draw(c)

                // draw do icon trash ic_delete
                val iconMargin = (itemHeight - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (deleteIcon?.intrinsicHeight ?: 0)
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - (deleteIcon?.intrinsicWidth ?: 0)

                deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon?.draw(c)
            } else if (dX > 0) { // agr deslizar para a dir
                // com fundo verde
                shareBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                shareBackground.draw(c)

                // e o ic_share
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
        // alert dialog para confirmar o delete
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.excluir_viagem))
            .setMessage(getString(R.string.deseja_realmente_excluir_a_viagem) + " ${viagem.nome}?")
            .setPositiveButton("Sim") { dialog, _ ->
                val userId = auth.currentUser?.uid ?: return@setPositiveButton

                // excluir a viagem do fire
                db.collection("usuarios")
                    .document(userId)
                    .collection("viagens")
                    .document(viagem.id)
                    .delete()
                    .addOnSuccessListener {
                        // tirar da lista com a sua pos e removeAt
                        val removedViagem = viagens.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        // desfazer, the option
                        Snackbar.make(viagensRecyclerView,
                            getString(R.string.viagem_excluida), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.desfazer)) {

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
                        // el tipico erro
                        Toast.makeText(this,
                            getString(R.string.erro_ao_excluir) + " ${e.message}", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(position)
                    }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.nao)) { dialog, _ ->
                // restaurar a viagem na lista se afinal não quiser
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        private const val REQUEST_CREATE_TRIP = 100
    }
}
