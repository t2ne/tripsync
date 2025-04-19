package com.example.tripsync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// data class q representa uma foto de viagem, useful asf
@Parcelize
data class FotoViagem(
    val id: String = "",
    val fotoUrl: String = "",
    val descricao: String = "",
    val data: String = "",
    val classificacao: String = "",
    val localId: String = "",
    val isDeleted: Boolean = false
) : Parcelable