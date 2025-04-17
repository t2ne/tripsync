package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        // Configurar botão de login
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar progresso
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage("A iniciar sessão...")
            progressDialog.show()

            // Autenticar com Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog.dismiss()

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // Verificar diretamente se o email está verificado no Firebase Auth
                        if (user != null && user.isEmailVerified) {
                            // Email já verificado, redirecionar para HomeActivity
                            updateUserVerificationStatus(user.uid) // Manter Firestore sincronizado
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            // Email não verificado, mostrar mensagem
                            Toast.makeText(
                                this,
                                "Por favor, verifique o seu email antes de fazer login",
                                Toast.LENGTH_LONG
                            ).show()

                            // Oferecer reenvio do email de verificação
                            AlertDialog.Builder(this)
                                .setTitle("Email não verificado")
                                .setMessage("Deseja reenviar o email de verificação?")
                                .setPositiveButton("Sim") { dialog, _ ->
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Email de verificação reenviado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Erro ao reenviar email: ${verificationTask.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Não") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()

                            // Fazer logout do usuário não verificado
                            auth.signOut()
                        }
                    } else {
                        // Falha no login
                        Toast.makeText(
                            this,
                            "Falha no login: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // Link para tela de registro
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Link para recuperação de senha
        forgotPassword.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    this, "Insira o seu email para recuperar a senha",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Email de recuperação enviado para $email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Erro ao enviar email de recuperação: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar se o usuário está logado e com email verificado
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun updateUserVerificationStatus(userId: String) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(userId)
                .update("emailVerificado", user.isEmailVerified)
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Erro ao atualizar status de verificação: ${e.message}")
                }
        }
    }
}