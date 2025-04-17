package com.example.tripsync

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class EditarViagemActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var viagemId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var imagePath: String = ""

    // Componentes dos locais
    private lateinit var containerLocais: LinearLayout
    private lateinit var btnAdicionarLocal: ImageView
    private lateinit var btnRemoverUltimoLocal: ImageView
    private lateinit var tvAdicionarRemoverLocal: TextView
    private val locaisViagem = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_viagem)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Recebe dados
        val nome = intent.getStringExtra("nomeViagem") ?: ""
        viagemId = intent.getStringExtra("viagemId") ?: ""

        // Inicializar componentes dos locais
        containerLocais = findViewById(R.id.containerLocais)
        btnAdicionarLocal = findViewById(R.id.btnAdicionarLocal)
        btnRemoverUltimoLocal = findViewById(R.id.btnRemoverUltimoLocal)
        tvAdicionarRemoverLocal = findViewById(R.id.tvAdicionarRemoverLocal)

        // Configurar botão de adicionar local
        btnAdicionarLocal.setOnClickListener {
            if (containerLocais.childCount < MAX_LOCAIS) {
                adicionarNovoLocal()

                // Ocultar apenas o botão + quando atingir o limite
                if (containerLocais.childCount >= MAX_LOCAIS) {
                    btnAdicionarLocal.visibility = View.GONE
                }
            }
        }

        // Configurar botão de remover último local
        btnRemoverUltimoLocal.setOnClickListener {
            if (containerLocais.childCount > 1) {
                removerUltimoLocal()
            } else {
                Toast.makeText(this, "É necessário pelo menos um local!", Toast.LENGTH_SHORT).show()
            }
        }

        // Carregar dados da viagem do Firestore
        carregarDadosViagem()

        // Botão Voltar
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        // Botão Salvar
        val btnCriar = findViewById<Button>(R.id.btnCriar)
        btnCriar.setOnClickListener {
            salvarAlteracoes()
        }

        // Configuração do botão de selecionar foto
        val btnSelecionarFoto = findViewById<Button>(R.id.btnSelecionarFoto)
        btnSelecionarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
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
                    findViewById<EditText>(R.id.edtNomeViagem).setText(document.getString("nome"))
                    findViewById<EditText>(R.id.edtData).setText(document.getString("data"))
                    findViewById<EditText>(R.id.edtDescricao).setText(document.getString("descricao"))
                    findViewById<EditText>(R.id.edtClassificacao).setText(document.getString("classificacao"))

                    // Guardar o caminho da imagem
                    imagePath = document.getString("fotoUrl") ?: ""

                    // Mostrar a imagem se existir
                    if (imagePath.isNotEmpty()) {
                        val file = File(imagePath)
                        if (file.exists()) {
                            val imgFotoViagem = findViewById<ImageView>(R.id.imgFotoViagem)
                            imgFotoViagem.setImageURI(Uri.fromFile(file))
                        }
                    }

                    // Carregar locais se existirem
                    val locais = document.get("locais") as? List<String>
                    if (!locais.isNullOrEmpty()) {
                        carregarLocaisExistentes(locais)
                    } else {
                        // Se não houver locais, adicionar pelo menos um vazio
                        adicionarNovoLocal()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarLocaisExistentes(locais: List<String>) {
        // Limpar container de locais
        containerLocais.removeAllViews()

        // Adicionar cada local
        for (local in locais) {
            val localView = LayoutInflater.from(this).inflate(R.layout.item_local, containerLocais, false)

            val indice = containerLocais.childCount + 1
            val tvLocalLabel = localView.findViewById<TextView>(R.id.tvLocalLabel)
            tvLocalLabel.text = "Local $indice"

            val etLocal = localView.findViewById<EditText>(R.id.etLocal)
            etLocal.setText(local)

            containerLocais.addView(localView)
        }

        // Atualizar visibilidade do botão de adicionar
        if (containerLocais.childCount >= MAX_LOCAIS) {
            btnAdicionarLocal.visibility = View.GONE
        } else {
            btnAdicionarLocal.visibility = View.VISIBLE
        }
    }

    private fun adicionarNovoLocal() {
        val inflater = LayoutInflater.from(this)
        val localView = inflater.inflate(R.layout.item_local, containerLocais, false)

        val indice = containerLocais.childCount + 1
        val tvLocalLabel = localView.findViewById<TextView>(R.id.tvLocalLabel)
        tvLocalLabel.text = "Local $indice"

        containerLocais.addView(localView)
    }

    private fun removerUltimoLocal() {
        if (containerLocais.childCount > 0) {
            containerLocais.removeViewAt(containerLocais.childCount - 1)

            // Mostrar botão de adicionar se estiver abaixo do limite
            if (containerLocais.childCount < MAX_LOCAIS) {
                btnAdicionarLocal.visibility = View.VISIBLE
            }
        }
    }

    private fun getLocais() {
        locaisViagem.clear()

        for (i in 0 until containerLocais.childCount) {
            val localView = containerLocais.getChildAt(i)
            val etLocal = localView.findViewById<EditText>(R.id.etLocal)
            val localText = etLocal.text.toString().trim()

            if (localText.isNotEmpty()) {
                locaisViagem.add(localText)
            }
        }
    }

    private fun salvarAlteracoes() {
        val userId = auth.currentUser?.uid ?: return
        val novoNome = findViewById<EditText>(R.id.edtNomeViagem).text.toString()
        val novaData = findViewById<EditText>(R.id.edtData).text.toString()
        val novaDescricao = findViewById<EditText>(R.id.edtDescricao).text.toString()
        val novaClassificacao = findViewById<EditText>(R.id.edtClassificacao).text.toString()

        if (novoNome.isBlank()) {
            findViewById<EditText>(R.id.edtNomeViagem).error = "Nome é obrigatório"
            return
        }

        // Coletar locais preenchidos
        getLocais()

        // Mostrar progresso
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Salvando alterações...")
        progressDialog.show()

        lifecycleScope.launch {
            // Se tiver uma nova imagem selecionada
            if (selectedImageUri != null) {
                // Deletar imagem antiga se existir
                if (imagePath.isNotEmpty()) {
                    ImageUtils.deleteImage(this@EditarViagemActivity, viagemId, "trip")
                }

                // Salvar nova imagem
                imagePath = ImageUtils.saveImageToInternalStorage(
                    this@EditarViagemActivity,
                    selectedImageUri!!,
                    viagemId,
                    "trip"
                )
            }

            atualizarViagem(userId, novoNome, novaData, novaDescricao, novaClassificacao, imagePath)
            progressDialog.dismiss()
        }
    }

    private fun atualizarViagem(
        userId: String, nome: String, data: String, descricao: String,
        classificacao: String, fotoUrl: String
    ) {
        val viagemAtualizada = hashMapOf(
            "nome" to nome,
            "data" to data,
            "descricao" to descricao,
            "classificacao" to classificacao,
            "fotoUrl" to fotoUrl,
            "locais" to locaisViagem
        )

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .update(viagemAtualizada as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Viagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this, "Erro ao atualizar viagem: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Recebendo a imagem selecionada da galeria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            selectedImageUri = data?.data
            val imageView = findViewById<ImageView>(R.id.imgFotoViagem)
            imageView.setImageURI(selectedImageUri)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val MAX_LOCAIS = 10
    }
}