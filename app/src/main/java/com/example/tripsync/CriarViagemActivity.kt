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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CriarViagemActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var containerLocais: LinearLayout
    private lateinit var btnAdicionarLocal: ImageView
    private lateinit var btnRemoverUltimoLocal: ImageView
    private lateinit var tvAdicionarRemoverLocal: TextView
    private val locaisViagem = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_viagem)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val btnCriarViagem = findViewById<Button>(R.id.btnCriarViagem)
        val nomeViagemInput = findViewById<EditText>(R.id.etNome)
        val dataInput = findViewById<EditText>(R.id.etData)
        val descricaoInput = findViewById<EditText>(R.id.etDescricao)
        val classificacaoInput = findViewById<EditText>(R.id.etClassificacao)

        // Inicializar container de locais e botões adicionar/remover
        containerLocais = findViewById(R.id.containerLocais)
        btnAdicionarLocal = findViewById(R.id.btnAdicionarLocal)
        btnRemoverUltimoLocal = findViewById(R.id.btnRemoverUltimoLocal)
        tvAdicionarRemoverLocal = findViewById(R.id.tvAdicionarRemoverLocal)

        // Adicionar primeiro local por padrão
        adicionarNovoLocal()

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

        // Botão voltar
        backButton.setOnClickListener {
            finish()
        }

        // Botão criar viagem
        btnCriarViagem.setOnClickListener {
            val nomeViagem = nomeViagemInput.text.toString().trim()
            val data = dataInput.text.toString().trim()
            val descricao = descricaoInput.text.toString().trim()
            val classificacao = classificacaoInput.text.toString().trim()

            // Validação básica
            if (nomeViagem.isEmpty()) {
                nomeViagemInput.error = "Nome da viagem é obrigatório"
                return@setOnClickListener
            }

            // Coletar locais preenchidos
            getLocais()

            // Mostrar progresso
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Criando viagem...")
            progressDialog.show()

            // Criar objeto da viagem
            val viagem = hashMapOf(
                "nome" to nomeViagem,
                "data" to data,
                "descricao" to descricao,
                "classificacao" to classificacao,
                "fotoUrl" to "", // Campo vazio para manter compatibilidade
                "locais" to locaisViagem // Nova lista de locais
            )

            // Salvar no Firestore
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("usuarios")
                    .document(userId)
                    .collection("viagens")
                    .add(viagem)
                    .addOnSuccessListener { documentReference ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Viagem criada com sucesso!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Erro ao criar viagem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                // Redirecionar para login
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
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

    companion object {
        private const val MAX_LOCAIS = 10
    }
}