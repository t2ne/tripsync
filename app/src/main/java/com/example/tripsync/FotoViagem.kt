package com.example.tripsync

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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