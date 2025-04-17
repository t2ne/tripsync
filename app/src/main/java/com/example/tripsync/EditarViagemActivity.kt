package com.example.tripsync

import android.annotation.SuppressLint
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
import androidx.core.view.isNotEmpty
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditarViagemActivity : AppCompatActivity() {

    private lateinit var viagemId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var imagePath: String = ""

    // the componentes dos locais
    private lateinit var containerLocais: LinearLayout
    private lateinit var btnAdicionarLocal: ImageView
    private lateinit var btnRemoverUltimoLocal: ImageView
    private lateinit var tvAdicionarRemoverLocal: TextView
    private val locaisViagem = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_viagem)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // recebe os dados que passamos da home activity
        val nome = intent.getStringExtra("nomeViagem") ?: ""
        viagemId = intent.getStringExtra("viagemId") ?: ""

        // init mais components
        containerLocais = findViewById(R.id.containerLocais)
        btnAdicionarLocal = findViewById(R.id.btnAdicionarLocal)
        btnRemoverUltimoLocal = findViewById(R.id.btnRemoverUltimoLocal)
        tvAdicionarRemoverLocal = findViewById(R.id.tvAdicionarRemoverLocal)

        // config add local
        btnAdicionarLocal.setOnClickListener {
            if (containerLocais.childCount < MAX_LOCAIS) {
                adicionarNovoLocal()

                // outra vez a cena do limite
                if (containerLocais.childCount >= MAX_LOCAIS) {
                    btnAdicionarLocal.visibility = View.GONE
                }
            }
        }

        //again btn do ultimo local
        btnRemoverUltimoLocal.setOnClickListener {
            if (containerLocais.childCount > 1) {
                removerUltimoLocal()
            } else {
                Toast.makeText(this, getString(R.string.necessario_pelo_menos_1_local), Toast.LENGTH_SHORT).show()
            }
        }

        carregarDadosViagem()

        // bck btn
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        // save
        val btnCriar = findViewById<Button>(R.id.btnCriar)
        btnCriar.setOnClickListener {
            salvarAlteracoes()
        }

        // config do btn para acessar fotos
        val btnAcessarFotos = findViewById<LinearLayout>(R.id.btnAcessarFotos)
        btnAcessarFotos.setOnClickListener {

            // fotos viagem activity tp
            val intent = Intent(this, FotosViagemActivity::class.java).apply {
                putExtra("viagemId", viagemId)
                putExtra("nomeViagem", findViewById<EditText>(R.id.edtNomeViagem).text.toString())
            }
            startActivity(intent)
        }
    }

    // mtd para ir buscar os dados da viagem needed
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

                    // imgpth
                    imagePath = document.getString("fotoUrl") ?: ""

                    // (opcional) counting
                    atualizarCounterFotos()

                    // se houverem locais, carregar
                    val locais = document.get("locais") as? List<String>
                    if (!locais.isNullOrEmpty()) {
                        carregarLocaisExistentes(locais)
                    } else {
                        // 1 local por defeito vzio
                        adicionarNovoLocal()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.erro_ao_carregar_dados) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Método para atualizar o contador de fotos
    private fun atualizarCounterFotos() {
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
                    numFotos == 0 -> getString(R.string.carregue_para_acessar_fotos)
                    numFotos == 1 -> getString(R.string.uma_foto)
                    else -> "$numFotos" + getString(R.string.fotos)
                }
            }
            .addOnFailureListener {
                tvQuantidadeFotos.text = getString(R.string.carregue_para_acessar_fotos)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun carregarLocaisExistentes(locais: List<String>) {

        //clean do container dos locais antes de adicionar os novos
        containerLocais.removeAllViews()

        // add cada local
        for (local in locais) {
            val localView = LayoutInflater.from(this).inflate(R.layout.item_local, containerLocais, false)
            val indice = containerLocais.childCount + 1
            val tvLocalLabel = localView.findViewById<TextView>(R.id.tvLocalLabel)
            val etLocal = localView.findViewById<EditText>(R.id.etLocal)

            tvLocalLabel.text = getString(R.string.local) + " $indice"
            etLocal.setText(local)

            containerLocais.addView(localView)
        }

        // update visibility do btn de adicionar
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
        tvLocalLabel.text = getString(R.string.local) + " $indice"

        containerLocais.addView(localView)
    }

    private fun removerUltimoLocal() {
        if (containerLocais.isNotEmpty()) {
            containerLocais.removeViewAt(containerLocais.childCount - 1)

            // rmover o ultimo local nuh uh
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
            Toast.makeText(this,
                getString(R.string.o_nome_da_viagem_obrigatorio), Toast.LENGTH_SHORT).show()
            return
        }

        // ir buscar locais preenchidos
        getLocais()

        // prgrss again
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.a_salvar_alteracoes))
        progressDialog.show()

        lifecycleScope.launch {
            // new obj
            val viagemAtualizada = hashMapOf(
                "nome" to novoNome,
                "data" to novaData,
                "descricao" to novaDescricao,
                "classificacao" to novaClassificacao,
                "fotoUrl" to imagePath,
                "locais" to locaisViagem
            )
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
                Toast.makeText(this, getString(R.string.viagem_atualizada), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this,
                    getString(R.string.erro_ao_atualizar_viagem) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val MAX_LOCAIS = 10
    }
}