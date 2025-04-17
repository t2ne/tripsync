package com.example.tripsync

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FotosViagemActivity : AppCompatActivity() {

    private lateinit var recyclerFotos: RecyclerView
    private lateinit var btnAdicionarFotos: Button
    private lateinit var tvSemFotos: TextView
    private lateinit var tvTituloViagem: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var viagemId: String
    private lateinit var nomeViagem: String
    private var locaisViagem: List<String> = emptyList()
    private val fotosList = mutableListOf<FotoViagem>()
    private var fotosAdapter: FotosAdapter? = null

    companion object {
        private const val PICK_MULTIPLE_IMAGES = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fotos_viagem)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obter os dados da intent
        viagemId = intent.getStringExtra("viagemId") ?: ""
        nomeViagem = intent.getStringExtra("nomeViagem") ?: "Fotos da Viagem"

        // Inicializar views
        recyclerFotos = findViewById(R.id.recyclerFotos)
        btnAdicionarFotos = findViewById(R.id.btnAdicionarFotos)

        // Configurar RecyclerView
        recyclerFotos.layoutManager = GridLayoutManager(this, 3)

        // Configurar botão voltar
        findViewById<ImageView>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        // Configurar botão adicionar fotos
        btnAdicionarFotos.setOnClickListener {
            selecionarMultiplasFotos()
        }

        // Carregar os dados da viagem (incluindo locais)
        carregarDadosViagem()
    }

    private fun carregarDadosViagem() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obter a lista de locais da viagem
                    locaisViagem = document.get("locais") as? List<String> ?: emptyList()

                    // Agora carregar as fotos
                    carregarFotos()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar dados da viagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarFotos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .get()
            .addOnSuccessListener { documents ->
                fotosList.clear()

                for (document in documents) {
                    val foto = document.toObject(FotoViagem::class.java).copy(id = document.id)
                    if (!foto.isDeleted) {
                        fotosList.add(foto)
                    }
                }

                // Ordenar fotos (opcional)
                // fotosList.sortByDescending { it.data }

                // Atualizar o RecyclerView
                fotosAdapter = FotosAdapter(fotosList) { foto ->
                    mostrarDetalhesFoto(foto)
                }
                recyclerFotos.adapter = fotosAdapter

                // Atualizar a UI baseada no número de fotos
                atualizarUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar fotos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarUI() {
        if (fotosList.isEmpty()) {
            recyclerFotos.visibility = View.GONE
        } else {
            recyclerFotos.visibility = View.VISIBLE
        }
    }

    private fun selecionarMultiplasFotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Selecione fotos"), PICK_MULTIPLE_IMAGES)
    }

    private fun mostrarDetalhesFoto(foto: FotoViagem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detalhes_foto, null)
        val imgFotoDetalhe = dialogView.findViewById<ImageView>(R.id.imgFotoDetalhe)
        val etDescricaoFoto = dialogView.findViewById<EditText>(R.id.etDescricaoFoto)
        val etDataFoto = dialogView.findViewById<EditText>(R.id.etDataFoto)
        val etClassificacaoFoto = dialogView.findViewById<EditText>(R.id.etClassificacaoFoto)
        val spinnerLocal = dialogView.findViewById<Spinner>(R.id.spinnerLocal)
        val btnExcluirFoto = dialogView.findViewById<Button>(R.id.btnExcluirFoto)
        val btnSalvarDetalhesFoto = dialogView.findViewById<Button>(R.id.btnSalvarDetalhesFoto)

        // Criando o AlertDialog para mostrar os detalhes - MOVIDO PARA CIMA
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Mostrar a imagem
        val file = File(foto.fotoUrl)
        if (file.exists()) {
            imgFotoDetalhe.setImageURI(Uri.fromFile(file))
        }

        // Preencher os campos com os dados existentes
        etDescricaoFoto.setText(foto.descricao)
        etDataFoto.setText(foto.data)
        etClassificacaoFoto.setText(foto.classificacao)

        // Configurar spinner de locais
        val locaisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locaisViagem)
        locaisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocal.adapter = locaisAdapter

        // Selecionar o local atual da foto (se existir)
        if (foto.localId.isNotEmpty()) {
            try {
                val index = foto.localId.toInt()
                if (index in 0 until locaisViagem.size) {
                    spinnerLocal.setSelection(index)
                }
            } catch (e: NumberFormatException) {
                // Se não for um número, ignorar
            }
        }

        btnExcluirFoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Excluir Foto")
                .setMessage("Tem certeza que deseja excluir esta foto?")
                .setPositiveButton("Sim") { confirmDialog, _ ->
                    excluirFoto(foto)
                    confirmDialog.dismiss()
                    dialog.dismiss() // Agora a variável dialog já está definida
                }
                .setNegativeButton("Não") { confirmDialog, _ ->
                    confirmDialog.dismiss()
                }
                .show()
        }

        // Configurar o botão de salvar
        btnSalvarDetalhesFoto.setOnClickListener {
            val descricao = etDescricaoFoto.text.toString()
            val data = etDataFoto.text.toString()
            val classificacao = etClassificacaoFoto.text.toString()
            val localIndex = spinnerLocal.selectedItemPosition.toString()

            atualizarDetalhesFoto(foto, descricao, data, classificacao, localIndex)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun excluirFoto(foto: FotoViagem) {
        val userId = auth.currentUser?.uid ?: return

        // Referência ao documento da foto
        val fotoRef = db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .document(foto.id)

        // Excluir completamente o documento
        fotoRef.delete()
            .addOnSuccessListener {
                // Excluir o arquivo da imagem do armazenamento interno
                try {
                    val file = File(foto.fotoUrl)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Ignorar erros de exclusão de arquivo
                }

                // Atualizar a lista e o adapter
                fotosList.remove(foto)
                fotosAdapter?.notifyDataSetChanged()
                atualizarUI()

                Toast.makeText(this, "Foto excluída com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarDetalhesFoto(foto: FotoViagem, descricao: String, data: String, classificacao: String, localId: String) {
        val userId = auth.currentUser?.uid ?: return

        val fotoAtualizada = hashMapOf(
            "descricao" to descricao,
            "data" to data,
            "classificacao" to classificacao,
            "localId" to localId
        )

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .document(foto.id)
            .update(fotoAtualizada as Map<String, Any>)
            .addOnSuccessListener {
                // Criar uma nova instância atualizada do objeto
                val fotoAtualizado = FotoViagem(
                    id = foto.id,
                    fotoUrl = foto.fotoUrl,
                    descricao = descricao,
                    data = data,
                    classificacao = classificacao,
                    localId = localId,
                    isDeleted = foto.isDeleted
                )

                // Encontrar e substituir o objeto na lista
                val index = fotosList.indexOfFirst { it.id == foto.id }
                if (index != -1) {
                    fotosList[index] = fotoAtualizado
                    fotosAdapter?.notifyItemChanged(index)
                }

                Toast.makeText(this, "Detalhes atualizados com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar detalhes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MULTIPLE_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val clipData = intent.clipData
                val singleUri = intent.data

                val imageUris = mutableListOf<Uri>()

                if (clipData != null) {
                    // Seleção múltipla
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        imageUris.add(uri)
                    }
                } else if (singleUri != null) {
                    // Seleção de uma única imagem
                    imageUris.add(singleUri)
                }

                if (imageUris.isNotEmpty()) {
                    processarFotosSelecionadas(imageUris)
                }
            }
        }
    }

    private fun processarFotosSelecionadas(uris: List<Uri>) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            // Mostrar mensagem de carregamento
            val loadingDialog = AlertDialog.Builder(this@FotosViagemActivity)
                .setMessage("Processando fotos...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            try {
                // Processar cada imagem
                for (uri in uris) {
                    // Gerar ID único para a foto
                    val fotoId = UUID.randomUUID().toString()

                    // Salvar imagem no armazenamento interno
                    val fotoPath = ImageUtils.saveImageToInternalStorage(
                        this@FotosViagemActivity,
                        uri,
                        "$viagemId-$fotoId",
                        "photo"
                    )

                    // Criar documento para a foto no Firestore
                    val novaFoto = FotoViagem(
                        id = fotoId,
                        fotoUrl = fotoPath,
                        descricao = "",
                        data = "",
                        classificacao = "",
                        localId = "", // Inicialmente sem local
                        isDeleted = false
                    )

                    // Salvar no Firestore
                    db.collection("usuarios")
                        .document(userId)
                        .collection("viagens")
                        .document(viagemId)
                        .collection("fotos")
                        .document(fotoId)
                        .set(novaFoto)
                }

                // Finalizar
                loadingDialog.dismiss()
                Toast.makeText(this@FotosViagemActivity, "Fotos adicionadas com sucesso", Toast.LENGTH_SHORT).show()

                // Recarregar as fotos
                carregarFotos()

            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@FotosViagemActivity, "Erro ao processar fotos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}