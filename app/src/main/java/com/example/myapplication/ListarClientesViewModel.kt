package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ClienteRepository
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListarClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val repository = ClienteRepository(clienteDao)

    // Termo de busca atual
    private val searchQuery = MutableStateFlow("")

    // LiveData que reage às mudanças no termo de busca
    val clientes: LiveData<List<Cliente>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            clienteDao.getAll()
        } else {
            clienteDao.searchClientes("%$query%")
        }
    }.asLiveData()

    // Função para ser chamada pela UI para atualizar a busca
    fun buscarClientes(query: String) {
        searchQuery.value = query
    }

    fun deletarCliente(cliente: Cliente) = viewModelScope.launch {
        repository.deletarPorId(cliente.id)
    }
}