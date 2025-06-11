package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.*
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

    private val _faturaEncontrada = MutableLiveData<Fatura?>()
    val faturaEncontrada: LiveData<Fatura?> = _faturaEncontrada

    fun moverFaturaParaLixeira(faturaResumida: FaturaResumidaItem) = viewModelScope.launch(Dispatchers.IO) {
        val faturaCompleta = faturaDao.getFaturaWithDetails(faturaResumida.id) ?: return@launch
        val faturaNaLixeira = FaturaLixeira(
            faturaOriginalId = faturaCompleta.fatura.id,
            numeroFatura = faturaCompleta.fatura.numeroFatura,
            cliente = faturaCompleta.fatura.cliente,
            subtotal = faturaCompleta.fatura.subtotal,
            desconto = faturaCompleta.fatura.desconto,
            descontoPercent = faturaCompleta.fatura.descontoPercent,
            taxaEntrega = faturaCompleta.fatura.taxaEntrega,
            saldoDevedor = faturaCompleta.fatura.saldoDevedor,
            data = faturaCompleta.fatura.data,
            dataDelecao = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        lixeiraDao.insert(faturaNaLixeira)
        faturaDao.deleteFaturaById(faturaCompleta.fatura.id)
    }

    fun buscarFaturaPorId(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val fatura = faturaDao.getFaturaById(id)
        _faturaEncontrada.postValue(fatura)
    }

    fun onBuscaConcluida() {
        _faturaEncontrada.value = null
    }

    private fun converterParaFaturaResumida(fatura: Fatura): FaturaResumidaItem {
        return FaturaResumidaItem(
            id = fatura.id,
            numeroFatura = fatura.numeroFatura ?: "N/A",
            cliente = fatura.cliente ?: "N/A",
            serialNumbers = emptyList(), // Serial agora vem de FaturaItem
            saldoDevedor = fatura.saldoDevedor ?: 0.0,
            data = formatarData(fatura.data),
            foiEnviada = fatura.foiEnviada == 1
        )
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