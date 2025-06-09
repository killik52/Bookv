package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtigoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artigo: Artigo): Long

    @Update
    suspend fun update(artigo: Artigo)

    @Query("SELECT * FROM artigos WHERE guardarFatura = 1 ORDER BY id DESC")
    fun getArtigosRecentes(): Flow<List<Artigo>>

    @Query("SELECT * FROM artigos WHERE id = :id")
    suspend fun getById(id: Long): Artigo?
}