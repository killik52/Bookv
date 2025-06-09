package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.model.FaturaLixeira
import com.example.myapplication.data.model.FaturaLixeiraItem
import com.example.myapplication.databinding.ItemFaturaLixeiraBinding

class FaturaLixeiraAdapter(
    private val onRestoreClick: (FaturaLixeira) -> Unit,
    private val onLongClick: (FaturaLixeira) -> Unit
) : RecyclerView.Adapter<FaturaLixeiraAdapter.ViewHolder>() {

    private var faturasNaLixeira = emptyList<FaturaLixeira>()

    class ViewHolder(private val binding: ItemFaturaLixeiraBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fatura: FaturaLixeira, onRestoreClick: (FaturaLixeira) -> Unit, onLongClick: (FaturaLixeira) -> Unit) {
            binding.numeroFaturaTextView.text = fatura.numeroFatura ?: "Fatura N/A"
            binding.clienteTextView.text = fatura.cliente ?: "Cliente N/A"
            binding.dataTextView.text = fatura.dataDelecao ?: "Data N/A"
            binding.restoreButton.setOnClickListener { onRestoreClick(fatura) }
            itemView.setOnLongClickListener {
                onLongClick(fatura)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFaturaLixeiraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(faturasNaLixeira[position], onRestoreClick, onLongClick)
    }

    override fun getItemCount(): Int = faturasNaLixeira.size

    fun updateFaturas(faturas: List<FaturaLixeira>) {
        this.faturasNaLixeira = faturas
        notifyDataSetChanged()
    }
}