package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LixeiraViewModel(application: Application) : AndroidViewModel(application) {

    private val lixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()
    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()

    val faturasNaLixeira = lixeiraDao.getAll().asLiveData()

    fun restaurarFatura(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        val faturaRestaurada = Fatura(
            id = faturaLixeira.faturaOriginalId,
            numeroFatura = faturaLixeira.numeroFatura,
            cliente = faturaLixeira.cliente,
            clienteId = null, // Cliente será resolvido na atividade
            subtotal = faturaLixeira.subtotal,
            desconto = faturaLixeira.desconto,
            descontoPercent = faturaLixeira.descontoPercent,
            taxaEntrega = faturaLixeira.taxaEntrega,
            saldoDevedor = faturaLixeira.saldoDevedor,
            data = faturaLixeira.data,
            foiEnviada = 0 // Faturas restauradas voltam como não enviadas
        )
        faturaDao.insertFatura(faturaRestaurada)
        lixeiraDao.deleteById(faturaLixeira.id)
    }

    fun excluirPermanentemente(faturaLixeira: FaturaLixeira) = viewModelScope.launch(Dispatchers.IO) {
        lixeiraDao.deleteById(faturaLixeira.id)
    }
}