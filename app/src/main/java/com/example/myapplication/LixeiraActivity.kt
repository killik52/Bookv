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
    private lateinit var lixeiraAdapter: FaturaLixeiraAdapter

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
                lixeiraAdapter.updateFaturas(it)
            }
        }
    }
}