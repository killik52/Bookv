package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData // Importar asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaWithDetails
import com.example.myapplication.data.model.Artigo // Importar Artigo
import com.example.myapplication.ResumoMensalItem // Importar ResumoMensalItem
import com.example.myapplication.ResumoClienteItem // Importar ResumoClienteItem
import com.example.myapplication.ResumoArtigoItem // Importar ResumoArtigoItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat // Importar SimpleDateFormat
import java.util.Locale // Importar Locale
import java.util.Date

class ResumoFinanceiroViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val artigoDao = AppDatabase.getDatabase(application).artigoDao() // Adicionar artigoDao para buscar nome do artigo

    private val _resumoMensal = MutableLiveData<List<ResumoMensalItem>>()
    val resumoMensal: LiveData<List<ResumoMensalItem>> = _resumoMensal

    private val _resumoCliente = MutableLiveData<List<ResumoClienteItem>>()
    val resumoCliente: LiveData<List<ResumoClienteItem>> = _resumoCliente

    private val _resumoArtigo = MutableLiveData<List<ResumoArtigoItem>>()
    val resumoArtigo: LiveData<List<ResumoArtigoItem>> = _resumoArtigo

    init {
        // Inicializa com dados de todo o período
        carregarFaturamentoMensal(null, null)
        carregarResumoPorCliente(null, null)
        carregarResumoPorArtigo(null, null)
    }

    // carregarFaturamentoMensal
    fun carregarFaturamentoMensal(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getFaturasNoPeriodo(startDate, endDate).collect { faturas ->
            val resumosMap = mutableMapOf<Pair<Int, Int>, ResumoMensalItem>() // Chave: Pair(mes, ano)

            faturas.forEach { fatura ->
                val cal = Calendar.getInstance().apply {
                    // Garantir que a data é um Long (timestamp) para setTimeInMillis
                    fatura.data?.let { // Assumindo que data é String no formato "yyyy-MM-dd HH:mm:ss"
                        try {
                            val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)
                            parsedDate?.time?.let { time -> timeInMillis = time }
                        } catch (e: Exception) {
                            // Logar erro de parse ou lidar com data inválida
                            timeInMillis = 0L // Valor padrão se a data for inválida
                        }
                    }
                }
                val mes = cal.get(Calendar.MONTH) // Mês é 0-based no Calendar
                val ano = cal.get(Calendar.YEAR)
                val mesAnoString = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time) // Formato para exibição

                val itemAtual = resumosMap.getOrPut(Pair(mes, ano)) {
                    ResumoMensalItem(mesAnoString, 0.0, ano, mes, 0, 0, 0.0) // Inicializa com 0
                }

                // Atualizar valorTotal
                val valorTotal = fatura.valorTotal ?: 0.0
                resumosMap[Pair(mes, ano)] = itemAtual.copy(valorTotal = itemAtual.valorTotal + valorTotal)

                // Calcular numFaturas e numFaturasEnviadas
                val updatedNumFaturas = itemAtual.numFaturas + 1
                val updatedNumFaturasEnviadas = itemAtual.numFaturasEnviadas + (if (fatura.foiEnviada == 1) 1 else 0)

                // Recalcular saldo devedor
                var saldoDevedorAtual = itemAtual.saldoDevedor
                if (fatura.status != "Paga") {
                    saldoDevedorAtual += (fatura.saldoDevedor ?: 0.0)
                }

                resumosMap[Pair(mes, ano)] = itemAtual.copy(
                    numFaturas = updatedNumFaturas,
                    numFaturasEnviadas = updatedNumFaturasEnviadas,
                    saldoDevedor = saldoDevedorAtual
                )
            }
            _resumoMensal.postValue(resumosMap.values.sortedByDescending { it.ano * 12 + it.mes })
        }
    }

    // carregarResumoPorCliente
    fun carregarResumoPorCliente(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getResumoPorCliente(startDate, endDate).collect { resumoClienteItems ->
            _resumoCliente.postValue(resumoClienteItems)
        }
    }

    // carregarResumoPorArtigo
    fun carregarResumoPorArtigo(startDate: String?, endDate: String?) = viewModelScope.launch {
        faturaDao.getFaturasNoPeriodo(startDate, endDate).collect { faturas ->
            val artigosMap = mutableMapOf<Long, ResumoArtigoItem>()

            for (fatura in faturas) {
                // Para pegar detalhes, precisamos de FaturaWithDetails.
                // Se getFaturasNoPeriodo retorna Fatura, precisamos de um método extra para obter os itens.
                // Ou mudar getFaturasNoPeriodo para retornar FaturaWithDetails.
                // Por agora, assumimos que precisamos buscar os itens da fatura separadamente se o Flow retorna apenas Fatura.
                val faturaItems = faturaDao.getFaturaItemsByFaturaIdList(fatura.id) // Supondo este método

                faturaItems.forEach { faturaItem -> // Aqui 'faturaItem' é FaturaItem
                    val artigoId = faturaItem.artigo_id
                    val quantidade = faturaItem.quantidade
                    val precoUnitario = faturaItem.preco

                    if (artigoId != null) { // artigoId pode ser nulo se não houver FK no modelo Artigo
                        val nomeArtigo = artigoDao.getArtigoById(artigoId.toLong())?.nome ?: "Desconhecido" // Buscar nome do artigo pelo ID

                        val itemAtual = artigosMap.getOrPut(artigoId.toLong()) { // Chave long
                            ResumoArtigoItem(nomeArtigo, 0, 0.0, artigoId.toLong())
                        }
                        artigosMap[artigoId.toLong()] = itemAtual.copy(
                            quantidadeTotalVendida = itemAtual.quantidadeTotalVendida + quantidade,
                            valorTotalVendido = itemAtual.valorTotalVendido + (quantidade * precoUnitario)
                        )
                    }
                }
            }
            _resumoArtigo.postValue(artigosMap.values.sortedByDescending { it.valorTotalVendido })
        }
    }
}