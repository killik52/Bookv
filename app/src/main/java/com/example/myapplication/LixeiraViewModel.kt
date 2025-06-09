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

    // Chamada de método corrigida
    val faturasNaLixeira = lixeiraDao.getAll().asLiveData()

    fun restaurarFatura(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        val faturaRestaurada = Fatura(
            id = faturaLixeira.faturaOriginalId,
            numeroFatura = faturaLixeira.numeroFatura,
            cliente = faturaLixeira.cliente,
            artigos = faturaLixeira.artigos,
            subtotal = faturaLixeira.subtotal,
            desconto = faturaLixeira.desconto,
            descontoPercent = faturaLixeira.descontoPercent,
            taxaEntrega = faturaLixeira.taxaEntrega,
            saldoDevedor = faturaLixeira.saldoDevedor,
            data = faturaLixeira.data,
            notas = faturaLixeira.notas,
            foiEnviada = 0, // Faturas restauradas voltam como não enviadas
            fotosImpressora = faturaLixeira.fotosImpressora
        )
        // Usando insert, que é a operação correta do DAO
        faturaDao.insertFatura(faturaRestaurada)

        // Chamada de método corrigida
        lixeiraDao.deleteById(faturaLixeira.id)
    }

    fun excluirPermanentemente(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        // Chamada de método corrigida
        lixeiraDao.deleteById(faturaLixeira.id)
    }
}