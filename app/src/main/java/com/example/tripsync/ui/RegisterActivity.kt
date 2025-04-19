package com.example.tripsync.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

            // validationes
            when {
                email.isEmpty() -> {
                    emailInput.error = getString(R.string.informe_o_email)
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    passwordInput.error = getString(R.string.informe_a_password)
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordInput.error = getString(R.string.password_6_chars_min)
                    return@setOnClickListener
                }
                nome.isEmpty() -> {
                    nomeInput.error = getString(R.string.informe_o_seu_nome)
                    return@setOnClickListener
                }
                username.isEmpty() -> {
                    usernameInput.error = getString(R.string.informe_o_seu_username)
                    return@setOnClickListener
                }
            }

            // progress dialog, usado para mostrar que o registo está a ser processado
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.a_registar_utilizador))
            progressDialog.show()

            // create
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // enviatings of the verificationes amaricans
                        val user = auth.currentUser

                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.email_de_verificacao_enviado_para) + " $email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        val userId = user?.uid

                        // hashmap para segurança do user
                        if (userId != null) {
                            val userData = hashMapOf(
                                "nome" to nome,
                                "username" to username,
                                "email" to email,
                                "fotoPerfilUrl" to "",
                                "emailVerificado" to false
                            )

                            // e depois salvar os dados do user no firestore
                            db.collection("usuarios")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()

                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this,
                                        getString(R.string.erro_ao_salvar_dados) + " ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        // caso falhar for some reason
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            getString(R.string.erro_ao_registar) + " ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // back button
        backButton.setOnClickListener {
            finish()
        }
    }
}