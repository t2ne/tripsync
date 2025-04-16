package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Iniciar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Validações
            if (email.isEmpty()) {
                emailInput.error = "Informe o email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Informe a senha"
                return@setOnClickListener
            }

            // Autenticação com Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login bem-sucedido, verificar se o email foi verificado
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            // Email verificado, permitir acesso ao app
                            Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Email não verificado, mostrar mensagem
                            auth.signOut() // Encerrar sessão
                            showEmailVerificationDialog()
                        }
                    } else {
                        // Falha no login
                        Toast.makeText(
                            this, "Falha no login: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

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
                            this, "Email de recuperação enviado para $email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this, "Erro ao enviar email de recuperação",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    @Override
    override fun onStart() {
        super.onStart()
        // Verificar se o usuário está logado E se o email está verificado
        val currentUser = auth.currentUser

        // Verificação simplificada: se estiver logado e email verificado, vai para Home
        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else if (currentUser != null) {
            // Usuário logado mas sem email verificado
            Toast.makeText(
                this,
                "É necessário verificar o seu email antes de usar a app.",
                Toast.LENGTH_LONG
            ).show()
            auth.signOut()
        }
        // Caso contrário, permanece na tela de login (atual)
    }

    private fun showEmailVerificationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Verificação de Email")
            .setMessage("Enviámos um email de verificação para o seu endereço de email. Por favor, verifique a sua caixa de entrada e confirme-o para continuar.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }
}