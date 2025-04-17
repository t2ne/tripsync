// app/src/main/java/com/example/tripsync/database/ImageEntity.kt
package com.example.tripsync.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey
    val id: String,
    val imagePath: String,
    val type: String,
    val createdAt: Long = System.currentTimeMillis()
)