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
import com.example.myapplication.data.model.ResumoMensalItem
import kotlinx.coroutines.launch
import java.util.Calendar

class ResumoFinanceiroViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    private val _faturasMensais = MutableLiveData<List<ResumoMensalItem>>()
    val faturasMensais: LiveData<List<ResumoMensalItem>> = _faturasMensais

    init {
        loadFaturasMensais()
    }

    private fun loadFaturasMensais() {
        viewModelScope.launch {
            val allFaturas = db.faturaDao().getAllFaturasWithDetailsList() // Obter todas as faturas com detalhes
            val groupedByMonth = allFaturas.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.fatura.dataEmissao }
                Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
            }

            val resumoMensalList = mutableListOf<ResumoMensalItem>()
            groupedByMonth.forEach { (monthYear, faturasList) -> // Explicitamente nomear os parâmetros
                val month = monthYear.first
                val year = monthYear.second
                val totalFaturas = faturasList.sumOf { it.fatura.valorTotal }
                val numFaturas = faturasList.size
                val numFaturasEnviadas = faturasList.count { it.fatura.status == "Enviada" }

                // Calcula o saldo devedor
                var saldoDevedor = 0.0
                faturasList.forEach { faturaWithDetails -> // Explicitamente nomear o parâmetro
                    if (faturaWithDetails.fatura.status != "Paga") {
                        saldoDevedor += faturaWithDetails.fatura.valorTotal
                    }
                }

                resumoMensalList.add(ResumoMensalItem(month, year, totalFaturas, numFaturas, numFaturasEnviadas, saldoDevedor))
            }
            _faturasMensais.postValue(resumoMensalList.sortedByDescending { it.year * 12 + it.month })
        }
    }

    fun getFaturasByMonthAndYear(month: Int, year: Int): LiveData<List<FaturaWithDetails>> {
        val faturasLiveData = MutableLiveData<List<FaturaWithDetails>>()
        viewModelScope.launch {
            db.faturaDao().getFaturasByMonth(month, year).observeForever {
                faturasLiveData.postValue(it)
            }
        }
        return faturasLiveData
    }

    // Exemplo de função para filtrar faturas por termo de pesquisa
    fun searchFaturas(query: String): LiveData<List<FaturaWithDetails>> {
        val searchResult = MutableLiveData<List<FaturaWithDetails>>()
        viewModelScope.launch {
            val allFaturas = db.faturaDao().getAllFaturasWithDetailsList() // Obter todas as faturas
            val filteredList = allFaturas.filter { faturaWithDetails -> // Explicitamente nomear o parâmetro
                val fatura = faturaWithDetails.fatura
                val cliente = faturaWithDetails.cliente
                val nomeClienteMatches = cliente?.nome?.contains(query, ignoreCase = true) ?: false
                val idFaturaMatches = fatura.id.toString().contains(query, ignoreCase = true) // 'id' pertence a 'fatura'
                nomeClienteMatches || idFaturaMatches
            }
            searchResult.postValue(filteredList)
        }
        return searchResult
    }
}
