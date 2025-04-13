package com.example.tripsync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditarViagemActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_viagem)

        // Recebe dados
        val nome = intent.getStringExtra("nomeViagem")

        // Atualiza o EditText com o nome da viagem
        findViewById<EditText>(R.id.edtNomeViagem).setText(nome) // Alterado para edtNomeViagem
        findViewById<EditText>(R.id.edtData).setText("27/12/2024")
        findViewById<EditText>(R.id.edtDescricao).setText("Visita em Família")
        findViewById<EditText>(R.id.edtClassificacao).setText("5.0")

        // Botão Voltar
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        // Botão Criar → Voltar ao Home
        val btnCriar = findViewById<Button>(R.id.btnCriar)
        btnCriar.setOnClickListener {
            // Pega os dados atualizados inseridos no EditText
            val novoNome = findViewById<EditText>(R.id.edtNomeViagem).text.toString()
            val novaData = findViewById<EditText>(R.id.edtData).text.toString()
            val novaDescricao = findViewById<EditText>(R.id.edtDescricao).text.toString()
            val novaClassificacao = findViewById<EditText>(R.id.edtClassificacao).text.toString()

            // Passa os dados atualizados para a HomeActivity ou para onde for necessário
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("nomeViagem", novoNome)
                putExtra("data", novaData)
                putExtra("descricao", novaDescricao)
                putExtra("classificacao", novaClassificacao)
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // Configuração do botão de selecionar foto
        val btnSelecionarFoto = findViewById<Button>(R.id.btnSelecionarFoto)
        btnSelecionarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
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
    }
}
