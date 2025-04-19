package com.example.tripsync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Viagem(
    var id: String = "",
    val nome: String = "",
    val data: String = "",
    val descricao: String = "",
    val classificacao: String = "",
    val fotoUrl: String = ""
) : Parcelable