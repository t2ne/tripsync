package com.example.tripsync.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tripsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //mmmmmmmmmmm
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        // lgnbtn
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                    getString(R.string.preencha_todos_os_campos), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //mais prgrss dialog
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.a_iniciar_sessao))
            progressDialog.show()

            // bulk of the code, autenticar user na firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog.dismiss()

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // ver se o email ta verificado or nah
                        if (user != null && user.isEmailVerified) {
                            //se sim redirecionar para a home
                            updateUserVerificationStatus(user.uid) // enquanto se mantém tudo sincronizado
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            // not verificatreds men ))
                            Toast.makeText(
                                this,
                                getString(R.string.verifique_o_seu_email),
                                Toast.LENGTH_LONG
                            ).show()

                            // reenvio se o usr quiser
                            AlertDialog.Builder(this)
                                .setTitle(getString(R.string.email_nao_verificado))
                                .setMessage(getString(R.string.deseja_reenviar_o_email))
                                .setPositiveButton(getString(R.string.sim)) { dialog, _ ->
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    getString(R.string.email_reenviado),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    getString(R.string.erro_ao_reenviar_email) +  " ${verificationTask.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    dialog.dismiss()
                                }
                                .setNegativeButton(getString(R.string.nao)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()

                            // logout do usr
                            auth.signOut()
                        }
                    } else {
                        // Falha no login
                        Toast.makeText(
                            this,
                            getString(R.string.falha_no_login) + " ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // intent se quiser registar
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // link recuperar senha
        forgotPassword.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.insira_o_seu_email_para_recuperar_a_senha),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            getString(R.string.email_de_recuperacao_enviado_para) +  " $email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.erro_ao_enviar_email_de_recuperacao) + " ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // email verified? se sim passa de activity
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
                    // handle do erro, dev shi
                    Log.e("MainActivity", "Erro ao atualizar status de verificação: ${e.message}")
                }
        }
    }
}