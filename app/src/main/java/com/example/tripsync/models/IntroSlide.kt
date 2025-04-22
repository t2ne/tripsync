package com.example.tripsync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntroSlide(
    val image: Int,
    val title: String,
    val description: String
) : Parcelable