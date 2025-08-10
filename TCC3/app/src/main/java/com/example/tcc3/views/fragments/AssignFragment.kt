package com.example.tcc3.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tcc3.R
import com.example.tcc3.databinding.FragmentAssignBinding
import com.example.tcc3.adapters.AssignAdapter
import com.example.tcc3.models.SectionData
import com.example.tcc3.models.WaiterData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AssignFragment : Fragment(), AssignAdapter.AssignAdapterClicksInterface {

    // Variáveis do BD
    private lateinit var auth: FirebaseAuth // conexão com o Firebase Auth
    private lateinit var database: DatabaseReference // conexão com o Firebase Realtime Database

    // Variáveis de navegação
    private lateinit var navController: NavController // controlador de navegação
    private lateinit var binding: FragmentAssignBinding // conexão com os elementos xml

    // Variáveis necessárias para exibir os garçons e setores cadastrados
    private lateinit var assignAdapter: AssignAdapter // adaptador de dados da classe
    private lateinit var waitersList: MutableList<WaiterData> // lista de dados dos garçons cadastrados
    private lateinit var sectionsList: MutableList<SectionData>  // lista de dados dos setores cadastrados

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Faz o processo de "inflate" da view
        binding = FragmentAssignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        saveChanges()
        recyclerViewConfig()
        getRecyclerViewData()
        setNavigationEvents()
    }

    /*
    * Função: init(view: View)
    * Parâmetros: (view: View -> recebe o elemento "view" retornado em "onCreateView", que referencia a view atual)
    * Tarefas: inicia as variáveis de navegação, autenticação e conexão com o Firebase
    */
    private fun init(view : View){
        navController = Navigation.findNavController(view) // atribui o controle de navegação dessa view
        auth = FirebaseAuth.getInstance() // inicia a conexão com o Firebase Auth
        database = FirebaseDatabase.getInstance().reference // inicia a conexão com o Firebase Realtime Database
    }

    /*
    * Função: recyclerViewConfig()
    * Tarefas: {
    *   gerencia o layout da recyclerView que mostra os garçons e setores cadastrados;
    *   atribui um adaptador para a recyclerView;
    * }
    */
    private fun recyclerViewConfig() {
        // Definindo um gerenciador de Layout para a RecyclerView (caso contrário, ela não seria exibida)
        binding.sectionsToWaitersRecyclerView.setHasFixedSize(true) // "true" quando o adaptador não pode afetar o tamanho
        binding.sectionsToWaitersRecyclerView.layoutManager = LinearLayoutManager(this.context) // define um tipo de layoutManager

        // Define as duas listas como vazias
        waitersList = mutableListOf()
        sectionsList = mutableListOf()

        // Inicia o adaptador da recyclerView
        assignAdapter = AssignAdapter(waitersList, sectionsList) // cria uma instância com as listas vazias
        assignAdapter.setListener(this) // define um ouvinte do adaptador para essa tela
        binding.sectionsToWaitersRecyclerView.adapter = assignAdapter // conecta a recyclerView ao seu adaptador
    }

    /*
    * Função: getRecyclerViewData()
    * Tarefas: {
    *   acessa o banco de dados e coleta os dados dos garçons;
    *   acessa o banco de dados e coleta os dados dos setores;
    * }
    */
    private fun getRecyclerViewData() {
        // Acessa os registros de usuário no BD e adiciona um ouvinte de eventos de valor (ValueEventListener)
        // O ValueEventListener escuta mudanças nos valores armazenados no BD
        database.child("Users").addValueEventListener( object: ValueEventListener {
            // determina o que acontece quando os dados são alterados
            override fun onDataChange(snapshot: DataSnapshot) {
                // limpa a lista de garçons
                waitersList.clear()

                // obtém ID, email e função(role) de cada registro de usuário
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key.toString() // captura o ID
                    val userDataMap = userSnapshot.getValue<Map<String, Any>>() // cria um mapa de dados no formato (Key: String, Value: Any) que contém os valores dos usuários
                    val userEmail = userDataMap?.get("email").toString() // captura o nome
                    val userName = userDataMap?.get("name").toString() // captura o nome
                    val userRole = userDataMap?.get("role").toString() // captura a função do usuário
                    // Adiciona o usuário a lista de garçons, se ele for um garçom
                    if (userRole == "waiter"){
                        // Cria uma lista para os ID's dos setores vinculados a garçom
                        val waiterSections = mutableListOf<SectionData>()
                        // Captura os ID's no BD e os adiciona a lista em formato de SectionData, mas sem o nome
                        userSnapshot.child("sections").children.forEach { section ->
                            waiterSections.add( SectionData(section.value.toString(), ""))
                        }
                        val user = userSnapshot.key?.let { WaiterData(userId, userEmail, userName, "", waiterSections) } // cria uma WaiterData para o usuário
                        waitersList.add(user!!) // adiciona a WaiterData à lista
                    }
                }
                assignAdapter.notifyDataSetChanged() // notifica a mudança para o adaptador da RecyclerView
            }
            // determina o que acontece quando o processo é cancelado
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show() // exibe um Toast com o erro
            }
        })

        // Acessa os registros dos setores no BD e adiciona um ouvinte de eventos de valor (ValueEventListener)
        // O ValueEventListener escuta mudanças nos valores armazenados no BD
        database.child("Sections").addValueEventListener( object: ValueEventListener {
            // determina o que acontece quando os dados são alterados
            override fun onDataChange(snapshot: DataSnapshot) {
                // limpa a lista de setores
                sectionsList.clear()

                // obtém ID e nome de cada setor
                for (sectionSnapshot in snapshot.children) {
                    val sectionId = sectionSnapshot.key.toString() // captura o ID do setor
                    val sectionDataMap = sectionSnapshot.getValue<Map<String, Any>>() // cria um mapa de dados no formato (Key: String, Value: Any) que contém os valores do registro
                    val sectionName = sectionDataMap?.get("name").toString() // captura o nome do setor
                    val section = sectionSnapshot.key?.let { SectionData (sectionId, sectionName) } // cria uma SectionData com ID e nome, sem mesas
                    sectionsList.add(section!!)
                }
                assignAdapter.notifyDataSetChanged() // notifica a mudança para o adaptador da RecyclerView
            }
            // determina o que acontece quando o processo é cancelado
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show() // exibe um Toast com o erro
            }
        })
    }

    /*
    * Função: saveChanges()
    * Tarefas: Faz o botão de salvar alterações efetivamente realizá-las no Banco de Dados
    */
    private fun saveChanges() {
        // Define um ouvinte para o botão de salvar mudanças
        binding.btnSaveChanges.setOnClickListener {
            // Acessa cada garçom
            waitersList.forEach { waiterData ->
                val waiterSectionsList = mutableListOf<String>() // Cria uma lista para armazenar os ID's dos setores
                // Acessa cada setor vinculado ao garçom
                waiterData.waiterSections.forEach { sectionData ->
                    waiterSectionsList.add(sectionData.sectionId) // Adiciona o Id à lista
                }
                // Atualiza os setores do garçom
                database.child("Users").child(waiterData.waiterId).updateChildren(mapOf("sections" to waiterSectionsList)).addOnCompleteListener { saveSectionChangesResult ->
                    if (saveSectionChangesResult.isSuccessful) {
                        Toast.makeText(context, "Alterações salvas", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, saveSectionChangesResult.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            navController.navigate(R.id.atribuirFragment)
        }
    }

    /*
    * Função: setNavigationEvents()
    * Tarefas: define ouvintes para os botões de navegação
    */
    private fun setNavigationEvents(){
        // Função do botão: mudar para a tela de registros/logs
        binding.imgButtonLogs.setOnClickListener(){
            navController.navigate(R.id.action_atribuirFragment_to_logsFragment)
        }
        // Função do botão: mudar para a tela de status
        binding.imgButtonStatus.setOnClickListener(){
            navController.navigate(R.id.action_atribuirFragment_to_statusFragment)
        }
        // Função do botão: mudar para a tela Home
        binding.imgButtonHome.setOnClickListener(){
            navController.navigate(R.id.action_atribuirFragment_to_homeFragment)
        }
    }
}