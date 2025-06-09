package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LixeiraViewModel(application: Application) : AndroidViewModel(application) {

    private val lixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()
    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()

    val faturasNaLixeira = lixeiraDao.getFaturasNaLixeira().asLiveData()

    fun restaurarFatura(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        // Recria a fatura original
        val faturaRestaurada = Fatura(
            id = faturaLixeira.faturaOriginalId,
            numeroFatura = faturaLixeira.numeroFatura,
            cliente = faturaLixeira.cliente,
            artigos = faturaLixeira.artigos,
            subtotal = faturaLixeira.subtotal,
            desconto = faturaLixeira.desconto,
            descontoPercent = 0, // Ajustar se necessário
            taxaEntrega = faturaLixeira.taxaEntrega,
            saldoDevedor = faturaLixeira.saldoDevedor,
            data = faturaLixeira.data,
            notas = faturaLixeira.notas,
            foiEnviada = 0, // Faturas restauradas voltam como não enviadas
            fotosImpressora = faturaLixeira.fotosImpressora
        )
        faturaDao.insert(faturaRestaurada)

        // Remove da lixeira
        lixeiraDao.deleteFaturaLixeiraById(faturaLixeira.id)
    }

    fun excluirPermanentemente(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        lixeiraDao.deleteFaturaLixeiraById(faturaLixeira.id)
    }
}