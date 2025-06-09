// app/src/main/java/com/example/myapplication/SecondScreenViewModel.kt
package com.example.myapplication

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.FaturaDao
import com.example.myapplication.data.dao.FaturaLixeiraDao
import com.example.myapplication.data.dao.FaturaNotaDao
import com.example.myapplication.data.model.ArtigoItem
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaFoto
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaLixeira
import com.example.myapplication.data.model.FaturaNota
import com.example.myapplication.data.model.FaturaWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SecondScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao: FaturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val faturaLixeiraDao: FaturaLixeiraDao = AppDatabase.getDatabase(application).lixeiraDao()
    private val faturaNotaDao: FaturaNotaDao = AppDatabase.getDatabase(application).faturaNotaDao()

    private val _fatura = MutableStateFlow<Fatura?>(null)
    val fatura: StateFlow<Fatura?> = _fatura.asStateFlow()

    private val _artigos = MutableStateFlow<List<ArtigoItem>>(emptyList())
    val artigos: StateFlow<List<ArtigoItem>> = _artigos.asStateFlow()

    private val _fotos = MutableStateFlow<List<Uri>>(emptyList())
    val fotos: StateFlow<List<Uri>> = _fotos.asStateFlow()

    private val _notas = MutableStateFlow<List<FaturaNota>>(emptyList())
    val notas: StateFlow<List<FaturaNota>> = _notas.asStateFlow()

    private val _totalFaturasDoCliente = MutableStateFlow(0)
    val totalFaturasDoCliente: StateFlow<Int> = _totalFaturasDoCliente.asStateFlow()

    init {
        // Observa as faturas do cliente para atualizar o total
        _fatura.combine(faturaDao.getAllFaturas()) { currentFatura, allFaturas ->
            val total = allFaturas.count { it.cliente == currentFatura?.cliente }
            _totalFaturasDoCliente.value = total
        }.launchIn(viewModelScope)
    }

    fun carregarFatura(faturaId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            faturaDao.getFaturaWithDetails(faturaId).collect { faturaWithDetails ->
                faturaWithDetails?.let {
                    _fatura.value = it.fatura
                    _artigos.value = it.artigos.map { item ->
                        ArtigoItem(
                            id = item.id.toInt(),
                            nome = item.nomeArtigo,
                            quantidade = item.quantidade,
                            preco = item.precoUnitario,
                            numeroSerial = item.numeroSerial,
                            descricao = item.descricao // Certifique-se de que a descrição esteja sendo armazenada
                        )
                    }
                    _fotos.value = it.fotos.map { foto -> Uri.parse(foto.uri) }
                    _notas.value = it.notas // Define as notas a partir da relação
                }
            }
        }
    }

    fun setFatura(fatura: Fatura) {
        _fatura.value = fatura
    }

    fun adicionarArtigo(artigo: ArtigoItem) {
        _artigos.value = _artigos.value + artigo
    }

    fun removerArtigo(artigo: ArtigoItem) {
        _artigos.value = _artigos.value.filter { it != artigo }
    }

    fun adicionarFoto(uri: Uri) {
        _fotos.value = _fotos.value + uri
    }

    fun removerFoto(uri: Uri) {
        _fotos.value = _fotos.value.filter { it != uri }
    }

    fun adicionarNota(noteContent: String) {
        val currentNotes = _notas.value.toMutableList()
        currentNotes.add(FaturaNota(faturaId = _fatura.value?.id ?: 0, noteContent = noteContent))
        _notas.value = currentNotes
    }

    fun removerNota(nota: FaturaNota) {
        _notas.value = _notas.value.filter { it != nota }
    }

    fun salvarFaturaCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFatura = _fatura.value ?: return@launch
            val faturaId = faturaDao.insertFatura(currentFatura)
            currentFatura.id = faturaId // Garante que a fatura tenha o ID correto para relações

            // Salva os itens da fatura
            faturaDao.deleteFaturaItemsByFaturaId(faturaId)
            val faturaItems = _artigos.value.map { artigo ->
                FaturaItem(
                    faturaId = faturaId,
                    nomeArtigo = artigo.nome,
                    quantidade = artigo.quantidade,
                    precoUnitario = artigo.preco,
                    numeroSerial = artigo.numeroSerial,
                    descricao = artigo.descricao
                )
            }
            faturaDao.insertAllFaturaItems(faturaItems)

            // Salva as fotos da fatura
            faturaDao.deleteFaturaFotosByFaturaId(faturaId)
            val faturaFotos = _fotos.value.map { uri ->
                FaturaFoto(faturaId = faturaId, uri = uri.toString())
            }
            faturaDao.insertAllFaturaFotos(faturaFotos)

            // Salva as notas da fatura
            faturaNotaDao.upsertAll(faturaId, _notas.value) // Usa o método upsertAll para lidar com remoção/inserção

            _fatura.value = currentFatura // Atualiza o LiveData com o ID gerado
            Log.d("SecondScreenViewModel", "Fatura e detalhes salvos com sucesso. ID: $faturaId")
        }
    }

    fun moverFaturaParaLixeira(fatura: Fatura) {
        viewModelScope.launch(Dispatchers.IO) {
            // Criar FaturaLixeira a partir de Fatura
            val faturaLixeira = FaturaLixeira(
                numeroFatura = fatura.numeroFatura,
                cliente = fatura.cliente,
                subtotal = fatura.subtotal,
                desconto = fatura.desconto,
                descontoPercent = fatura.descontoPercent,
                taxaEntrega = fatura.taxaEntrega,
                saldoDevedor = fatura.saldoDevedor,
                data = fatura.data,
                foiEnviada = fatura.foiEnviada,
                dataDelecao = System.currentTimeMillis().toString() // Ou um formato de data legível
            )
            faturaLixeiraDao.insert(faturaLixeira)
            faturaDao.deleteFatura(fatura.id)
            // Itens e fotos são excluídos via CASCADE ao deletar a fatura principal.
            // Se precisar recuperar detalhes da lixeira, considere uma cópia mais profunda ou um ID da fatura original.
        }
    }


    fun updateFatura(fatura: Fatura) {
        viewModelScope.launch(Dispatchers.IO) {
            faturaDao.updateFatura(fatura)
        }
    }

    // Helper para converter Bitmap para Base64 String
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Helper para converter Base64 String para Bitmap
    private fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("SecondScreenViewModel", "Invalid Base64 string for bitmap: ${e.message}")
            null
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SecondScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SecondScreenViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}