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

class EditarPerfilActivity : AppCompatActivity() {

    private var selectedProfileImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        // Configurar o botão de voltar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Configurar o botão de salvar
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        btnSalvar.setOnClickListener {
            // Aqui você pode adicionar qualquer lógica para salvar as informações
            // Exemplo: Verificar se os campos estão preenchidos ou salvar dados no banco.
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar o botão de selecionar foto de perfil
        val btnSelecionarFotoPerfil = findViewById<Button>(R.id.btnSelecionarFotoPerfil)
        btnSelecionarFotoPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // Recebendo a imagem selecionada da galeria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            selectedProfileImageUri = data?.data
            val imageView = findViewById<ImageView>(R.id.imgPerfil)
            imageView.setImageURI(selectedProfileImageUri)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
