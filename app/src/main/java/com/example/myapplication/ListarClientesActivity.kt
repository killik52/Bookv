package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityListarClientesBinding // Verifique este import

class ListarClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private val viewModel: ListarClientesViewModel by viewModels()
    private lateinit var clienteAdapter: ClienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarListarClientes)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        clienteAdapter = ClienteAdapter(
            onItemClick = { cliente ->
                val resultIntent = Intent().apply {
                    putExtra("cliente_id", cliente.id)
                    putExtra("nome_cliente", cliente.nome)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
            onEditClick = { cliente ->
                val intent = Intent(this, ClienteActivity::class.java).apply {
                    putExtra("id", cliente.id)
                }
                startActivity(intent)
            }
        )

        binding.recyclerViewClientes.apply {
            layoutManager = LinearLayoutManager(this@ListarClientesActivity)
            adapter = clienteAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.clientes.observe(this) { clientes ->
            clientes?.let {
                clienteAdapter.submitList(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onOnQueryTextChange(newText: String?): Boolean {
                viewModel.buscarClientes(newText.orEmpty()) // Corrigido para buscarClientes
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}