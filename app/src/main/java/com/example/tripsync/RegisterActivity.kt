package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase Auth e Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.registerEmail)
        val passwordInput = findViewById<EditText>(R.id.registerPassword)
        val nomeInput = findViewById<EditText>(R.id.registerNome)
        val usernameInput = findViewById<EditText>(R.id.registerUsername)
        val registerButton = findViewById<Button>(R.id.btnRegistar)
        val backButton = findViewById<ImageView>(R.id.btnBack)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val nome = nomeInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()

            // Validações
            when {
                email.isEmpty() -> {
                    emailInput.error = "Informe o email"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    passwordInput.error = "Informe a senha"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordInput.error = "A senha deve ter pelo menos 6 caracteres"
                    return@setOnClickListener
                }
                nome.isEmpty() -> {
                    nomeInput.error = "Informe seu nome"
                    return@setOnClickListener
                }
                username.isEmpty() -> {
                    usernameInput.error = "Informe seu username"
                    return@setOnClickListener
                }
            }

            // Mostrar progresso
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage("Registrando usuário...")
            progressDialog.show()

            // Criar usuário no Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro bem-sucedido, salvar dados adicionais no Firestore
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            val userData = hashMapOf(
                                "nome" to nome,
                                "username" to username,
                                "email" to email,
                                "fotoPerfilUrl" to ""
                            )

                            db.collection("usuarios")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Registro realizado com sucesso!",
                                        Toast.LENGTH_SHORT).show()

                                    // Redirecionar para HomeActivity
                                    val intent = Intent(this, HomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Erro ao salvar dados: ${e.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Falha no registro
                        progressDialog.dismiss()
                        Toast.makeText(this, "Erro no registro: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Botão voltar
        backButton.setOnClickListener {
            finish()
        }
    }
}