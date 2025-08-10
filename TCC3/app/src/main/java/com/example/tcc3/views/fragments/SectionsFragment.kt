package com.example.tcc3.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tcc3.R
import com.example.tcc3.adapters.SectionAdapter
import com.example.tcc3.databinding.FragmentSectionsBinding
import com.example.tcc3.models.SectionData
import com.example.tcc3.views.popups.AddSectionPopupFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SectionsFragment : Fragment(), AddSectionPopupFragment.ConfirmSectionChangeBtnClickListener, SectionAdapter.SectionAdapterClicksInterface {

    // Variáveis do Firebase
    private lateinit var auth: FirebaseAuth // conexão com Firebase Auth
    private lateinit var database: DatabaseReference // conexão com Firebase Realtime Database

    // Variáveis de navegação
    private lateinit var navController: NavController // controlador de navegação
    private lateinit var binding: FragmentSectionsBinding // conexão com elementos XML

    // Variáveis de PopUp
    private var popUpFragment: AddSectionPopupFragment? = null // tela de adição de setores

    // Variáveis de setores
    private lateinit var sectionAdapter: SectionAdapter // adaptador da RecyclerView dos setores
    private lateinit var sectionsMutableList: MutableList<SectionData> // lista de setores no formato SectionData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Faz o processo de "Inflate"
        binding = FragmentSectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        saveTotalTablesNumber()
        sectionsRecyclerViewConfig()
        getSectionDataFromFirebase()
        navigationEvents()
        openPopUpEventConfig()
    }

    /*
    * Função: getTotalTablesNumber()
    * Tarefas: acessa as preferências salvas e captura a quantidade de mesas no estabelecimento
    * Retorna: (totalTables: Int)
    */
    private fun getTotalTablesNumber(): Int {
        // Acessa as preferências
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        // Captura o valor de "totalTables" ou retorna "0" se não houver
        val totalTables = preferences?.getString("totalTables", "0")!!.toInt()
        // Retorna a variável
        return totalTables
    }
    /*
    * Função: saveTotalTablesNumber()
    * Tarefas: Salva a quantidade de mesas assim que seu valor é alterado
    */
    private fun saveTotalTablesNumber() {
        // Define uma função que é ativada quando o texto da EditText é alterado
        binding.editTextTotalTables.doAfterTextChanged {
            // Captura o novo valor
            val totalTables = binding.editTextTotalTables.text.toString()
            // Acessa as preferências
            val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
            // Salva o novo valor nas preferências com a chave "totalTables"
            sharedPreferences?.edit()?.putString("totalTables", totalTables)?.apply()
        }
    }

    /*
    * Função: init(view: View)
    * Parâmetros: (view: View) -> view/tela atual, recebida em onCreateView
    * Tarefas:
    *   Inicia variáveis de navegação, autenticação e conexão com Firebase
    *
    */
    private fun init(view : View){
        navController = Navigation.findNavController(view) // vincula o controlador de navegação dessa View
        auth = FirebaseAuth.getInstance() // obtém uma instância (conexão) do FirebaseAuth
        database = FirebaseDatabase.getInstance().reference // obtém uma instância do banco de dados

        binding.editTextTotalTables.setText(getTotalTablesNumber().toString())
    }

    /*
    * Função: sectionsRecyclerViewConfig()
    * Tarefas: Configura a RecyclerView que exibe o nome dos setores e os botões de edição/exclusão
    * */
    private fun sectionsRecyclerViewConfig() {
        // Configurações do Layout
        binding.sectionsRecyclerView.setHasFixedSize(true)
        binding.sectionsRecyclerView.layoutManager = LinearLayoutManager(this.context)

        // Definindo lista de setores como vazia
        sectionsMutableList = mutableListOf()

        // Configuração do adaptador da RecyclerView
        sectionAdapter = SectionAdapter(sectionsMutableList) // iniciando instância do adaptador
        sectionAdapter.setListener(this) // definindo ouvinte para essa View
        binding.sectionsRecyclerView.adapter = sectionAdapter // conectando a RecyclerView ao adaptador configurado
    }

    /* Função: getSectionDataFromFirebase()
    * Tarefas: Captura os dados dos setores cadastrados e os adiciona na lista de setores
    */
    private fun getSectionDataFromFirebase() {
        // Acessa os registros dos setores e adiciona um ValueEventListener
        database.child("Sections").addValueEventListener( object: ValueEventListener {
            // Define o que acontece quando os valores são alterados
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpa a lista de setores
                sectionsMutableList.clear()

                // Captura os dados dos setores e adiciona à lista
                for (sectionSnapshot in snapshot.children) {
                    // Capturando dados
                    val sectionId = sectionSnapshot.key.toString() // obtém o ID do setor
                    val sectionDataMap = sectionSnapshot.getValue<Map<String, Any>>() // cria um mapa com os valores do registro no formato (Key: String, Value: Any)
                    val sectionName = sectionDataMap?.get("name").toString() // obtém o nome do setor
                    // Atualizando lista
                    val section = sectionSnapshot.key?.let { SectionData (sectionId, sectionName) } // cria um objeto SectionData com os valores capturados
                    if (section != null) {
                        sectionsMutableList.add(section) // adiciona o setor na lista de setores
                    }
                }
                // Notifica as mudanças para o adaptador
                sectionAdapter.notifyDataSetChanged()
            }

            // Define o que acontece quando a tarefa é cancelada/interrompida
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show() // mostra o erro em um Toast
            }
        })
    }

    /*
    * Função: openPopUpEventConfig()
    * Tarefas: Configura a chamada para o PopUp de adição de setores
    */
    private fun openPopUpEventConfig() {
        // Define um ouvinte de clique para o botão de adicionar setores
        binding.imgButtonAdicionar.setOnClickListener {
            // Remove qualquer PopUp que esteja salvo
            if (popUpFragment != null) {
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            }

            // Captura a quantidade total de mesas do estabelecimento
            val totalTables = binding.editTextTotalTables.text.toString()
            if (totalTables.isNullOrEmpty()) {
                Toast.makeText(context, "Insira alguma quantidade de mesas", Toast.LENGTH_SHORT).show()
            } else {
                if (totalTables.toInt() <= 0) {
                    Toast.makeText(context, "A quantidade de mesas deve ser superior a zero!", Toast.LENGTH_SHORT).show()
                } else {
                    // Inicia o PopUp
                    popUpFragment = AddSectionPopupFragment(totalTables.toInt()) // inicia uma instância do PopUp, passando a quantidade de mesas
                    popUpFragment!!.setListener(this) // define um ouvinte para o PopUp
                    popUpFragment!!.show(childFragmentManager, AddSectionPopupFragment.TAG) // mostra o PopUp
                }
            }


        }
    }

    private fun navigationEvents() {
//        Função do botão: mudar para a tela de registros/logs
        binding.imgButtonLogs.setOnClickListener {
            navController.navigate(R.id.action_setoresFragment_to_logsFragment)
        }

//        Função do botão: mudar para a tela de status
        binding.imgButtonStatus.setOnClickListener(){
            navController.navigate(R.id.action_setoresFragment_to_statusFragment)
        }

//        Função do botão: mudar para a tela Home
        binding.imgButtonHome.setOnClickListener(){
            navController.navigate(R.id.action_setoresFragment_to_homeFragment)
        }
    }

    override fun onSaveSection(sectionName: String, txtSectionName: TextInputEditText, selectedTables: MutableList<Int>) {
        val sectionReference = database.child("Sections").push()
        val sectionDataMap = mapOf("name" to sectionName, "tables" to selectedTables.sorted())

        sectionReference.updateChildren(sectionDataMap).addOnCompleteListener { saveSectionResult ->
            if (saveSectionResult.isSuccessful) {
                Toast.makeText(context, "Setor adicionado", Toast.LENGTH_SHORT).show()

                txtSectionName.text = null
                popUpFragment!!.dismiss()
            } else {
                Toast.makeText(context, saveSectionResult.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUpdateSection(sectionData: SectionData, txtSectionName: TextInputEditText, selectedTables: MutableList<Int>) {
        val sectionsReference = database.child("Sections")

        val map = HashMap<String, Any>()
        map[sectionData.sectionId] = mapOf("name" to sectionData.sectionName, "tables" to selectedTables.sorted())
        sectionsReference.updateChildren(map).addOnCompleteListener { updateSectionResult ->
            if (updateSectionResult.isSuccessful){
                Toast.makeText(context, "Setor atualizado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, updateSectionResult.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popUpFragment!!.dismiss()
        }
    }

    override fun onDeleteSectionBtnClicked(sectionData: SectionData) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Popup_Theme_TCC3)
            .setTitle("Deletar")
            .setMessage("Deletar setor?")
            .setPositiveButton("Sim") { _, _ ->
                val sectionsReference = database.child("Sections").child(sectionData.sectionId)
                sectionsReference.removeValue().addOnCompleteListener { deleteSectionResult ->
                    if (deleteSectionResult.isSuccessful) {
                        Toast.makeText(context, "Setor deletado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            deleteSectionResult.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar") { _,_ -> }
            .show()
    }

    override fun onEditSectionBtnClicked(sectionData: SectionData) {

        val totalTables = binding.editTextTotalTables.text.toString()
        if (totalTables.isNullOrEmpty()) {
            Toast.makeText(context, "Insira alguma quantidade de mesas", Toast.LENGTH_SHORT).show()
        } else {
            if (totalTables.toInt() <= 0) {
                Toast.makeText(context, "A quantidade de mesas deve ser superior a zero!", Toast.LENGTH_SHORT).show()
            } else {
                if (popUpFragment != null) {
                    childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
                } else {
                    childFragmentManager.beginTransaction().commit()
                }
                popUpFragment = AddSectionPopupFragment.newInstance(sectionData, totalTables.toInt())
                popUpFragment!!.setListener(this)
                popUpFragment!!.show(childFragmentManager, AddSectionPopupFragment.TAG)
            }
        }
    }
}