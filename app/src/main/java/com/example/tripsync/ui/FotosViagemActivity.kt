package com.example.tripsync.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.adapters.FotosAdapter
import com.example.tripsync.models.FotoViagem
import com.example.tripsync.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

// finalmente, fotos da viagem actvt
class FotosViagemActivity : AppCompatActivity() {

    private lateinit var recyclerFotos: RecyclerView
    private lateinit var btnAdicionarFotos: Button
    private lateinit var tvSemFotos: TextView
    private lateinit var tvTituloViagem: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var viagemId: String
    private lateinit var nomeViagem: String
    private var locaisViagem: List<String> = emptyList()
    private val fotosList = mutableListOf<FotoViagem>()
    private var fotosAdapter: FotosAdapter? = null

    // const para por um máximo de fotos at once
    companion object {
        private const val PICK_MULTIPLE_IMAGES = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fotos_viagem)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ir buscar dados ao intent da edit viagem
        viagemId = intent.getStringExtra("viagemId") ?: ""
        nomeViagem = intent.getStringExtra("nomeViagem") ?: getString(R.string.fotos_da_viagem)

        // init das views
        recyclerFotos = findViewById(R.id.recyclerFotos)
        btnAdicionarFotos = findViewById(R.id.btnAdicionarFotos)

        // config recycler view
        recyclerFotos.layoutManager = GridLayoutManager(this, 3)

        // quase último bckbtn
        findViewById<ImageView>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        // btn de add das fotos
        btnAdicionarFotos.setOnClickListener {
            selecionarMultiplasFotos()
        }

