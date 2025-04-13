package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailInput = findViewById<EditText>(R.id.registerEmail)
        val passwordInput = findViewById<EditText>(R.id.registerPassword)
        val registerButton = findViewById<Button>(R.id.btnRegistar)
        val backButton = findViewById<ImageView>(R.id.btnBack)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Conta criada para $email", Toast.LENGTH_SHORT).show()

                // Ir para a HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish() // Volta ao MainActivity
        }
    }
}
