package com.example.tripsync.activities

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
import com.example.tripsync.R
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etNome = findViewById<EditText>(R.id.etNome)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)

        carregarDadosUser(etNome, etUsername, etEmail, imgPerfil)

        // bckbtn
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // o resto é td botões
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        btnSalvar.setOnClickListener {
            salvarAlteracoes(etNome, etUsername, etEmail, etPassword)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        val btnSelecionarFotoPerfil = findViewById<Button>(R.id.btnSelecionarFotoPerfil)
        btnSelecionarFotoPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val btnRemoverFotoPerfil = findViewById<Button>(R.id.btnRemoverFotoPerfil)
        btnRemoverFotoPerfil.setOnClickListener {
            if (!hasFotoPerfil && !fotoMarcadaParaRemocao) {
                Toast.makeText(this, getString(R.string.no_pfp_para_remover), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alertDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.remover_foto))
                .setMessage(getString(R.string.u_sure_remover_foto_perfil))
                .setPositiveButton(getString(R.string.sim)) { dialog, _ ->

                    marcarFotoParaRemocao()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.nao)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }

    // marca apenas para remover visualmente
    private fun marcarFotoParaRemocao() {
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        imgPerfil.setImageResource(R.drawable.logo)

        fotoMarcadaParaRemocao = true
        selectedProfileImageUri = null

        Toast.makeText(this, getString(R.string.foto_sera_removida), Toast.LENGTH_SHORT).show()
        Log.d("EditarPerfilActivity", getString(R.string.foto_marcada_para_remocao))
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.terminar_sessao))
            .setMessage(getString(R.string.tem_a_certeza_que_deseja_sair_da_sua_conta))
            .setPositiveButton(getString(R.string.sim)) { dialog, _ ->

                //dá o logout
                auth.signOut()

                // redirect para o login com intent
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.nao)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // carrega dados do usuário
    private fun carregarDadosUser(etNome: EditText, etUsername: EditText, etEmail: EditText, imgPerfil: ImageView) {
        val userId = auth.currentUser?.uid ?: return

        // reset das flags ao carregar
        fotoMarcadaParaRemocao = false
        selectedProfileImageUri = null
        hasFotoPerfil = false

        // vai buscar os dados do user ao firestore primeiro

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    // preencher campos
                    etNome.setText(document.getString("nome"))
                    etUsername.setText(document.getString("username"))
                    etEmail.setText(document.getString("email"))

                    // verify do caminho de fotos
                    val fotoPerfilUrl = document.getString("fotoPerfilUrl") ?: ""

                    // verify da PFP
                    if (fotoPerfilUrl.isNotEmpty()) {
                        lifecycleScope.launch {
                            val file = File(fotoPerfilUrl)
                            if (file.exists()) {
                                imgPerfil.setImageURI(Uri.fromFile(file))
                                imagePath = fotoPerfilUrl
                                hasFotoPerfil = true
                            } else {
                                imgPerfil.setImageResource(R.drawable.logo)
                                hasFotoPerfil = false
                            }
                        }
                    } else {

                        // no foto? no problem
                        imgPerfil.setImageResource(R.drawable.logo)
                        hasFotoPerfil = false
                    }
                } else {
                    imgPerfil.setImageResource(R.drawable.logo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_carregar_dados) + " ${e.message}", Toast.LENGTH_SHORT).show()
                imgPerfil.setImageResource(R.drawable.logo)
            }
    }

    // salva todas as alterações do usr
    private fun salvarAlteracoes(etNome: EditText, etUsername: EditText, etEmail: EditText, etPassword: EditText) {
        val nome = etNome.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // vê se os campos obrgratorios tao preenchidos
        if (nome.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this,
                getString(R.string.preencha_os_campos_obrigatorios), Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser ?: return
        val userId = user.uid

        // mostra progresso como no criarviagem
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.a_salvar_alteracoes))
        progressDialog.show()

        // update email (não implementado, não é necessário tbh, só mais um add)
        if (email != user.email) {
            Toast.makeText(this,
                getString(R.string.nao_e_possivel_alterar_email), Toast.LENGTH_SHORT).show()
        }

        // update password se ele quiser, não há autofill para isso q vá ao home actvt
        if (password.isNotEmpty()) {
            user.updatePassword(password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,
                            getString(R.string.password_atualizada), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this,
                            getString(R.string.erro_ao_atualizar_senha) + "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // update no firestore e gerir img
        lifecycleScope.launch {
            // hashmap para guardar os dados (outra vez)
            val userData = hashMapOf(
                "nome" to nome,
                "username" to username,
                "email" to email
            )

            // processar mudança de foto
            if (fotoMarcadaParaRemocao) {

                // remover a foto se foi marcada para remoção
                if (imagePath.isNotEmpty()) {
                    try {
                        ImageUtils.deleteImage(this@EditarPerfilActivity, userId, "profile")
                        userData["fotoPerfilUrl"] = ""
                        hasFotoPerfil = false
                        imagePath = ""
                    } catch (e: Exception) {
                        Log.e("EditarPerfilActivity", "Erro ao remover a imagem: ${e.message}")
                    }
                }
            } else if (selectedProfileImageUri != null) {
                // nova imagem selecionada, e eliminar antiga se existir
                if (imagePath.isNotEmpty()) {
                    try {
                        ImageUtils.deleteImage(this@EditarPerfilActivity, userId, "profile")
                    } catch (e: Exception) {
                        Log.e("EditarPerfilActivity", "Erro ao eliminar imagem antiga: ${e.message}")
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

                    // update do caminho para a nova foto
                    userData["fotoPerfilUrl"] = imagePath
                    hasFotoPerfil = true
                } catch (e: Exception) {
                    Toast.makeText(this@EditarPerfilActivity,
                        getString(R.string.erro_ao_salvar_imagem) + "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            updateUserData(userId, userData, progressDialog)
        }
    }

    private fun updateUserData(userId: String, userData: HashMap<String, String>, progressDialog: ProgressDialog) {
        db.collection("usuarios").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, getString(R.string.dados_atualizados), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this,
                    getString(R.string.erro_ao_atualizar_dados) + "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedProfileImageUri = data.data
            val imageView = findViewById<ImageView>(R.id.imgPerfil)
            imageView.setImageURI(selectedProfileImageUri)

            // reset da flag da foto marcada para remoção
            fotoMarcadaParaRemocao = false

            // e tem foto
            hasFotoPerfil = true
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}