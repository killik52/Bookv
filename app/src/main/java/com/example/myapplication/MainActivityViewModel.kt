// app/src/main/java/com/example/myapplication/MainActivityViewModel.kt
package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.ArtigoDao
import com.example.myapplication.data.dao.ClienteDao
import com.example.myapplication.data.dao.FaturaDao
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaResumidaItem
import com.example.myapplication.data.model.FaturaWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao: FaturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val clienteDao: ClienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val artigoDao: ArtigoDao = AppDatabase.getDatabase(application).artigoDao()

    private val _faturasRecentes = MutableLiveData<List<FaturaResumidaItem>>()
    val faturasRecentes: LiveData<List<FaturaResumidaItem>> = _faturasRecentes

    private val _clientesRecentes = MutableLiveData<List<Cliente>>()
    val clientesRecentes: LiveData<List<Cliente>> = _clientesRecentes

    private val _artigosMaisVendidos = MutableLiveData<List<Pair<String, Int>>>()
    val artigosMaisVendidos: LiveData<List<Pair<String, Int>>> = _artigosMaisVendidos

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FaturaResumidaItem>>(emptyList())
    val searchResults: StateFlow<List<FaturaResumidaItem>> = _searchResults.asStateFlow()


    init {
        viewModelScope.launch {
            // Carrega faturas recentes
            faturaDao.getAllFaturasWithDetails().map { faturaWithDetailsList ->
                faturaWithDetailsList.map { converterParaFaturaResumida(it) }
            }.collect {
                _faturasRecentes.postValue(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            clienteDao.getRecentClients(5).collect {
                _clientesRecentes.postValue(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Usar o fluxo de FaturaWithDetails para agregar artigos
            faturaDao.getAllFaturasWithDetails().collect { faturaWithDetailsList ->
                val artigoVendas = mutableMapOf<String, Int>()
                faturaWithDetailsList.forEach { faturaDetails ->
                    faturaDetails.artigos.forEach { item ->
                        artigoVendas[item.nomeArtigo] = artigoVendas.getOrDefault(item.nomeArtigo, 0) + item.quantidade
                    }
                }
                _artigosMaisVendidos.postValue(artigoVendas.toList().sortedByDescending { it.second })
            }
        }

        // Configura o fluxo de busca
        _searchText.combine(faturaDao.getAllFaturasWithDetails()) { query, allFaturasWithDetails ->
            if (query.isBlank()) {
                _isSearching.value = false
                emptyList()
            } else {
                _isSearching.value = true
                val filtered = allFaturasWithDetails.filter { faturaWithDetails ->
                    faturaWithDetails.fatura.numeroFatura?.contains(query, ignoreCase = true) == true ||
                            faturaWithDetails.fatura.cliente?.contains(query, ignoreCase = true) == true
                }.map { converterParaFaturaResumida(it) }
                filtered
            }
        }.flatMapLatest {
            MutableStateFlow(it) // Transforma a lista filtrada em um novo Flow
        }.collect {
            _searchResults.value = it
        }
    }


    fun updateSearchText(query: String) {
        _searchText.value = query
    }

    private fun converterParaFaturaResumida(faturaWithDetails: FaturaWithDetails): FaturaResumidaItem {
        val fatura = faturaWithDetails.fatura
        val totalItens = faturaWithDetails.artigos.sumOf { it.quantidade }
        val totalFotos = faturaWithDetails.fotos.size
        return FaturaResumidaItem(
            id = fatura.id,
            numeroFatura = fatura.numeroFatura,
            cliente = fatura.cliente,
            data = fatura.data,
            subtotal = fatura.subtotal,
            saldoDevedor = fatura.saldoDevedor,
            foiEnviada = fatura.foiEnviada,
            totalItens = totalItens,
            totalFotos = totalFotos
        )
    }

    fun getTotalClientes(): Flow<Int> {
        return clienteDao.getTotalClientes()
    }

    fun getTotalArtigos(): Flow<Int> {
        return artigoDao.getTotalArtigos()
    }

    fun getTotalFaturas(): Flow<Int> {
        return faturaDao.getTotalFaturas()
    }
}