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

    private val searchQuery = MutableStateFlow("")

    // LiveData que reage às mudanças no termo de busca e filtra os clientes
    val clientes: LiveData<List<Cliente>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            clienteDao.getAll() // getAll retorna Flow<List<Cliente>>
        } else {
            clienteDao.searchClientes("%$query%") // searchClientes também retorna Flow<List<Cliente>>
        }
    }.asLiveData() // Converte o Flow para LiveData

    // `loadClientes()` já não é necessário, pois `clientes` já está a ser observado reativamente.
    init {
        // Nada a fazer aqui se `clientes` já é reativo
    }

    // Função para ser chamada pela UI para atualizar a busca
    fun buscarClientes(query: String) {
        searchQuery.value = query
    }

    fun deletarCliente(cliente: Cliente) = viewModelScope.launch {
        repository.deletarPorId(cliente.id)
    }
}