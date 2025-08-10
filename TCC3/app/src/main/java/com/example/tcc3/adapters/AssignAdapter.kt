package com.example.tcc3.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.databinding.FragmentAssignItemBinding
import com.example.tcc3.models.SectionData
import com.example.tcc3.models.WaiterData

/*
* Classe: AssignAdapter
* Tipo de classe: Adaptador de RecyclerView que utiliza uma ViewHolder para acessar seus elementos
* Parâmetros:
*   waitersList: MutableList<WaiterData> -> lista mutável de garçons na qual os items são do tipo WaiterData
*   sectionsList: MutableList<SectionData> -> lista mutável de setores na qual os items são do tipo SectionData
* Função: Usado na tela de atribuição garçom-setor como adaptador da RecyclerView utilizada
* */
class AssignAdapter (
    private val waitersList: MutableList<WaiterData>, private val sectionsList: MutableList<SectionData>
): RecyclerView.Adapter<AssignAdapter.AssignViewHolder>() {

    // Define um ViewHolder que se conecta com os itens que serão mostrados na tela de atribuição de setores
    inner class AssignViewHolder(val binding: FragmentAssignItemBinding): RecyclerView.ViewHolder(binding.root)

    // Define um ouvinte conectado aos eventos de clique do adaptador
    private var listener: AssignAdapterClicksInterface? = null // declara um ouvinte da classe, que começa com valor nulo
    fun setListener(listener: AssignAdapterClicksInterface) {
        this.listener = listener // conecta o ouvinte da classe com o ouvinte de tela passado no parâmetro "listener"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignViewHolder {
        // Faz o processo de "inflate"
        val binding = FragmentAssignItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssignViewHolder, position: Int) {
        with(holder) {
            binding.checkboxContainer.visibility = View.GONE // esconde o container das checkbox
            // Define um ouvinte de clique para cada item adicionado na RecyclerView
            // Quando o ouvinte for acionado, esconde o checkboxContainer se ele estiver visível e vice-versa
            binding.imageView3.setOnClickListener {
                if (holder.binding.checkboxContainer.visibility == View.GONE) {
                    holder.binding.checkboxContainer.visibility = View.VISIBLE
                } else {
                    holder.binding.checkboxContainer.visibility = View.GONE
                }
            }
            binding.txtWaiterEmail.text = waitersList[position].waiterName // Substitui o texto do EditText pelo email do garçom

            // Configura o layout das checkbox que vão aparecer
            val checkboxLayout = LinearLayout(holder.binding.root.context) // Define um tipo de layout
            checkboxLayout.orientation = LinearLayout.VERTICAL // Define a orientação do layout

            // Mostra os setores se a lista tiver mais de um elemento
            if (sectionsList.size != 0) {
                // Configura cada checkbox individualmente, uma para cada setor cadastrado
                for (section in sectionsList) {
                    // Configuração dos elementos individuais Checkbox
                    val checkbox = CheckBox(binding.checkboxContainer.context) // Cria um elemento Checkbox
                    checkbox.text = section.sectionName // Define o texto da checkbox
                    checkbox.setTextColor(Color.BLACK) // Define a cor do texto da checkbox
                    checkboxLayout.addView(checkbox) // Adiciona a checkbox ao layout

                    waitersList[position].waiterSections.forEach {
                        if (section.sectionId == it.sectionId) {
                            it.sectionName = section.sectionName
                            checkbox.isChecked = true
                        }
                    }

                    // Adiciona um ouvinte para monitorar a troca de estado da checkbox
                    checkbox.setOnCheckedChangeListener { _, isChecked ->
                        checkbox.isChecked = isChecked // Atualiza o estado da checkbox no código
                        val selectedSectionData = SectionData(section.sectionId, section.sectionName) // Captura os dados do setor que recebeu um check
                        // Verifica se a checkbox está ativa
                        if (checkbox.isChecked) {
                            val selectedWaiterData = WaiterData(
                                waitersList[position].waiterId,
                                waitersList[position].waiterEmail,
                                waiterName = "",
                                waiterPassword = "",
                                waitersList[position].waiterSections,
                            )
                            var waiterContainsSection = false
                            for (selectedWaiterSection in selectedWaiterData.waiterSections) {
                                if (selectedWaiterSection.sectionId == selectedSectionData.sectionId) {
                                    waiterContainsSection = true
                                }
                            }
                            if (!waiterContainsSection) {
                                waitersList[position].waiterSections.add(selectedSectionData)
//                                Toast.makeText(binding.root.context, "Setor atribuido ao garçom", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            waitersList[position].waiterSections.remove(selectedSectionData)
//                            Toast.makeText(binding.root.context, "Setor removido do garçom", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            // Adiciona o layout das checkbox ao container
            binding.checkboxContainer.addView(checkboxLayout)
        }
    }

    override fun getItemCount(): Int {
        return waitersList.size // Retorna o tamanho da lista de garçons
    }

    interface AssignAdapterClicksInterface
}