        // ir buscar os dados da viagem (locais em ids)
        carregarDadosViagem()
    }

    private fun carregarDadosViagem() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // ir buscar a lista dos locais para depois dar o link dos locais entre as fotos tmb
                    locaisViagem = document.get("locais") as? List<String> ?: emptyList()

                    // loadfotos
                    carregarFotos()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_carregar_dados_da_viagem) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarFotos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .get()
            .addOnSuccessListener { documents ->
                fotosList.clear()

                for (document in documents) {
                    val foto = document.toObject(FotoViagem::class.java).copy(id = document.id)
                    if (!foto.isDeleted) {
                        fotosList.add(foto)
                    }
                }
                // ordenar fotos pela data
                fotosList.sortByDescending { it.data }

                // update do recycler
                fotosAdapter = FotosAdapter(fotosList) { foto ->
                    mostrarDetalhesFoto(foto)
                }
                recyclerFotos.adapter = fotosAdapter

                // e finally update do UI
                atualizarUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_carregar_fotos) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // update do UI, kinda useless mas pronto
    private fun atualizarUI() {
        if (fotosList.isEmpty()) {
            recyclerFotos.visibility = View.GONE
        } else {
            recyclerFotos.visibility = View.VISIBLE
        }
    }

    // selecionar multiplas fotos da galeria, downloads etc
    private fun selecionarMultiplasFotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selecione_fotos)), PICK_MULTIPLE_IMAGES)
    }

    private fun mostrarDetalhesFoto(foto: FotoViagem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detalhes_foto, null)
        val imgFotoDetalhe = dialogView.findViewById<ImageView>(R.id.imgFotoDetalhe)
        val etDescricaoFoto = dialogView.findViewById<EditText>(R.id.etDescricaoFoto)
        val etDataFoto = dialogView.findViewById<EditText>(R.id.etDataFoto)
        val etClassificacaoFoto = dialogView.findViewById<EditText>(R.id.etClassificacaoFoto)
        val spinnerLocal = dialogView.findViewById<Spinner>(R.id.spinnerLocal)
        val btnExcluirFoto = dialogView.findViewById<Button>(R.id.btnExcluirFoto)
        val btnSalvarDetalhesFoto = dialogView.findViewById<Button>(R.id.btnSalvarDetalhesFoto)

        // criar o alert dialog para mostrar detalhes - MOVIDO PARA CIMA
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // show the img
        val file = File(foto.fotoUrl)
        if (file.exists()) {
            imgFotoDetalhe.setImageURI(Uri.fromFile(file))
        }

        // fill dos campos com os detalhes ditos nel PDF
        etDescricaoFoto.setText(foto.descricao)
        etDataFoto.setText(foto.data)
        etClassificacaoFoto.setText(foto.classificacao)

        // spinner para locais (dropdown)
        val locaisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locaisViagem)
        locaisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocal.adapter = locaisAdapter

        // se a foto já tiver um localId, selecionar o local correspondente no spinner
        if (foto.localId.isNotEmpty()) {
            try {
                val index = foto.localId.toInt()
                if (index in 0 until locaisViagem.size) {
                    spinnerLocal.setSelection(index)
                }
            } catch (e: NumberFormatException) {
                // se não for um num ignorar
            }
        }

        btnExcluirFoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.excluir_foto))
                .setMessage(getString(R.string.tem_certeza_que_deseja_excluir_esta_foto))
                .setPositiveButton(getString(R.string.sim)) { confirmDialog, _ ->
                    excluirFoto(foto)
                    confirmDialog.dismiss()
                    dialog.dismiss() // variavel dialog já definida
                }
                .setNegativeButton(getString(R.string.nao)) { confirmDialog, _ ->
                    confirmDialog.dismiss()
                }
                .show()
        }

        // config do save btn, carrega e os dados são updated
        btnSalvarDetalhesFoto.setOnClickListener {
            val descricao = etDescricaoFoto.text.toString()
            val data = etDataFoto.text.toString()
            val classificacao = etClassificacaoFoto.text.toString()
            val localIndex = spinnerLocal.selectedItemPosition.toString()

            atualizarDetalhesFoto(foto, descricao, data, classificacao, localIndex)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun excluirFoto(foto: FotoViagem) {
        val userId = auth.currentUser?.uid ?: return

        // referência ao doc da foto
        val fotoRef = db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .document(foto.id)

        // excluir completamente o doc da foto
        fotoRef.delete()
            .addOnSuccessListener {
                // delete o arquivo do armazenamento interno
                try {
                    val file = File(foto.fotoUrl)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // ignorar erros tbh, not worth it
                }

                // update da lista e do adapter
                fotosList.remove(foto)
                fotosAdapter?.notifyDataSetChanged()
                atualizarUI()

                Toast.makeText(this,
                    getString(R.string.foto_excluida_com_sucesso), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_excluir_foto) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarDetalhesFoto(foto: FotoViagem, descricao: String, data: String, classificacao: String, localId: String) {
        val userId = auth.currentUser?.uid ?: return

        // update dos detalhes da foto na firestore, hashmap again para segurança
        val fotoAtualizada = hashMapOf(
            "descricao" to descricao,
            "data" to data,
            "classificacao" to classificacao,
            "localId" to localId
        )

        db.collection("usuarios")
            .document(userId)
            .collection("viagens")
            .document(viagemId)
            .collection("fotos")
            .document(foto.id)
            .update(fotoAtualizada as Map<String, Any>)
            .addOnSuccessListener {
                // nova instance do obj :)
                val fotoAtualizado = FotoViagem(
                    id = foto.id,
                    fotoUrl = foto.fotoUrl,
                    descricao = descricao,
                    data = data,
                    classificacao = classificacao,
                    localId = localId,
                    isDeleted = foto.isDeleted
                )

                // find e substitute na lista
                val index = fotosList.indexOfFirst { it.id == foto.id }
                if (index != -1) {
                    fotosList[index] = fotoAtualizado
                    fotosAdapter?.notifyItemChanged(index)
                }

                Toast.makeText(this,
                    getString(R.string.detalhes_atualizados_com_sucesso), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.erro_ao_atualizar_detalhes) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MULTIPLE_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val clipData = intent.clipData
                val singleUri = intent.data

                val imageUris = mutableListOf<Uri>()

                if (clipData != null) {
                    // seleção de múltiplas imagens
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        imageUris.add(uri)
                    }
                } else if (singleUri != null) {
                    // selectn de uma única imagem
                    imageUris.add(singleUri)
                }

                // simple check
                if (imageUris.isNotEmpty()) {
                    processarFotosSelecionadas(imageUris)
                }
            }
        }
    }

    private fun processarFotosSelecionadas(uris: List<Uri>) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            // mostrar ao user o progresso
            val loadingDialog = AlertDialog.Builder(this@FotosViagemActivity)
                .setMessage(getString(R.string.a_processar_fotos))
                .setCancelable(false)
                .create()
            loadingDialog.show()

            try {
                // processar cada imagem
                for (uri in uris) {
                    // new id unico para cada foto
                    val fotoId = UUID.randomUUID().toString()

                    // salvar imagem no storage interno
                    val fotoPath = ImageUtils.saveImageToInternalStorage(
                        this@FotosViagemActivity,
                        uri,
                        "$viagemId-$fotoId",
                        "photo"
                    )

                    // new doc para a foto
                    val novaFoto = FotoViagem(
                        id = fotoId,
                        fotoUrl = fotoPath,
                        descricao = "",
                        data = "",
                        classificacao = "",
                        localId = "", // init sem local, mas depois pode ser atualizado
                        isDeleted = false
                    )

                    // save para a firestore
                    db.collection("usuarios")
                        .document(userId)
                        .collection("viagens")
                        .document(viagemId)
                        .collection("fotos")
                        .document(fotoId)
                        .set(novaFoto)
                }

                // finalize finalmente final fin
                loadingDialog.dismiss()
                Toast.makeText(this@FotosViagemActivity,
                    getString(R.string.fotos_adicionadas_com_sucesso), Toast.LENGTH_SHORT).show()

                // Recarregar as fotos
                carregarFotos()

            } catch (e: Exception) {
                loadingDialog.dismiss()
                // erro ao processar as fotos, again just a lil toast
                Toast.makeText(this@FotosViagemActivity,
                    getString(R.string.erro_ao_processar_fotos) + " ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}