package com.example.tripsync

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FotosAdapter(
    private val fotos: List<FotoViagem>,
    private val onFotoClick: (FotoViagem) -> Unit
) : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

    class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFoto: ImageView = itemView.findViewById(R.id.imgFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        // Carregar imagem a partir do caminho armazenado
        if (foto.fotoUrl.isNotEmpty()) {
            val file = File(foto.fotoUrl)
            if (file.exists()) {
                holder.imgFoto.setImageURI(Uri.fromFile(file))
            }
        }

        // Configurar clique na foto
        holder.itemView.setOnClickListener {
            onFotoClick(foto)
        }
    }

    override fun getItemCount() = fotos.size
}