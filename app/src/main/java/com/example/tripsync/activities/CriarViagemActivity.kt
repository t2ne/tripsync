package com.example.tripsync.activities

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
import com.example.tripsync.R
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

        // init firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val btnCriarViagem = findViewById<Button>(R.id.btnCriarViagem)
        val nomeViagemInput = findViewById<EditText>(R.id.etNome)
        val dataInput = findViewById<EditText>(R.id.etData)
        val descricaoInput = findViewById<EditText>(R.id.etDescricao)
        val classificacaoInput = findViewById<EditText>(R.id.etClassificacao)

        // iniciar containers e etc
        containerLocais = findViewById(R.id.containerLocais)
        btnAdicionarLocal = findViewById(R.id.btnAdicionarLocal)
        btnRemoverUltimoLocal = findViewById(R.id.btnRemoverUltimoLocal)
        tvAdicionarRemoverLocal = findViewById(R.id.tvAdicionarRemoverLocal)

        // add local inicial
        adicionarNovoLocal()

        // config do botão de adicionar local
        btnAdicionarLocal.setOnClickListener {
            if (containerLocais.childCount < MAX_LOCAIS) {
                adicionarNovoLocal()

                // esconder o botão se tiverem 10 locais
                if (containerLocais.childCount >= MAX_LOCAIS) {
                    btnAdicionarLocal.visibility = View.GONE
                }
            }
        }

        //  config do botão de remover locais, se for o último local não pode
        btnRemoverUltimoLocal.setOnClickListener {
            if (containerLocais.childCount > 1) {
                removerUltimoLocal()
            } else {
                Toast.makeText(this,
                    getString(R.string.necessario_pelo_menos_1_local), Toast.LENGTH_SHORT).show()
            }
        }

        // back
        backButton.setOnClickListener {
            finish()
        }

        // btn para criar viagem (salvar)
        btnCriarViagem.setOnClickListener {
            val nomeViagem = nomeViagemInput.text.toString().trim()
            val data = dataInput.text.toString().trim()
            val descricao = descricaoInput.text.toString().trim()
            val classificacao = classificacaoInput.text.toString().trim()

            // validação ez
            if (nomeViagem.isEmpty()) {
                nomeViagemInput.error = getString(R.string.nome_da_viagem_obrigatorio)
                return@setOnClickListener
            }

            getLocais()

            // mostrar progresso ao user
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.a_criar_viagem))
            progressDialog.show()

            // add obj viagem
            val viagem = hashMapOf(
                "nome" to nomeViagem,
                "data" to data,
                "descricao" to descricao,
                "classificacao" to classificacao,
                "fotoUrl" to "", //vazio por default
                "locais" to locaisViagem //usado depois no edit
            )

            // save para a firestore
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("usuarios")
                    .document(userId)
                    .collection("viagens")
                    .add(viagem)
                    .addOnSuccessListener { documentReference ->
                        progressDialog.dismiss()
                        Toast.makeText(this,
                            getString(R.string.viagem_criada_com_sucesso), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this,
                            getString(R.string.erro_ao_criar_viagem) + " ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this,
                    getString(R.string.utilizador_nao_autenticado), Toast.LENGTH_SHORT).show()

                // redirect para login
                startActivity(Intent(this, LoginActivity::class.java))
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

            // moostrar o botão de adicionar se houver menos de 10 locais
            if (containerLocais.childCount < MAX_LOCAIS) {
                btnAdicionarLocal.visibility = View.VISIBLE
            }
        }
    }

    //ir buscar os locais
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