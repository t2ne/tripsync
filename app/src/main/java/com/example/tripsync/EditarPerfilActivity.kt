package com.example.tripsync

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditarPerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        // Configurar o botão de voltar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Finalizar a atividade e voltar para a tela anterior
            onBackPressed() // Isso chama o comportamento padrão de voltar
        }

        // Configurar o botão de salvar
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        btnSalvar.setOnClickListener {
            // Aqui você pode adicionar qualquer lógica para salvar as informações
            // Por exemplo, verificar se os campos estão preenchidos ou salvar dados no banco.

            // Após salvar, voltar para a HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent) // Inicia a HomeActivity
            finish() // Finaliza a EditarPerfilActivity para que o usuário não volte para ela ao pressionar o "Voltar"
        }
    }
}
