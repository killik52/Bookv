package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun getAll(): Flow<List<Cliente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long

    @Update
    suspend fun update(cliente: Cliente)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: Long): Cliente?
}

@Dao
interface ClienteBloqueadoDao {
    @Query("SELECT * FROM clientes_bloqueados ORDER BY nome ASC")
    fun getAll(): Flow<List<ClienteBloqueado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clienteBloqueado: ClienteBloqueado)

    @Query("DELETE FROM clientes_bloqueados WHERE id = :id")
    suspend fun deleteById(id: Long)
}