package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.launch

class ListarClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    init {
        loadClientes()
    }

    fun loadClientes() {
        viewModelScope.launch {
            // Observa alterações no banco de dados e atualiza a LiveData
            db.clienteDao().getAll().observeForever {
                _clientes.value = it
            }
        }
    }

    // Corrigido para usar os nomes corretos das propriedades de Cliente e para inferência de tipo
    fun searchClientes(query: String): LiveData<List<Cliente>> {
        val searchResult = MutableLiveData<List<Cliente>>()
        viewModelScope.launch {
            val filteredList = db.clienteDao().getAllClientesList().filter { cliente -> // Explicitamente nomear o parâmetro 'cliente'
                val nomeMatches = cliente.nome?.contains(query, ignoreCase = true) ?: false
                val emailMatches = cliente.email?.contains(query, ignoreCase = true) ?: false
                val telefoneMatches = cliente.telefone?.contains(query, ignoreCase = true) ?: false
                val cnpjMatches = cliente.cnpj?.contains(query, ignoreCase = true) ?: false
                nomeMatches || emailMatches || telefoneMatches || cnpjMatches
            }
            searchResult.postValue(filteredList)
        }
        return searchResult
    }
}
