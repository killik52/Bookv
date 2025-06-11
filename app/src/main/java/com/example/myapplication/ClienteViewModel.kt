package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val clienteBloqueadoDao = AppDatabase.getDatabase(application).clienteBloqueadoDao()
    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()

    private val _cliente = MutableLiveData<Cliente?>()
    val cliente: LiveData<Cliente?> = _cliente

    private val _seriaisAssociados = MutableLiveData<String>()
    val seriaisAssociados: LiveData<String> = _seriaisAssociados

    fun carregarCliente(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteCarregado = clienteDao.getById(id)
            withContext(Dispatchers.Main) {
                _cliente.value = clienteCarregado
            }
            // Carrega os seriais em paralelo
            carregarSeriais(clienteCarregado)
        }
    }

    private suspend fun carregarSeriais(cliente: Cliente?) {
        if (cliente == null || cliente.nome == null) {
            _seriaisAssociados.postValue("")
            return
        }
        val todosSeriais = mutableSetOf<String>()

        // Adiciona seriais do cadastro do cliente (importado de CSV, etc.)
        cliente.numeroSerial?.split(',')?.forEach {
            if (it.trim().isNotEmpty()) todosSeriais.add(it.trim())
        }

        // Busca a lista de faturas para o cliente
        val faturas = faturaDao.getFaturasPorClienteNome(cliente.nome!!)
        faturas.forEach { fatura ->
            // Obtém os itens da fatura usando getFaturaWithDetails
            val faturaDetails = faturaDao.getFaturaWithDetails(fatura.id)
            faturaDetails?.artigos?.forEach { item ->
                // Assume que o número de série está armazenado em outro lugar, por exemplo, em Artigo
                val artigo = item.artigoId?.let { clienteDao.getById(it) }
                artigo?.numeroSerial?.split(',')?.forEach { serial ->
                    if (serial.trim().isNotEmpty() && serial.lowercase() != "null") {
                        todosSeriais.add(serial.trim())
                    }
                }
            }
        }
        _seriaisAssociados.postValue(todosSeriais.joinToString(", "))
    }

    fun salvarAlteracoesCliente(clienteAtualizado: Cliente) = viewModelScope.launch(Dispatchers.IO) {
        clienteDao.update(clienteAtualizado)
    }

    fun excluirCliente(cliente: Cliente, onFinished: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        clienteDao.deleteById(cliente.id)
        withContext(Dispatchers.Main) { onFinished() }
    }

    fun bloquearCliente(cliente: Cliente, onFinished: (Long) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        // Mapeia o Cliente para um ClienteBloqueado
        val bloqueado = ClienteBloqueado(
            nome = cliente.nome ?: "Nome Desconhecido",
            email = cliente.email,
            telefone = cliente.telefone,
            informacoesAdicionais = cliente.informacoesAdicionais,
            cpf = cliente.cpf,
            cnpj = cliente.cnpj,
            logradouro = cliente.logradouro,
            numero = cliente.numero,
            complemento = cliente.complemento,
            bairro = cliente.bairro,
            municipio = cliente.municipio,
            uf = cliente.uf,
            cep = cliente.cep,
            numeroSerial = cliente.numeroSerial
        )
        clienteBloqueadoDao.insert(bloqueado)

        val clienteBloqueadoSalvo = clienteBloqueadoDao.getByNome(bloqueado.nome)

        clienteDao.deleteById(cliente.id)

        withContext(Dispatchers.Main) {
            onFinished(clienteBloqueadoSalvo?.id ?: -1L)
        }
    }
}