package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtigoDao {
    @Query("SELECT * FROM artigos ORDER BY nome ASC")
    fun getAll(): Flow<List<Artigo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artigo: Artigo)

    @Update
    suspend fun update(artigo: Artigo)

    @Query("DELETE FROM artigos WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Função que estava faltando, necessária para o ArtigoViewModel
    @Query("SELECT * FROM artigos WHERE id = :id")
    suspend fun getById(id: Long): Artigo?

    // Função que estava faltando, necessária para o ArquivosRecentesViewModel
    @Query("SELECT * FROM artigos WHERE guardar_fatura = 1 ORDER BY id DESC")
    fun getArtigosRecentes(): Flow<List<Artigo>>
}