package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// CORRIGIDO: O caminho completo para a classe ClienteBloqueado
import com.example.myapplication.data.model.ClienteBloqueado

class ClienteBloqueadoAdapter(
    // Agora o tipo ClienteBloqueado será encontrado corretamente
    private val clientes: List<ClienteBloqueado>
) : RecyclerView.Adapter<ClienteBloqueadoAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.nomeTextView.text = cliente.nome
    }

    override fun getItemCount(): Int = clientes.size
}