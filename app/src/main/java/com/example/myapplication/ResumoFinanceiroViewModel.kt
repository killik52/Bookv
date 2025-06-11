package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ResumoFinanceiroViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()

    private val _resumoMensal = MutableLiveData<List<ResumoMensalItem>>()
    val resumoMensal: LiveData<List<ResumoMensalItem>> = _resumoMensal

    private val _resumoCliente = MutableLiveData<List<ResumoClienteItem>>()
    val resumoCliente: LiveData<List<ResumoClienteItem>> = _resumoCliente

    private val _resumoArtigo = MutableLiveData<List<ResumoArtigoItem>>()
    val resumoArtigo: LiveData<List<ResumoArtigoItem>> = _resumoArtigo

    fun carregarFaturamentoMensal(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getFaturasNoPeriodo(startDate, endDate).collect { faturas ->
            val resumosMap = mutableMapOf<String, ResumoMensalItem>()
            faturas.forEach { fatura ->
                val ano = fatura.data?.substring(0, 4)?.toIntOrNull() ?: 0
                val mes = fatura.data?.substring(5, 7)?.toIntOrNull() ?: 0
                if (ano != 0 && mes != 0) {
                    val mesAno = String.format("%02d/%d", mes, ano)
                    val itemAtual = resumosMap.getOrPut(mesAno) {
                        ResumoMensalItem(mesAno, 0.0, ano, mes)
                    }
                    resumosMap[mesAno] = itemAtual.copy(valorTotal = itemAtual.valorTotal + (fatura.saldoDevedor ?: 0.0))
                }
            }
            _resumoMensal.postValue(resumosMap.values.sortedByDescending { it.ano * 100 + it.mes })
        }
    }

    fun carregarResumoPorCliente(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getResumoPorCliente(startDate, endDate).collect {
            _resumoCliente.postValue(it)
        }
    }

    fun carregarResumoPorArtigo(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getFaturasNoPeriodo(startDate, endDate).collect { faturas ->
            val artigosMap = mutableMapOf<Long?, ResumoArtigoItem>()
            faturas.forEach { fatura ->
                val itens = faturaDao.getFaturaWithDetails(fatura.id)?.artigos ?: emptyList()
                itens.forEach { item ->
                    val artigoId = item.artigoId
                    val quantidade = item.quantidade ?: 0
                    val precoTotalItem = item.preco ?: 0.0
                    val nomeArtigo = item.artigoId?.let { AppDatabase.getDatabase(getApplication()).artigoDao().getById(it)?.nome } ?: "Desconhecido"

                    if (nomeArtigo.isNotEmpty() && quantidade > 0) {
                        val itemAtual = artigosMap.getOrPut(artigoId) {
                            ResumoArtigoItem(nomeArtigo, 0, 0.0, artigoId)
                        }
                        artigosMap[artigoId] = itemAtual.copy(
                            quantidadeTotalVendida = itemAtual.quantidadeTotalVendida + quantidade,
                            valorTotalVendido = itemAtual.valorTotalVendido + precoTotalItem
                        )
                    }
                }
            }
            _resumoArtigo.postValue(artigosMap.values.sortedByDescending { it.valorTotalVendido })
        }
    }
}