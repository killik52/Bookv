package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaWithDetails
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    private val _faturasRecentes = MutableLiveData<List<FaturaWithDetails>>()
    val faturasRecentes: LiveData<List<FaturaWithDetails>> = _faturasRecentes

    private val _totalFaturasHoje = MutableLiveData<Double>()
    val totalFaturasHoje: LiveData<Double> = _totalFaturasHoje

    init {
        loadFaturasRecentes()
        loadTotalFaturasHoje()
    }

    private fun loadFaturasRecentes() {
        viewModelScope.launch {
            db.faturaDao().getLast30DaysFaturasWithDetails().observeForever {
                _faturasRecentes.value = it
            }
        }
    }

    private fun loadTotalFaturasHoje() {
        viewModelScope.launch {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            db.faturaDao().getFaturasByDateRange(startOfDay, endOfDay).observeForever { faturas ->
                _totalFaturasHoje.value = faturas.sumOf { it.fatura.valorTotal }
            }
        }
    }

    fun addFatura(fatura: Fatura, faturaItems: List<FaturaItem>) {
        viewModelScope.launch {
            db.faturaDao().insertFaturaWithItems(fatura, faturaItems)
        }
    }

    fun getFaturaById(faturaId: Int): LiveData<FaturaWithDetails?> {
        return db.faturaDao().getFaturaWithDetails(faturaId)
    }

    // Corrigido para garantir que o tipo do parâmetro é inferível
    fun filterFaturas(query: String): LiveData<List<FaturaWithDetails>> {
        val searchResult = MutableLiveData<List<FaturaWithDetails>>()
        viewModelScope.launch {
            val filteredList = _faturasRecentes.value?.filter { faturaWithDetails -> // Explicitamente nomear o parâmetro
                val fatura = faturaWithDetails.fatura
                val cliente = faturaWithDetails.cliente
                val nomeClienteMatches = cliente?.nome?.contains(query, ignoreCase = true) ?: false
                val idFaturaMatches = fatura.id.toString().contains(query, ignoreCase = true)
                nomeClienteMatches || idFaturaMatches
            } ?: emptyList()
            searchResult.postValue(filteredList)
        }
        return searchResult
    }
}
