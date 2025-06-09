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

    fun moverFaturaParaLixeira(faturaResumida: FaturaResumidaItem) = viewModelScope.launch(Dispatchers.IO) {
        val faturaCompleta = faturaDao.getFaturaById(faturaResumida.id) ?: return@launch

        val faturaNaLixeira = FaturaLixeira(
            faturaOriginalId = faturaCompleta.id,
            numeroFatura = faturaCompleta.numeroFatura,
            cliente = faturaCompleta.cliente,
            artigos = faturaCompleta.artigos,
            subtotal = faturaCompleta.subtotal,
            desconto = faturaCompleta.desconto,
            taxaEntrega = faturaCompleta.taxaEntrega,
            saldoDevedor = faturaCompleta.saldoDevedor,
            data = faturaCompleta.data,
            notas = faturaCompleta.notas,
            fotosImpressora = faturaCompleta.fotosImpressora,
            dataDelecao = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        lixeiraDao.moveToLixeira(faturaNaLixeira)
        faturaDao.deleteFaturaById(faturaCompleta.id)
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

    private fun extrairSeriais(artigosString: String?): List<String?> {
        if (artigosString.isNullOrEmpty()) return emptyList()
        return artigosString.split("|").mapNotNull { artigoData ->
            val parts = artigoData.split(",")
            if (parts.size >= 5) parts[4].takeIf { it.isNotBlank() && it.lowercase(Locale.ROOT) != "null" }
            else null
        }
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