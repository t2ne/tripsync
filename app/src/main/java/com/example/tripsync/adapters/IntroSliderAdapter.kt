package com.example.tripsync.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.models.IntroSlide

class IntroSliderAdapter(private val context: Context) :
    RecyclerView.Adapter<IntroSliderAdapter.IntroSlideViewHolder>() {

        // lista dos 3 slides q vão aparecer no intro slider
    private val slides = listOf(
        IntroSlide(
            R.drawable.item_1,
            context.getString(R.string.bem_vindo_ao_tripsync),
            context.getString(R.string.a_unica_aplicacao_que)
        ),
        IntroSlide(
            R.drawable.item_2,
            context.getString(R.string.farto_de_confusoes),
            context.getString(R.string.tu_decides_o_que_fazer)
        ),
        IntroSlide(
            R.drawable.item_3,
            context.getString(R.string.estas_pronto),
            context.getString(R.string.a_tua_jornada_comeca_aqui)
        )
    )

    // viewHolder para cada slide
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSlideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.slide_item, parent, false
        )
        return IntroSlideViewHolder(view)
    }

    override fun getItemCount(): Int = slides.size

    override fun onBindViewHolder(holder: IntroSlideViewHolder, position: Int) {
        holder.bind(slides[position])
    }

    // viewHolder para cada slide
    inner class IntroSlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageSlide = view.findViewById<ImageView>(R.id.imageSlide)
        private val titleSlide = view.findViewById<TextView>(R.id.titleSlide)
        private val descriptionSlide = view.findViewById<TextView>(R.id.descriptionSlide)

        fun bind(introSlide: IntroSlide) {
            imageSlide.setImageResource(introSlide.image)
            titleSlide.text = introSlide.title
            descriptionSlide.text = introSlide.description
        }
    }

}