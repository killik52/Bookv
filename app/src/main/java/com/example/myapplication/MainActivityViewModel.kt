package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaLixeira // Importar FaturaLixeira
import com.example.myapplication.data.model.FaturaWithDetails
import com.example.myapplication.FaturaResumidaItem // Importar FaturaResumidaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first // Importar first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val lixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()

    // Observa todas as faturas e as mapeia para FaturaResumidaItem
    val faturas: LiveData<List<FaturaResumidaItem>> = faturaDao.getAllFaturas()
        .map { faturas ->
            faturas.map { converterParaFaturaResumida(it) }
        }.asLiveData()

    private val _totalFaturasHoje = MutableLiveData<Double>()
    val totalFaturasHoje: LiveData<Double> = _totalFaturasHoje

    private val _faturaEncontrada = MutableLiveData<Fatura?>()
    val faturaEncontrada: LiveData<Fatura?> = _faturaEncontrada

    init {
        loadTotalFaturasHoje() // Mantenha esta inicialização
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

            faturaDao.getFaturasByDateRange(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startOfDay)),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(endOfDay))
            ).collect { faturasWithDetails -> // Usar collect para Flow, e renomear para evitar conflito
                _totalFaturasHoje.postValue(faturasWithDetails.sumOf { it.fatura.valorTotal ?: 0.0 }) // it.fatura.valorTotal para FaturaWithDetails
            }
        }
    }

    fun addFatura(fatura: Fatura, faturaItems: List<FaturaItem>) {
        viewModelScope.launch {
            faturaDao.insertFaturaWithItems(fatura, faturaItems)
        }
    }

    // Assumindo que o DAO retorna LiveData, então é só retornar direto.
    // Se o DAO retorna suspend fun, precisa de liveData { emit(faturaDao.getFaturaWithDetails(faturaId)) }
    fun getFaturaById(faturaId: Long): LiveData<FaturaWithDetails?> { // faturaId deve ser Long
        return liveData {
            emit(faturaDao.getFaturaWithDetails(faturaId))
        }
    }

    // Corrigido para mover FaturaResumidaItem para Lixeira
    fun moverFaturaParaLixeira(faturaResumida: FaturaResumidaItem) = viewModelScope.launch(Dispatchers.IO) {
        // Obter a fatura completa do banco de dados usando o ID
        val faturaCompleta = faturaDao.getFaturaById(faturaResumida.id) // Buscar Fatura original pelo ID
        if (faturaCompleta != null) {
            val faturaNaLixeira = FaturaLixeira(
                faturaOriginalId = faturaCompleta.id,
                numeroFatura = faturaCompleta.numeroFatura,
                cliente = faturaCompleta.cliente,
                subtotal = faturaCompleta.subtotal,
                desconto = faturaCompleta.desconto,
                descontoPercent = faturaCompleta.descontoPercent,
                taxaEntrega = faturaCompleta.taxaEntrega,
                saldoDevedor = faturaCompleta.saldoDevedor,
                data = faturaCompleta.data,
                foiEnviada = 0, // Faturas restauradas voltam como não enviadas
                tipo = faturaCompleta.tipo, // Adicione este campo em FaturaLixeira se existir no Fatura
                caminhoArquivo = faturaCompleta.caminhoArquivo // Adicione este campo em FaturaLixeira se existir no Fatura
            )
            lixeiraDao.insert(faturaNaLixeira)
            faturaDao.deleteFaturaById(faturaCompleta.id) // Excluir a fatura original
        } else {
            Log.e("MainActivityViewModel", "Fatura com ID ${faturaResumida.id} não encontrada para mover para lixeira.")
        }
    }

    fun buscarFaturaPorId(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val fatura = faturaDao.getFaturaById(id)
        _faturaEncontrada.postValue(fatura)
    }

    fun onBuscaConcluida() {
        _faturaEncontrada.value = null
    }

    // Corrigido para garantir que o tipo do parâmetro é inferível
    fun searchFaturas(query: String): LiveData<List<FaturaResumidaItem>> {
        val searchResult = MutableLiveData<List<FaturaResumidaItem>>()
        viewModelScope.launch {
            val allFaturasWithDetails = faturaDao.getAllFaturasWithDetailsList() // Suspend fun
            val filteredList = allFaturasWithDetails.filter { faturaWithDetails ->
                val fatura = faturaWithDetails.fatura
                val cliente = faturaWithDetails.cliente
                val nomeClienteMatches = cliente?.nome?.contains(query, ignoreCase = true) ?: false
                val idFaturaMatches = fatura.id.toString().contains(query, ignoreCase = true)
                nomeClienteMatches || idFaturaMatches
            }.map { faturaWithDetails ->
                converterParaFaturaResumida(faturaWithDetails.fatura) // Converter para FaturaResumidaItem
            }
            searchResult.postValue(filteredList)
        }
        return searchResult
    }

    private fun converterParaFaturaResumida(fatura: Fatura): FaturaResumidaItem {
        return FaturaResumidaItem(
            id = fatura.id,
            numeroFatura = fatura.numeroFatura ?: "N/A",
            cliente = fatura.cliente ?: "N/A",
            serialNumbers = emptyList(), // Preencha se tiver lógica para obter seriais de FaturaItem
            saldoDevedor = fatura.saldoDevedor ?: 0.0,
            data = fatura.data ?: "", // Ajustado para ser String não nula
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