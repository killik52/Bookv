package data

import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.* // Importar todas as classes de modelo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class DatabaseBackup(private val db: AppDatabase) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // Backup de todas as tabelas para um único arquivo JSON
    suspend fun backup(outputFile: File) {
        val allData = mutableMapOf<String, Any>()

        // Adaptações para Listas para GSON
        // Chamar os métodos DAO para obter as listas diretamente
        val clientes = db.clienteDao().getAllClientesList()
        val artigos = db.artigoDao().getAllArtigosList()
        val faturas = db.faturaDao().getAllFaturasWithDetailsList()
        val faturaItems = db.faturaDao().getAllFaturaItemsList()
        val faturaNotas = db.faturaNotaDao().getAllNotesList()
        val faturaLixeira = db.lixeiraDao().getAllFaturasLixeiraList()

        allData["clientes"] = clientes
        allData["artigos"] = artigos
        allData["faturas"] = faturas
        allData["faturaItems"] = faturaItems
        allData["faturaNotas"] = faturaNotas
        allData["faturaLixeira"] = faturaLixeira

        FileWriter(outputFile).use { writer ->
            gson.toJson(allData, writer)
        }
    }

    // Restaura o banco de dados a partir de um arquivo JSON
    suspend fun restore(inputFile: File) {
        FileReader(inputFile).use { reader ->
            val allData = gson.fromJson(reader, MutableMap::class.java) as Map<String, List<Map<String, Any>>>

            db.clearAllTables() // Limpar todas as tabelas antes de restaurar

            // Restaurar clientes
            (allData["clientes"] as? List<Map<String, Any>>)?.forEach { clienteMap ->
                val cliente = gson.fromJson(gson.toJson(clienteMap), Cliente::class.java)
                db.clienteDao().insert(cliente)
            }

            // Restaurar artigos
            (allData["artigos"] as? List<Map<String, Any>>)?.forEach { artigoMap ->
                val artigo = gson.fromJson(gson.toJson(artigoMap), Artigo::class.java)
                db.artigoDao().insert(artigo)
            }

            // Restaurar faturas (e seus itens e notas, se estiverem incluídos no backup principal)
            // IMPORTANTE: A ordem de restauração é crucial devido às chaves estrangeiras.
            // Faturas devem ser restauradas antes de faturaItems e faturaNotas.
            (allData["faturas"] as? List<Map<String, Any>>)?.forEach { faturaMap ->
                val fatura = gson.fromJson(gson.toJson(faturaMap), Fatura::class.java) // Corrigido 'furaMap' para 'faturaMap'
                db.faturaDao().insertFatura(fatura) // Chamar insertFatura
            }

            // Restaurar itens da fatura
            (allData["faturaItems"] as? List<Map<String, Any>>)?.forEach { itemMap ->
                val item = gson.fromJson(gson.toJson(itemMap), FaturaItem::class.java)
                db.faturaDao().insertFaturaItem(item)
            }

            // Restaurar notas da fatura
            (allData["faturaNotas"] as? List<Map<String, Any>>)?.forEach { notaMap ->
                val nota = gson.fromJson(gson.toJson(notaMap), FaturaNota::class.java)
                db.faturaNotaDao().insert(nota)
            }

            // Restaurar lixeira de faturas
            (allData["faturaLixeira"] as? List<Map<String, Any>>)?.forEach { lixeiraMap ->
                val lixeiraItem = gson.fromJson(gson.toJson(lixeiraMap), FaturaLixeira::class.java)
                db.lixeiraDao().insert(lixeiraItem)
            }
        }
    }
}