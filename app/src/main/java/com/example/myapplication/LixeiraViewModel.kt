package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData // Importar asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.data.dao.FaturaLixeiraDao // Importar FaturaLixeiraDao
import com.example.myapplication.data.dao.FaturaDao // Importar FaturaDao

class LixeiraViewModel(application: Application) : AndroidViewModel(application) {

    private val lixeiraDao: FaturaLixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()
    private val faturaDao: FaturaDao = AppDatabase.getDatabase(application).faturaDao()

    val faturasLixeira: LiveData<List<FaturaLixeira>> = lixeiraDao.getAll().asLiveData()

    // Este método é usado para restaurar uma fatura da lixeira para a tabela de faturas
    fun restoreFaturaLixeira(faturaLixeira: FaturaLixeira) {
        viewModelScope.launch {
            val faturaRestaurada = Fatura(
                id = faturaLixeira.faturaOriginalId, // Usa o ID original
                numeroFatura = faturaLixeira.numeroFatura,
                cliente = faturaLixeira.cliente,
                // clienteId será nulo se não houver um ID correspondente no modelo de Cliente.
                // Idealmente, você deve ter o clienteId original salvo na FaturaLixeira.
                // Por enquanto, deixamos como null.
                clienteId = null,
                subtotal = faturaLixeira.subtotal,
                desconto = faturaLixeira.desconto,
                descontoPercent = faturaLixeira.descontoPercent,
                taxaEntrega = faturaLixeira.taxaEntrega,
                saldoDevedor = faturaLixeira.saldoDevedor,
                data = faturaLixeira.data,
                foiEnviada = 0, // Faturas restauradas voltam como não enviadas
                // Preencher campos adicionais da Fatura se necessário
                // Ex: tipo = faturaLixeira.tipo, caminhoArquivo = faturaLixeira.caminhoArquivo
                tipo = faturaLixeira.tipo, // Adicione este campo em FaturaLixeira se existir no Fatura
                caminhoArquivo = null // Adicione este campo em FaturaLixeira se existir no Fatura
            )
            faturaDao.insertFatura(faturaRestaurada) // Insere a fatura de volta na tabela principal
            lixeiraDao.deleteById(faturaLixeira.id) // Exclui da lixeira
        }
    }

    // Este método é para exclusão permanente
    fun deleteFaturaLixeira(faturaLixeira: FaturaLixeira) {
        viewModelScope.launch {
            lixeiraDao.deleteById(faturaLixeira.id)
        }
    }

    // Método para filtrar faturas da lixeira por mês e ano
    fun filterFaturasLixeiraByMonth(month: Int, year: Int) {
        viewModelScope.launch {
            // Assumindo que você tenha um método no FaturaLixeiraDao para isso
            // Exemplo: db.lixeiraDao().getFaturasLixeiraByMonth(month, year).asLiveData()
            // Se não tiver, você precisará adicionar no FaturaLixeiraDao
            val filtered = lixeiraDao.getAll().asLiveData().value?.filter { fatura ->
                val cal = Calendar.getInstance().apply {
                    fatura.dataDelecao?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.time?.let { time -> timeInMillis = time } }
                }
                cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
            } ?: emptyList()
            _faturasLixeira.postValue(filtered)
        }
    }
}