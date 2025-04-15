// app/src/main/java/com/example/tripsync/database/ImageEntity.kt
package com.example.tripsync.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey
    val id: String,
    val imagePath: String,
    val type: String, // "profile" ou "trip"
    val createdAt: Long = System.currentTimeMillis()
)