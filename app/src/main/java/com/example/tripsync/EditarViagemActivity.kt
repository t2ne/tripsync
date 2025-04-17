package com.example.tripsync

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditarViagemActivity : AppCompatActivity() {

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

        // Configuração do botão para acessar o álbum de fotos
        val btnAcessarFotos = findViewById<LinearLayout>(R.id.btnAcessarFotos)
        btnAcessarFotos.setOnClickListener {
            // Abrir a atividade de fotos
            val intent = Intent(this, FotosViagemActivity::class.java).apply {
                putExtra("viagemId", viagemId)
                putExtra("nomeViagem", findViewById<EditText>(R.id.edtNomeViagem).text.toString())
            }
            startActivity(intent)
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

                    // Atualizar contador de fotos (opcional)
                    atualizarContadorFotos()

                    // Carregar locais se existirem
                    val locais = document.get("locais") as? List<String>
                    if (!locais.isNullOrEmpty()) {
                        carregarLocaisExistentes(locais)
                    } else {
                        // Adicionar pelo menos um local vazio
                        adicionarNovoLocal()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Método para atualizar o contador de fotos
    private fun atualizarContadorFotos() {
        val userId = auth.currentUser?.uid ?: return
        val tvQuantidadeFotos = findViewById<TextView>(R.id.tvQuantidadeFotos)

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .whereEqualTo("isDeleted", false)
            .get()
            .addOnSuccessListener { documents ->
                val numFotos = documents.size()
                tvQuantidadeFotos.text = when {
                    numFotos == 0 -> "Carregue para acessar fotos"
                    numFotos == 1 -> "1 foto"
                    else -> "$numFotos fotos"
                }
            }
            .addOnFailureListener {
                tvQuantidadeFotos.text = "Carregue para acessar fotos"
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
            val etLocal = localView.findViewById<EditText>(R.id.etLocal)

            tvLocalLabel.text = "Local $indice"
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
            Toast.makeText(this, "O nome da viagem é obrigatório", Toast.LENGTH_SHORT).show()
            return
        }

        // Coletar locais preenchidos
        getLocais()

        // Mostrar progresso
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Salvando alterações...")
        progressDialog.show()

        lifecycleScope.launch {
            // Criar objeto com os dados atualizados
            val viagemAtualizada = hashMapOf(
                "nome" to novoNome,
                "data" to novaData,
                "descricao" to novaDescricao,
                "classificacao" to novaClassificacao,
                "fotoUrl" to imagePath,
                "locais" to locaisViagem
            )

            // Atualizar no Firestore
            atualizarViagem(userId, viagemAtualizada, progressDialog)
        }
    }

    private fun atualizarViagem(
        userId: String,
        viagemAtualizada: HashMap<String, Any>,
        progressDialog: ProgressDialog
    ) {
        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .update(viagemAtualizada)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Viagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Erro ao atualizar viagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val MAX_LOCAIS = 10
    }
}