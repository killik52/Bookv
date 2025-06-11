package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun getAll(): Flow<List<Cliente>> // Retorna todos os clientes

    @Query("SELECT * FROM clientes WHERE bloqueado = 1 ORDER BY nome ASC")
    fun getBloqueados(): Flow<List<Cliente>> // Retorna apenas clientes bloqueados

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

    // Método para obter todos os clientes como List (útil para operações sem LiveData)
    @Query("SELECT * FROM clientes")
    suspend fun getAllClientesList(): List<Cliente>
}

@Dao
interface ClienteBloqueadoDao {
    @Query("SELECT * FROM clientes_bloqueados ORDER BY nome ASC")
    fun getAll(): Flow<List<ClienteBloqueado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clienteBloqueado: ClienteBloqueado): Long

    @Query("DELETE FROM clientes_bloqueados WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Update // Adicionado para permitir atualizar ClienteBloqueado
    suspend fun update(clienteBloqueado: ClienteBloqueado)

    @Query("SELECT * FROM clientes_bloqueados WHERE nome = :nomeCliente ORDER BY id DESC LIMIT 1")
    suspend fun getByNome(nomeCliente: String): ClienteBloqueado?
}