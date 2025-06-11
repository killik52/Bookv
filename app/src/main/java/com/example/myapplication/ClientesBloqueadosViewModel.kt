package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Cliente
import kotlinx.coroutines.launch

class ClientesBloqueadosViewModel(application: Application) : AndroidViewModel(application) {

    private val _clientesBloqueados = MutableLiveData<List<Cliente>>()
    val clientesBloqueados: LiveData<List<Cliente>> = _clientesBloqueados

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    init {
        loadClientesBloqueados()
    }

    private fun loadClientesBloqueados() {
        viewModelScope.launch {
            db.clienteDao().getBloqueados().observeForever {
                _clientesBloqueados.value = it
            }
        }
    }

    fun desbloquearCliente(cliente: Cliente) {
        viewModelScope.launch {
            val updatedCliente = cliente.copy(bloqueado = false)
            db.clienteDao().update(updatedCliente)
        }
    }
}
