package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Artigo

class ArquivosRecentesActivity : AppCompatActivity() {

    private lateinit var listViewArquivosRecentes: ListView
    private lateinit var editTextPesquisa: EditText
    private lateinit var textViewNovoArquivo: TextView

    // Usando o ViewModel para acessar os dados
    private val viewModel: ArquivosRecentesViewModel by viewModels()

    // Lista para guardar os artigos carregados do ViewModel
    private var artigosRecentesList: List<Artigo> = listOf()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arquivos_recentes)

        // Inicializando as Views
        listViewArquivosRecentes = findViewById(R.id.listViewArquivosRecentes)
        editTextPesquisa = findViewById(R.id.editTextPesquisa)
        textViewNovoArquivo = findViewById(R.id.textViewNovoArquivo)

        // Configurando o adapter da lista
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listViewArquivosRecentes.adapter = adapter

        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        // Listener para clique em um item da lista
        listViewArquivosRecentes.setOnItemClickListener { parent, view, position, id ->
            val nomeSelecionado = adapter.getItem(position)
            val artigoSelecionado = artigosRecentesList.find { it.nome == nomeSelecionado }

            artigoSelecionado?.let { artigo ->
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

        // Listener para criar um novo artigo
        textViewNovoArquivo.setOnClickListener {
            val intent = Intent(this, CriarNovoArtigoActivity::class.java)
            startActivityForResult(intent, 792)
        }

        // Listener para a barra de pesquisa
        editTextPesquisa.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                filtrarArtigos(editTextPesquisa.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun observeViewModel() {
        viewModel.artigosRecentes.observe(this) { artigos ->
            artigosRecentesList = artigos ?: emptyList()
            filtrarArtigos(editTextPesquisa.text.toString())
        }
    }

    private fun filtrarArtigos(query: String) {
        val filteredList = if (query.isBlank()) {
            artigosRecentesList
        } else {
            artigosRecentesList.filter { artigo -> // Explicitamente nomear o parâmetro 'artigo'
                val nomeMatches = artigo.nome?.contains(query, ignoreCase = true) ?: false
                val descricaoMatches = artigo.descricao?.contains(query, ignoreCase = true) ?: false
                val serialMatches = artigo.numeroSerial?.contains(query, ignoreCase = true) ?: false

                nomeMatches || descricaoMatches || serialMatches
            }
        }

        val nomesParaExibir = filteredList.mapNotNull { it.nome }
        adapter.clear()
        adapter.addAll(nomesParaExibir)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 792 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}