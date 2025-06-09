// app/src/main/java/com/example/myapplication/ResumoFinanceiroViewModel.kt
package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.FaturaDao
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaResumidaItem
import com.example.myapplication.data.model.FaturaWithDetails
import com.example.myapplication.data.model.ResumoArtigoItem
import com.example.myapplication.data.model.ResumoClienteItem
import com.example.myapplication.data.model.ResumoMensalItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResumoFinanceiroViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao: FaturaDao = AppDatabase.getDatabase(application).faturaDao()

    private val _resumoMensal = MutableLiveData<List<ResumoMensalItem>>()
    val resumoMensal: LiveData<List<ResumoMensalItem>> = _resumoMensal

    private val _resumoPorCliente = MutableLiveData<List<ResumoClienteItem>>()
    val resumoPorCliente: LiveData<List<ResumoClienteItem>> = _resumoPorCliente

    private val _resumoPorArtigo = MutableLiveData<List<ResumoArtigoItem>>()
    val resumoPorArtigo: LiveData<List<ResumoArtigoItem>> = _resumoPorArtigo

    private val _mesesDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val mesesDisponiveis: StateFlow<List<String>> = _mesesDisponiveis.asStateFlow()

    private val _selectedMonth = MutableStateFlow<String?>(null)
    val selectedMonth: StateFlow<String?> = _selectedMonth.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            faturaDao.getMesesComFaturas().collect { months ->
                _mesesDisponiveis.value = months
                if (months.isNotEmpty() && _selectedMonth.value == null) {
                    _selectedMonth.value = months.first() // Seleciona o mês mais recente por padrão
                }
            }
        }

        _selectedMonth.flatMapLatest { selectedMonth ->
            if (selectedMonth == null) {
                flowOf(emptyList())
            } else {
                faturaDao.getFaturasPorMes(selectedMonth)
            }
        }.map { faturas ->
            val resumo = mutableMapOf<String, ResumoMensalItem>()
            faturas.forEach { fatura ->
                val mes = fatura.data?.substring(0, 7) ?: "Desconhecido" // Assume formato YYYY-MM-DD
                val current = resumo.getOrPut(mes) { ResumoMensalItem(mes, 0.0, 0, 0) }
                current.totalFaturado += fatura.subtotal ?: 0.0
                current.quantidadeFaturas++
            }
            resumo.values.toList().sortedByDescending { it.mes }
        }.collect {
            _resumoMensal.postValue(it)
        }

        viewModelScope.launch(Dispatchers.IO) {
            faturaDao.getAllFaturasWithDetails().map { faturasWithDetails ->
                val resumo = mutableMapOf<String, ResumoClienteItem>()
                faturasWithDetails.forEach { faturaDetails ->
                    val clienteNome = faturaDetails.fatura.cliente ?: "Cliente Desconhecido"
                    val current = resumo.getOrPut(clienteNome) { ResumoClienteItem(clienteNome, 0.0, 0) }
                    current.totalFaturado += faturaDetails.fatura.subtotal ?: 0.0
                    current.quantidadeFaturas++
                }
                resumo.values.toList().sortedByDescending { it.totalFaturado }
            }.collect {
                _resumoPorCliente.postValue(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            faturaDao.getAllFaturasWithDetails().map { faturasWithDetails ->
                val resumo = mutableMapOf<String, ResumoArtigoItem>()
                faturasWithDetails.forEach { faturaDetails ->
                    faturaDetails.artigos.forEach { item ->
                        val artigoNome = item.nomeArtigo ?: "Artigo Desconhecido"
                        val current = resumo.getOrPut(artigoNome) { ResumoArtigoItem(artigoNome, 0, 0.0) }
                        current.quantidadeVendida += item.quantidade
                        current.totalFaturado += item.quantidade * (item.precoUnitario ?: 0.0)
                    }
                }
                resumo.values.toList().sortedByDescending { it.totalFaturado }
            }.collect {
                _resumoPorArtigo.postValue(it)
            }
        }
    }

    fun setSelectedMonth(month: String?) {
        _selectedMonth.value = month
    }

    private fun formatarValor(valor: Double?): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "AO")).format(valor ?: 0.0)
    }

    // Função para formatar uma data de string (ex: "2024-05-30") para "Maio/2024"
    fun formatarMesAno(data: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM/yyyy", Locale("pt", "AO"))
            val date = inputFormat.parse(data)
            outputFormat.format(date).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            data // Retorna a data original se houver erro
        }
    }
}