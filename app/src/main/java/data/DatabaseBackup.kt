package com.example.myapplication.data

import android.app.Application
import com.example.myapplication.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class DatabaseBackup(private val application: Application) {

    private val db = AppDatabase.getDatabase(application)
    private val gson = Gson()

    suspend fun exportDatabaseToJson(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val faturas = db.faturaDao().getAllFaturas().first()
        val clientes = db.clienteDao().getAll().first()
        val artigos = db.artigoDao().getAll().first()
        val faturaItems = faturas.flatMap { fatura -> db.faturaDao().getFaturaWithDetails(fatura.id)?.artigos ?: emptyList() }
        val faturaNotas = faturas.flatMap { fatura -> db.faturaDao().getFaturaWithDetails(fatura.id)?.notas ?: emptyList() }
        val faturaFotos = faturas.flatMap { fatura -> db.faturaDao().getFaturaWithDetails(fatura.id)?.fotos ?: emptyList() }

        val data = mapOf(
            "faturas" to faturas,
            "clientes" to clientes,
            "artigos" to artigos,
            "fatura_itens" to faturaItems,
            "fatura_notas" to faturaNotas,
            "fatura_fotos" to faturaFotos
        )
        outputStream.use { it.write(toJson(data).toByteArray(StandardCharsets.UTF_8)) }
    }

    suspend fun importDatabaseFromJson(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val json = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val data = gson.fromJson(json, Map::class.java)

        val faturas = (data["faturas"] as? List<Map<String, Any>>)?.map {
            Fatura(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                numeroFatura = it["numero_fatura"] as? String,
                cliente = it["cliente"] as? String,
                clienteId = (it["cliente_id"] as? Double)?.toLong(),
                subtotal = it["subtotal"] as? Double,
                desconto = it["desconto"] as? Double,
                descontoPercent = (it["desconto_percent"] as? Double)?.toInt(),
                taxaEntrega = it["taxa_entrega"] as? Double,
                saldoDevedor = it["saldo_devedor"] as? Double,
                data = it["data"] as? String,
                foiEnviada = (it["foi_enviada"] as? Double)?.toInt() ?: 0
            )
        } ?: emptyList()

        val clientes = (data["clientes"] as? List<Map<String, Any>>)?.map {
            Cliente(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                nome = it["nome"] as? String,
                email = it["email"] as? String,
                telefone = it["telefone"] as? String,
                informacoesAdicionais = it["informacoes_adicionais"] as? String,
                cpf = it["cpf"] as? String,
                cnpj = it["cnpj"] as? String,
                logradouro = it["logradouro"] as? String,
                numero = it["numero"] as? String,
                complemento = it["complemento"] as? String,
                bairro = it["bairro"] as? String,
                municipio = it["municipio"] as? String,
                uf = it["uf"] as? String,
                cep = it["cep"] as? String,
                numeroSerial = it["numero_serial"] as? String
            )
        } ?: emptyList()

        val artigos = (data["artigos"] as? List<Map<String, Any>>)?.map {
            Artigo(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                nome = it["nome"] as? String,
                preco = it["preco"] as? Double,
                quantidade = (it["quantidade"] as? Double)?.toInt(),
                desconto = it["desconto"] as? Double,
                descricao = it["descricao"] as? String,
                guardarFatura = (it["guardar_fatura"] as? Double)?.toInt(),
                numeroSerial = it["numero_serial"] as? String
            )
        } ?: emptyList()

        val faturaItems = (data["fatura_itens"] as? List<Map<String, Any>>)?.map {
            FaturaItem(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                faturaId = (it["fatura_id"] as? Double)?.toLong(),
                artigoId = (it["artigo_id"] as? Double)?.toLong(),
                quantidade = (it["quantidade"] as? Double)?.toInt(),
                preco = it["preco"] as? Double,
                clienteId = (it["cliente_id"] as? Double)?.toLong()
            )
        } ?: emptyList()

        val faturaNotas = (data["fatura_notas"] as? List<Map<String, Any>>)?.map {
            FaturaNota(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                faturaId = (it["fatura_id"] as? Double)?.toLong() ?: 0,
                noteContent = it["note_content"] as? String ?: ""
            )
        } ?: emptyList()

        val faturaFotos = (data["fatura_fotos"] as? List<Map<String, Any>>)?.map {
            FaturaFoto(
                id = (it["id"] as? Double)?.toLong() ?: 0,
                faturaId = (it["fatura_id"] as? Double)?.toLong() ?: 0,
                photoPath = it["photo_path"] as? String
            )
        } ?: emptyList()

        // Inserir dados no banco
        clientes.forEach { db.clienteDao().insert(it) }
        artigos.forEach { db.artigoDao().insert(it) }
        faturas.forEach { db.faturaDao().insertFatura(it) }
        faturaItems.forEach { db.faturaDao().insertFaturaItem(it) }
        faturaNotas.forEach { db.faturaNotaDao().insert(it) }
        faturaFotos.forEach { db.faturaDao().insertFaturaFoto(it) }
    }

    private fun toJson(data: Any): String = gson.toJson(data)
}