package com.example.tcc3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.databinding.FragmentStatusItemBinding
import com.example.tcc3.websocket.WebSocketClient

class StatusAdapter(private val totalTables: Int, private val webSocketClient: WebSocketClient): RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(val binding: FragmentStatusItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = FragmentStatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        // TODO("Display if buttons are connected to WebSocket's Client")
        when (position + 1) {
            9, 10, 11, 12 -> holder.binding.txtTable.text = java.lang.String("Cozinha ${position - 7}")
            else -> holder.binding.txtTable.text = java.lang.String("Mesa ${position+1}")
        }
    }

    override fun getItemCount(): Int {
        // Cria um holder para cada mesa do estabelecimento
        return totalTables
    }
}