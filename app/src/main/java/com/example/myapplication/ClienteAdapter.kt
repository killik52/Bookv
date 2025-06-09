package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.databinding.ItemClienteSimplesBinding

class ClienteAdapter(
    private val onItemClick: (Cliente) -> Unit
) : ListAdapter<Cliente, ClienteAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        // Usa o ViewBinding gerado a partir do XML do passo 2
        val binding = ItemClienteSimplesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ClienteViewHolder(
        private val binding: ItemClienteSimplesBinding,
        private val onItemClick: (Cliente) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cliente: Cliente) {
            binding.textViewNomeCliente.text = cliente.nome
            binding.root.setOnClickListener { onItemClick(cliente) }
        }
    }
}

class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
    override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
        return oldItem == newItem
    }
}