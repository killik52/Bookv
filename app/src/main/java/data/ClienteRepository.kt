package com.example.myapplication.data

import com.example.myapplication.data.dao.ClienteDao
import com.example.myapplication.data.model.Cliente

class ClienteRepository(private val clienteDao: ClienteDao) {

    val todosClientes = clienteDao.getAll()

    suspend fun inserir(cliente: Cliente) {
        clienteDao.insert(cliente)
    }

    suspend fun deletarPorId(id: Long) {
        clienteDao.deleteById(id)
    }
}