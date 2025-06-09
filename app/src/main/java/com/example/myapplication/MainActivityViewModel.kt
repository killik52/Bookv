package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val lixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()

    val faturas: LiveData<List<FaturaResumidaItem>> = faturaDao.getAllFaturas()
        .map { faturas ->
            faturas.map { converterParaFaturaResumida(it) }
        }.asLiveData()

    // LiveData para a fatura encontrada pelo código de barras
    private val _faturaEncontrada = MutableLiveData<Fatura?>()
    val faturaEncontrada: LiveData<Fatura?> = _faturaEncontrada

    fun moverFaturaParaLixeira(faturaResumida: FaturaResumidaItem) = viewModelScope.launch(Dispatchers.IO) {
        val faturaCompleta = faturaDao.getFaturaById(faturaResumida.id) ?: return@launch
        val faturaNaLixeira = FaturaLixeira(
            faturaOriginalId = faturaCompleta.id,
            numeroFatura = faturaCompleta.numeroFatura,
            cliente = faturaCompleta.cliente,
            artigos = faturaCompleta.artigos, // Passa a lista diretamente
            subtotal = faturaCompleta.subtotal,
            desconto = faturaCompleta.desconto,
            descontoPercent = faturaCompleta.descontoPercent,
            taxaEntrega = faturaCompleta.taxaEntrega,
            saldoDevedor = faturaCompleta.saldoDevedor,
            data = faturaCompleta.data,
            notas = faturaCompleta.notas, // Passa a lista diretamente
            fotosImpressora = faturaCompleta.fotosImpressora,
            dataDelecao = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        lixeiraDao.insert(faturaNaLixeira)
        faturaDao.deleteFaturaById(faturaCompleta.id)
    }

    // Função para buscar a fatura pelo ID (código de barras)
    fun buscarFaturaPorId(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val fatura = faturaDao.getFaturaById(id)
        _faturaEncontrada.postValue(fatura)
    }

    // Função para ser chamada quando a busca for concluída
    fun onBuscaConcluida() {
        _faturaEncontrada.value = null
    }

    private fun converterParaFaturaResumida(fatura: Fatura): FaturaResumidaItem {
        return FaturaResumidaItem(
            id = fatura.id,
            numeroFatura = fatura.numeroFatura ?: "N/A",
            cliente = fatura.cliente ?: "N/A",
            serialNumbers = extrairSeriais(fatura.artigos),
            saldoDevedor = fatura.saldoDevedor ?: 0.0,
            data = formatarData(fatura.data),
            foiEnviada = fatura.foiEnviada == 1
        )
    }

    // Adaptação para extrair seriais de List<ArtigoItem>
    private fun extrairSeriais(artigosList: List<ArtigoItem>?): List<String?> {
        return artigosList?.mapNotNull { it.numeroSerial?.takeIf { s -> s.isNotBlank() && s.lowercase(Locale.ROOT) != "null" } } ?: emptyList()
    }

    private fun formatarData(dataDb: String?): String {
        if (dataDb.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yy", Locale("pt", "BR"))
            inputFormat.parse(dataDb)?.let { outputFormat.format(it) } ?: dataDb
        } catch (e: Exception) { dataDb }
    }
}