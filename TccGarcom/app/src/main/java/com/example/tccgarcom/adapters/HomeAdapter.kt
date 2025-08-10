package com.example.tccgarcom.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.utils.WebSocketClient
import com.example.tccgarcom.R
import com.example.tccgarcom.models.WaiterData
import com.example.tccgarcom.databinding.FragmentHomeItemBinding
import com.example.tccgarcom.utils.CallsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Adaptador da RecyclerView da Home que exibe os botões de chamado
// Recebe os dados do garçom (waiterData) e o cliente WebSocket
class HomeAdapter(private val waiterData: WaiterData,  private var webSocketClient: WebSocketClient): RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    // Define um ViewHolder que permite a manipulação de diferentes Views de forma individual
    inner class HomeViewHolder(val binding: FragmentHomeItemBinding): RecyclerView.ViewHolder(binding.root)

    // Variável para armazenar todas as mesas de todos os setores vinculados ao garçom
    private val tablesList: MutableList<Int> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = FragmentHomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }


    // Código que é executado quando uma nova View é conectada à tela (repete uma vez para cada elemento gerado)
    // O número de repetições depende da função getItemCount()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        webSocketClient.waiterSections = waiterData.waiterSections

        with(holder) {
            // Captura o número da mesa que está sendo percorrida por meio de seu índex
            val tableNumber = tablesList[position]

            // Define uma String vazia que receberá um valor a depender do número da mesa
            // Usada em conjunto com a classe WebSocket para verificar o status do botão (chamando ou não)
            var btnKey = ""
            // O código abaixo deve ser utilizado por conta da configuração das mensagens enviadas pelo servidor do Hardware,
            // onde os botões de 1 a 8 são de mesas e os de 9 a 12 são da cozinha
            // "Quando o número da mesa for igual a  x -> execute este código"
            when (tableNumber) {
                1, 2, 3, 4, 5, 6, 7, 8 -> btnKey = "stMesa$tableNumber"
                9, 10, 11, 12 -> btnKey = "stGar${tableNumber-8}"
            }

            if (webSocketClient.btnStates[btnKey] == true) {
                binding.txtTable.text = "Mesa $tableNumber - Chamando"
            } else {
                binding.txtTable.text = "Mesa $tableNumber"
            }

            // Define um ouvinte de toque para a ImageView que cobre o botão
            binding.homeItemBackground.setOnClickListener {
                // Usa a chave do botão para buscar seu status e verifica se ele está ativo (isActive == true)
                if (webSocketClient.btnStates[btnKey] == true) {

                    MaterialAlertDialogBuilder(binding.root.context, com.google.android.material.R.style.Theme_MaterialComponents_Dialog_Alert)
                        .setTitle(R.string.alert_dialog_title)
                        .setMessage("Assumir atendimento da mesa $tableNumber")
                        .setPositiveButton(R.string.alert_dialog_positive_button) { _, _ ->
                            // Envia a mensagem que alterna o status do botão por meio do WebSocket
                            webSocketClient.sendMessage("toggle$tableNumber")
                            binding.txtTable.text = "Mesa $tableNumber"
                            Toast.makeText(binding.root.context, R.string.msg_alert_dialog_confirm, Toast.LENGTH_SHORT).show()

                            CallsManager(waiterData.waiterId).checkLastCall(btnKey, true)
                        }
                        .setNegativeButton(R.string.alert_dialog_negative_button) { _, _ -> }
                        .show()

                }
            }
        }
    }

    // Define quantos itens a Recycler deve ter (quantas vezes deve ser executado um BindViewHolder)
    override fun getItemCount(): Int {
        // Para cada setor vinculado ao garçom:
        for (section in waiterData.waiterSections) {
            // Para cada mesa nesse setor:
            for (table in section.sectionTables) {
                // Caso ainda não esteja na lista (pode ocorrer de uma mesa estar em dois ou mais setores):
                if (table !in tablesList) {
                    // Adiciona a mesa na lista de mesas do garçom
                    tablesList.add(table)
                }
            }
        }

        // Retorna a quantidade de mesas na lista
        return tablesList.size
    }
}