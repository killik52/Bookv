// app/src/main/java/data/dao/FaturaNotaDao.kt
package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.model.FaturaNota
import kotlinx.coroutines.flow.Flow

@Dao
interface FaturaNotaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(faturaNota: FaturaNota): Long

    @Update
    suspend fun update(faturaNota: FaturaNota)

    @Query("DELETE FROM fatura_notas WHERE id = :notaId")
    suspend fun delete(notaId: Long)

    @Query("SELECT * FROM fatura_notas WHERE fatura_id = :faturaId")
    fun getAllByFaturaId(faturaId: Long): Flow<List<FaturaNota>>

    @Query("DELETE FROM fatura_notas WHERE fatura_id = :faturaId")
    suspend fun deleteAllNotesForFatura(faturaId: Long)

    @Transaction
    suspend fun upsertAll(faturaId: Long, notas: List<FaturaNota>) {
        deleteAllNotesForFatura(faturaId) // Remove existing notes
        notas.forEach { note ->
            insert(note.copy(faturaId = faturaId)) // Insert new ones, ensuring correct faturaId
        }
    }
}