package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Artigo

class ListarArtigosActivity : AppCompatActivity() {

    private lateinit var listViewArtigos: ListView
    // Usa o ViewModel para acessar os dados do Room
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
                // Retorna os dados do artigo para a tela anterior
                val resultIntent = Intent().apply {
                    putExtra("artigo_id", artigo.id)
                    putExtra("nome_artigo", artigo.nome)
                    putExtra("quantidade", artigo.quantidade ?: 1)
                    val precoUnitario = artigo.preco ?: 0.0
                    putExtra("valor", precoUnitario * (artigo.quantidade ?: 1))
                    putExtra("numero_serial", artigo.numeroSerial)
                    putExtra("descricao", artigo.descricao)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun observeArtigos() {
        // Observa as mudanças na lista de artigos do ViewModel
        viewModel.todosArtigos.observe(this) { artigos ->
            artigos?.let {
                this.artigosList = it
                val nomes = it.mapNotNull { artigo -> artigo.nome }
                listAdapter.clear()
                listAdapter.addAll(nomes)
                listAdapter.notifyDataSetChanged()
            }
        }
    }
}