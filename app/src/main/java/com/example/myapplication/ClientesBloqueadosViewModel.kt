package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData // Importar asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientesBloqueadosViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteBloqueadoDao = AppDatabase.getDatabase(application).clienteBloqueadoDao()
    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()

    val clientesBloqueados: LiveData<List<Cliente>> = clienteDao.getBloqueados().asLiveData() // Agora observa Clientes com bloqueado=true

    // Assumindo que `salvarAlteracoes` e `excluirPermanentemente` são para `ClienteBloqueado`
    fun salvarAlteracoes(clienteBloqueado: ClienteBloqueado) = viewModelScope.launch(Dispatchers.IO) {
        clienteBloqueadoDao.update(clienteBloqueado) // Certifique-se que ClienteBloqueadoDao tem update
    }

    fun excluirPermanentemente(clienteBloqueado: ClienteBloqueado) = viewModelScope.launch(Dispatchers.IO) {
        clienteBloqueadoDao.deleteById(clienteBloqueado.id)
    }

    // Método para desbloquear um cliente
    fun desbloquearCliente(cliente: Cliente) = viewModelScope.launch(Dispatchers.IO) { // Parâmetro tipo Cliente
        val clienteDesbloqueado = cliente.copy(bloqueado = false)
        clienteDao.update(clienteDesbloqueado)
    }

    // Método para deletar um cliente (não bloqueado)
    fun deleteCliente(cliente: Cliente) = viewModelScope.launch(Dispatchers.IO) {
        clienteDao.deleteById(cliente.id)
    }
}