package com.example.tripsync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CriarViagemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_viagem)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val btnCriarViagem = findViewById<Button>(R.id.btnCriarViagem)
        val nomeViagemInput = findViewById<EditText>(R.id.etNome)

        // Voltar ao clicar no botão voltar
        backButton.setOnClickListener {
            finish()
        }

        // Enviar nome da viagem para HomeActivity
        btnCriarViagem.setOnClickListener {
            val nomeViagem = nomeViagemInput.text.toString()

            if (nomeViagem.isNotBlank()) {
                val intent = Intent()
                intent.putExtra("NOVA_VIAGEM", nomeViagem)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                nomeViagemInput.error = "Insere o nome da viagem"
            }
        }
    }
}
