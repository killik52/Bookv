package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Cliente
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

    @Query("SELECT * FROM clientes WHERE nome = :nomeCliente LIMIT 1")
    suspend fun getByName(nomeCliente: String): Cliente?

    @Query("SELECT * FROM clientes WHERE nome LIKE :query OR email LIKE :query OR telefone LIKE :query OR cpf LIKE :query OR cnpj LIKE :query")
    fun searchClientes(query: String): Flow<List<Cliente>>
}
