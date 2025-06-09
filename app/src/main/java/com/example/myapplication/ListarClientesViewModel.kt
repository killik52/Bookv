package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ClienteRepository
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.launch

class ListarClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClienteRepository

    val todosClientes: LiveData<List<Cliente>>

    init {
        val clienteDao = AppDatabase.getDatabase(application).clienteDao()
        repository = ClienteRepository(clienteDao)
        todosClientes = repository.todosClientes.asLiveData()
    }

    fun deletarCliente(cliente: Cliente) = viewModelScope.launch {
        repository.deletarPorId(cliente.id)
    }
}