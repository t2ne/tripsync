package com.example.tripsync

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CriarViagemActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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
                "fotoUrl" to "" // Campo vazio para manter compatibilidade
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
}