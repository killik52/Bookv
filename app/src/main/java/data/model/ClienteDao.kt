package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long

    @Update
    suspend fun update(cliente: Cliente)

    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun getAll(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: Long): Cliente?

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun deleteById(id: Long)
}