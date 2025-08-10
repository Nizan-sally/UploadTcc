package com.example.tcc3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.databinding.FragmentWaitersManageItemBinding
import com.example.tcc3.models.WaiterData

// Classe de adaptação que relaciona a RecyclerView, WaiterData e elementos gráficos, possibilitando sua alteração e exibição
class WaiterAdapter(private val list:MutableList<WaiterData>) : RecyclerView.Adapter<WaiterAdapter.WaiterViewHolder>(){

    // Variável que gerencia eventos de clique e pode receber um valor do tipo WaiterAdapterClicksInterface ou um valor nulo (por conta do símbolo "?")
    private var listener: WaiterAdapterClicksInterface? = null

    // Função que define um ouvinte (listener) para os eventos de clique
    fun setListener(listener: WaiterAdapterClicksInterface){
            this.listener = listener
    }
    // Uma ViewHolder serve para gerenciar os itens de RecyclerView's mais facilmente
    inner class WaiterViewHolder(val binding: FragmentWaitersManageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaiterViewHolder {
        val binding = FragmentWaitersManageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WaiterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaiterViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                // Associa o valor do elemento gráfico txtWaiterEmail com o valor estabelecido na classe WaiterData
                binding.txtWaiterEmail.text = this.waiterEmail
                binding.txtWaiterName.text = this.waiterName

                // Associa um ouvinte e uma função ao botão de deletar
                binding.btnDeleteWaiter.setOnClickListener{
                    listener?.onDeleteWaiterBtnClicked(this)
                }

                // Associa um ouvinte e uma função ao botão de editar
                binding.btnEditWaiter.setOnClickListener {
                    listener?.onEditWaiterBtnClicked(this)
                }
            }
        }
    }

    // Função requerida pela classe, verifica a quantidade de itens
    override fun getItemCount(): Int {
        return list.size
    }

    interface WaiterAdapterClicksInterface{
        fun onDeleteWaiterBtnClicked(waiterData: WaiterData)
        fun onEditWaiterBtnClicked(waiterData: WaiterData)
    }
}