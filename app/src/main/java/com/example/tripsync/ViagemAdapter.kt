package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViagemAdapter(
    private val viagens: List<Viagem>,
    private val onItemClick: (Viagem) -> Unit,
    private val onShareClick: (Int) -> Unit
) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

    class ViagemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeTextView: TextView = view.findViewById(R.id.nomeViagem)
        val dataTextView: TextView = view.findViewById(R.id.dataViagem)
        val btnShare: ImageView = view.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viagem_swipe, parent, false)
        return ViagemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
        val viagem = viagens[position]

        holder.nomeTextView.text = viagem.nome
        holder.dataTextView.text = if (viagem.data.isNotEmpty()) viagem.data else "Sem data"

        // Configurar clique no item
        holder.itemView.setOnClickListener {
            onItemClick(viagem)
        }

        // Configurar clique no botão de compartilhar
        holder.btnShare.setOnClickListener {
            onShareClick(position)
        }
    }

    override fun getItemCount() = viagens.size
}