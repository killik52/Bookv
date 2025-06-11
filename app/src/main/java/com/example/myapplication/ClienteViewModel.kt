package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.ClienteRepository
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.FaturaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val _artigos = MutableLiveData<List<Artigo>>()
    val artigos: LiveData<List<Artigo>> = _artigos

    private val db = AppDatabase.getDatabase(application) // Corrigido para obter a instância do DB

    init {
        // Inicializar os clientes e artigos ao criar o ViewModel
        loadClientes()
        loadArtigos()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            db.clienteDao().getAll().observeForever {
                _clientes.value = it
            }
        }
    }

    private fun loadArtigos() {
        viewModelScope.launch {
            db.artigoDao().getAllArtigos().observeForever {
                _artigos.value = it
            }
        }
    }

    fun insertCliente(cliente: Cliente) {
        viewModelScope.launch {
            db.clienteDao().insert(cliente)
        }
    }

    fun updateCliente(cliente: Cliente) {
        viewModelScope.launch {
            db.clienteDao().update(cliente)
        }
    }

    fun deleteCliente(cliente: Cliente) {
        viewModelScope.launch {
            db.clienteDao().delete(cliente)
        }
    }

    fun toggleBloqueioCliente(cliente: Cliente) {
        viewModelScope.launch {
            val updatedCliente = cliente.copy(bloqueado = !cliente.bloqueado)
            db.clienteDao().update(updatedCliente)
        }
    }

    fun addFaturaItem(faturaItem: FaturaItem) {
        viewModelScope.launch {
            db.faturaDao().insertFaturaItem(faturaItem)
        }
    }

    fun getArtigoById(artigoId: Int): LiveData<Artigo?> {
        return db.artigoDao().getArtigoById(artigoId)
    }

    fun getArtigoBySerialOrName(query: String): LiveData<List<Artigo>> {
        return db.artigoDao().searchArtigos(query)
    }

    // Corrigido para garantir que o tipo do parâmetro é inferível
    fun filterArtigos(query: String): List<Artigo> {
        return artigos.value?.filter { artigo -> // Explicitamente nomear o parâmetro 'artigo'
            val nomeMatches = artigo.nome?.contains(query, ignoreCase = true) ?: false
            val descricaoMatches = artigo.descricao?.contains(query, ignoreCase = true) ?: false
            val serialMatches = artigo.numeroSerial?.contains(query, ignoreCase = true) ?: false
            nomeMatches || descricaoMatches || serialMatches
        } ?: emptyList() // Retorna uma lista vazia se artigos.value for nulo
    }

}
