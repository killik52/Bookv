package com.example.myapplication.data

import com.example.myapplication.data.dao.ClienteDao
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.flow.Flow

class ClienteRepository(private val clienteDao: ClienteDao) {

    val todosClientes: Flow<List<Cliente>> = clienteDao.getAll()

    // Corrigido para retornar o Long (ID do novo cliente)
    suspend fun inserir(cliente: Cliente): Long {
        return clienteDao.insert(cliente)
    }

    suspend fun deletarPorId(id: Long) {
        clienteDao.deleteById(id)
    }
}