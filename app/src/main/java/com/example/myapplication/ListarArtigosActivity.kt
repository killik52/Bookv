package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast // Importe Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Artigo

class ListarArtigosActivity : AppCompatActivity() {

    private lateinit var listViewArtigos: ListView
    private val viewModel: ArtigoViewModel by viewModels()

    private var artigosList: List<Artigo> = listOf()
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_artigos)

        listViewArtigos = findViewById(R.id.listViewArtigos)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listViewArtigos.adapter = listAdapter

        observeArtigos()
        setupListeners()
    }

    private fun setupListeners() {
        listViewArtigos.setOnItemClickListener { _, _, position, _ ->
            val artigoSelecionado = artigosList.getOrNull(position)
            artigoSelecionado?.let { artigo ->
                val resultIntent = Intent().apply {
                    putExtra("artigo_id", artigo.id)
                    putExtra("nome_artigo", artigo.nome)
                    val quantidade = artigo.quantidade ?: 1
                    putExtra("quantidade", quantidade)
                    val precoUnitario = artigo.preco ?: 0.0
                    putExtra("valor", precoUnitario * quantidade)
                    putExtra("numero_serial", artigo.numeroSerial)
                    putExtra("descricao", artigo.descricao)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } ?: Toast.makeText(this, "Artigo não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeArtigos() {
        viewModel.todosArtigos.observe(this) { artigos ->
            artigos?.let {
                this.artigosList = it
                val nomes = it.mapNotNull { artigo -> artigo.nome }
                listAdapter.clear()
                listAdapter.addAll(nomes)
                listAdapter.notifyDataSetChanged()
            } ?: Toast.makeText(this, "Nenhum artigo encontrado.", Toast.LENGTH_SHORT).show()
        }
    }
}