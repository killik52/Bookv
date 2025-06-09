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
            val artigosMap = mutableMapOf<String, ResumoArtigoItem>()
            faturas.forEach { fatura ->
                // Agora fatura.artigos é uma List<ArtigoItem>
                fatura.artigos?.forEach { artigoItem ->
                    val nomeArtigo = artigoItem.nome
                    val quantidade = artigoItem.quantidade ?: 0
                    val precoTotalItem = artigoItem.preco ?: 0.0 // O preço aqui é unitário, então multiplique pela quantidade
                    val artigoId = artigoItem.id

                    if (!nomeArtigo.isNullOrEmpty() && quantidade > 0) {
                        val itemAtual = artigosMap.getOrPut(nomeArtigo) {
                            ResumoArtigoItem(nomeArtigo, 0, 0.0, artigoId)
                        }
                        artigosMap[nomeArtigo] = itemAtual.copy(
                            quantidadeTotalVendida = itemAtual.quantidadeTotalVendida + quantidade,
                            valorTotalVendido = itemAtual.valorTotalVendido + (precoTotalItem * quantidade) // Multiplique o preço unitário pela quantidade
                        )
                    }
                }
            }
            _resumoArtigo.postValue(artigosMap.values.sortedByDescending { it.valorTotalVendido })
        }
    }
}