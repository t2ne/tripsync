package com.example.tripsync

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedProfileImageUri: Uri? = null
    private var imagePath: String = ""
    private var hasFotoPerfil = false
    private var fotoMarcadaParaRemocao = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referencias para os campos de texto
        val etNome = findViewById<EditText>(R.id.etNome)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)

        // Carregar dados do usuário
        carregarDadosUsuario(etNome, etUsername, etEmail, imgPerfil)

        // Configurar o botão de voltar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Configurar o botão de salvar
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        btnSalvar.setOnClickListener {
            salvarAlteracoes(etNome, etUsername, etEmail, etPassword)
        }

        // Configurar o botão de logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Configurar o botão de selecionar foto de perfil
        val btnSelecionarFotoPerfil = findViewById<Button>(R.id.btnSelecionarFotoPerfil)
        btnSelecionarFotoPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Configurar o botão de remover foto de perfil
        val btnRemoverFotoPerfil = findViewById<Button>(R.id.btnRemoverFotoPerfil)
        btnRemoverFotoPerfil.setOnClickListener {
            if (!hasFotoPerfil && !fotoMarcadaParaRemocao) {
                Toast.makeText(this, "Não há foto de perfil para remover", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Remover Foto")
                .setMessage("Tem a certeza que deseja remover a foto de perfil?")
                .setPositiveButton("Sim") { dialog, _ ->
                    // Apenas marca para remoção visual
                    marcarFotoParaRemocao()
                    dialog.dismiss()
                }
                .setNegativeButton("Não") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }

    private fun marcarFotoParaRemocao() {
        // Apenas alteração visual - a remoção real será feita apenas no salvamento
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        imgPerfil.setImageResource(R.drawable.logo)

        fotoMarcadaParaRemocao = true
        selectedProfileImageUri = null

        Toast.makeText(this, "Foto será removida ao salvar as alterações", Toast.LENGTH_SHORT).show()
        Log.d("EditarPerfilActivity", "Foto marcada para remoção")
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Terminar Sessão")
            .setMessage("Tem a certeza que deseja sair da sua conta?")
            .setPositiveButton("Sim") { dialog, _ ->
                // Fazer logout
                auth.signOut()
                // Redirecionar para a tela de login
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun carregarDadosUsuario(etNome: EditText, etUsername: EditText, etEmail: EditText, imgPerfil: ImageView) {
        val userId = auth.currentUser?.uid ?: return

        // Resetar flags no carregamento
        fotoMarcadaParaRemocao = false
        selectedProfileImageUri = null
        hasFotoPerfil = false

        // Buscar dados do Firestore primeiro
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Preencher campos de texto
                    etNome.setText(document.getString("nome"))
                    etUsername.setText(document.getString("username"))
                    etEmail.setText(document.getString("email"))

                    // Verificar se existe um caminho de foto no Firestore
                    val fotoPerfilUrl = document.getString("fotoPerfilUrl") ?: ""

                    // Verificar se existe foto de perfil (caminho não vazio)
                    if (fotoPerfilUrl.isNotEmpty()) {
                        lifecycleScope.launch {
                            val file = File(fotoPerfilUrl)
                            if (file.exists()) {
                                imgPerfil.setImageURI(Uri.fromFile(file))
                                imagePath = fotoPerfilUrl
                                hasFotoPerfil = true
                                Log.d("EditarPerfilActivity", "Foto de perfil carregada: $fotoPerfilUrl")
                            } else {
                                imgPerfil.setImageResource(R.drawable.logo)
                                Log.d("EditarPerfilActivity", "Arquivo de foto não existe: $fotoPerfilUrl")
                                hasFotoPerfil = false
                            }
                        }
                    } else {
                        // Sem foto, usar logo padrão
                        imgPerfil.setImageResource(R.drawable.logo)
                        hasFotoPerfil = false
                        Log.d("EditarPerfilActivity", "Usuário sem foto de perfil")
                    }
                } else {
                    Log.d("EditarPerfilActivity", "Documento não existe no Firestore")
                    imgPerfil.setImageResource(R.drawable.logo)
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditarPerfilActivity", "Erro ao carregar dados: ${e.message}")
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                imgPerfil.setImageResource(R.drawable.logo)
            }
    }

    private fun salvarAlteracoes(etNome: EditText, etUsername: EditText, etEmail: EditText, etPassword: EditText) {
        val nome = etNome.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (nome.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser ?: return
        val userId = user.uid

        // Mostrar progresso
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Salvando alterações...")
        progressDialog.show()

        // Se o email foi alterado, atualizar no Authentication
        if (email != user.email) {
            // Por simplicidade, vamos apenas mostrar um toast informando que não é possível
            Toast.makeText(this, "Não é possível alterar o email nesta versão", Toast.LENGTH_SHORT).show()
        }

        // Se a senha foi preenchida, atualizar no Authentication
        if (password.isNotEmpty()) {
            user.updatePassword(password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao atualizar senha: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Atualizar dados no Firestore e gerenciar imagem
        lifecycleScope.launch {
            // Criar um mapa com os dados a serem atualizados
            val userData = hashMapOf(
                "nome" to nome,
                "username" to username,
                "email" to email
            )

            // Processar alterações de imagem
            if (fotoMarcadaParaRemocao) {
                // Remover a foto se foi marcada para remoção
                if (imagePath.isNotEmpty()) {
                    try {
                        ImageUtils.deleteImage(this@EditarPerfilActivity, userId, "profile")
                        userData["fotoPerfilUrl"] = ""
                        hasFotoPerfil = false
                        imagePath = ""
                        Log.d("EditarPerfilActivity", "Foto removida com sucesso durante o salvamento")
                    } catch (e: Exception) {
                        Log.e("EditarPerfilActivity", "Erro ao remover imagem: ${e.message}")
                    }
                }
            } else if (selectedProfileImageUri != null) {
                // Se uma nova imagem foi selecionada
                Log.d("EditarPerfilActivity", "Nova imagem selecionada, salvando...")

                // Deletar imagem antiga se existir
                if (imagePath.isNotEmpty()) {
                    try {
                        ImageUtils.deleteImage(this@EditarPerfilActivity, userId, "profile")
                    } catch (e: Exception) {
                        Log.e("EditarPerfilActivity", "Erro ao deletar imagem antiga: ${e.message}")
                    }
                }

                // Salvar nova imagem
                try {
                    imagePath = ImageUtils.saveImageToInternalStorage(
                        this@EditarPerfilActivity,
                        selectedProfileImageUri!!,
                        userId,
                        "profile"
                    )

                    // Atualizar o caminho da imagem no Firestore
                    userData["fotoPerfilUrl"] = imagePath
                    hasFotoPerfil = true
                    Log.d("EditarPerfilActivity", "Nova imagem salva em: $imagePath")
                } catch (e: Exception) {
                    Log.e("EditarPerfilActivity", "Erro ao salvar nova imagem: ${e.message}")
                    Toast.makeText(this@EditarPerfilActivity, "Erro ao salvar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Atualizar no Firestore
            updateUserData(userId, userData, progressDialog)
        }
    }

    private fun updateUserData(userId: String, userData: HashMap<String, String>, progressDialog: ProgressDialog) {
        db.collection("usuarios").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Erro ao atualizar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedProfileImageUri = data.data
            val imageView = findViewById<ImageView>(R.id.imgPerfil)
            imageView.setImageURI(selectedProfileImageUri)

            // Resetar flag de remoção se selecionar nova imagem
            fotoMarcadaParaRemocao = false

            // Marcar que tem foto de perfil quando seleciona
            hasFotoPerfil = true
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}