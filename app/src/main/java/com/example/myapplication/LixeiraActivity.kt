package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.FaturaLixeira
import com.example.myapplication.databinding.ActivityLixeiraBinding

class LixeiraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLixeiraBinding
    private val viewModel: LixeiraViewModel by viewModels()
    private lateinit var lixeiraAdapter: FaturaLixeiraAdapter // Você precisará criar este Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLixeiraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        lixeiraAdapter = FaturaLixeiraAdapter(
            onRestoreClick = { faturaNaLixeira ->
                viewModel.restaurarFatura(faturaNaLixeira)
                Toast.makeText(this, "Fatura restaurada!", Toast.LENGTH_SHORT).show()
                // Define o resultado para que a MainActivity possa atualizar sua lista
                setResult(Activity.RESULT_OK)
            },
            onLongClick = { faturaNaLixeira ->
                AlertDialog.Builder(this)
                    .setTitle("Excluir Permanentemente")
                    .setMessage("Esta ação não pode ser desfeita. Deseja excluir esta fatura para sempre?")
                    .setPositiveButton("Excluir") { _, _ ->
                        viewModel.excluirPermanentemente(faturaNaLixeira)
                        Toast.makeText(this, "Fatura excluída permanentemente.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.faturasLixeiraRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.faturasLixeiraRecyclerView.adapter = lixeiraAdapter
    }

    private fun observeViewModel() {
        viewModel.faturasNaLixeira.observe(this) { faturas ->
            faturas?.let {
                // A lista de FaturaLixeiraItem precisa ser criada a partir de FaturaLixeira
                val itensParaAdapter = it.map { faturaLixeira ->
                    FaturaLixeiraItem(
                        id = faturaLixeira.id,
                        numeroFatura = faturaLixeira.numeroFatura ?: "N/A",
                        cliente = faturaLixeira.cliente ?: "N/A",
                        data = faturaLixeira.dataDelecao // Mostra a data de deleção
                    )
                }
                lixeiraAdapter.updateFaturas(itensParaAdapter)
            }
        }
    }
}