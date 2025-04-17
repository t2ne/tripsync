// app/src/main/java/com/example/tripsync/database/ImageDao.kt -
package com.example.tripsync.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE id = :id AND type = :type")
    suspend fun getImage(id: String, type: String): ImageEntity?

    @Query("DELETE FROM images WHERE id = :id AND type = :type")
    suspend fun deleteImage(id: String, type: String)
}