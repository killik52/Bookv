package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ClienteRepository
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.FaturaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

class ClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val artigoDao = AppDatabase.getDatabase(application).artigoDao()
    private val faturaDao = AppDatabase.getDatabase(application).faturaDao() // Para fatura items

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val _artigos = MutableLiveData<List<Artigo>>()
    val artigos: LiveData<List<Artigo>> = _artigos

    private val _cliente = MutableLiveData<Cliente?>()
    val cliente: LiveData<Cliente?> = _cliente

    private val _seriaisAssociados = MutableLiveData<String>()
    val seriaisAssociados: LiveData<String> = _seriaisAssociados


    init {
        loadClientes()
        loadArtigos()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            clienteDao.getAll().collect { // Usar collect para Flow
                _clientes.value = it
            }
        }
    }

    private fun loadArtigos() {
        viewModelScope.launch {
            artigoDao.getAllArtigos().collect { // Usar collect para Flow
                _artigos.value = it
            }
        }
    }

    fun loadCliente(clienteId: Long) {
        viewModelScope.launch {
            val loadedCliente = clienteDao.getById(clienteId)
            _cliente.postValue(loadedCliente)

            // Lógica para carregar seriais associados ao cliente
            // Isso depende de como 'numeroSerial' se relaciona com o cliente
            // Se um Artigo está ligado a um Cliente, e um cliente pode ter vários artigos/seriais:
            val artigosDoCliente = artigoDao.getArtigosByClienteId(clienteId) // Supondo que você tem este método no ArtigoDao
            _seriaisAssociados.postValue(artigosDoCliente.mapNotNull { it.numeroSerial }.joinToString(", "))
        }
    }

    fun insertCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteDao.insert(cliente)
        }
    }

    fun updateCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteDao.update(cliente)
        }
    }

    fun deleteCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteDao.deleteById(cliente.id)
        }
    }

    fun toggleBloqueioCliente(cliente: Cliente) {
        viewModelScope.launch {
            val updatedCliente = cliente.copy(bloqueado = !cliente.bloqueado)
            clienteDao.update(updatedCliente)
        }
    }

    fun addFaturaItem(faturaItem: FaturaItem) {
        viewModelScope.launch {
            faturaDao.insertFaturaItem(faturaItem)
        }
    }

    fun getArtigoById(artigoId: Int): LiveData<Artigo?> {
        // Corrigido para converter Int para Long para o DAO
        return liveData { emit(artigoDao.getArtigoById(artigoId.toLong())) }
    }

    fun getArtigoBySerialOrName(query: String): LiveData<List<Artigo>> {
        return artigoDao.searchArtigos(query).asLiveData()
    }

    fun filterArtigos(query: String): List<Artigo> {
        return artigos.value?.filter { artigo ->
            val nomeMatches = artigo.nome?.contains(query, ignoreCase = true) ?: false
            val descricaoMatches = artigo.descricao?.contains(query, ignoreCase = true) ?: false
            val serialMatches = artigo.numeroSerial?.contains(query, ignoreCase = true) ?: false
            nomeMatches || descricaoMatches || serialMatches
        } ?: emptyList()
    }
}