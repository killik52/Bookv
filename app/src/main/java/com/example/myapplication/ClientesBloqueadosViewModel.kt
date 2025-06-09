package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientesBloqueadosViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteBloqueadoDao = AppDatabase.getDatabase(application).clienteBloqueadoDao()
    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()

    val todosClientesBloqueados: LiveData<List<ClienteBloqueado>> = clienteBloqueadoDao.getAll().asLiveData()

    fun salvarAlteracoes(clienteBloqueado: ClienteBloqueado) = viewModelScope.launch(Dispatchers.IO) {
        clienteBloqueadoDao.update(clienteBloqueado)
    }

    fun excluirPermanentemente(clienteBloqueado: ClienteBloqueado) = viewModelScope.launch(Dispatchers.IO) {
        clienteBloqueadoDao.deleteById(clienteBloqueado.id)
    }

    fun desbloquearCliente(clienteBloqueado: ClienteBloqueado, onFinished: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        // Mapeia o ClienteBloqueado de volta para um Cliente normal
        val clienteDesbloqueado = Cliente(
            nome = clienteBloqueado.nome,
            email = clienteBloqueado.email,
            telefone = clienteBloqueado.telefone,
            informacoesAdicionais = clienteBloqueado.informacoesAdicionais,
            cpf = clienteBloqueado.cpf,
            cnpj = clienteBloqueado.cnpj,
            logradouro = clienteBloqueado.logradouro,
            numero = clienteBloqueado.numero,
            complemento = clienteBloqueado.complemento,
            bairro = clienteBloqueado.bairro,
            municipio = clienteBloqueado.municipio,
            uf = clienteBloqueado.uf,
            cep = clienteBloqueado.cep,
            numeroSerial = clienteBloqueado.numeroSerial
        )
        // Insere na tabela de clientes ativos
        clienteDao.insert(clienteDesbloqueado)
        // Remove da tabela de clientes bloqueados
        clienteBloqueadoDao.deleteById(clienteBloqueado.id)

        launch(Dispatchers.Main) {
            onFinished()
        }
    }
}