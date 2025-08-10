package com.example.tccgarcom.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tcc3.utils.WebSocketClient
import com.example.tccgarcom.R
import com.example.tccgarcom.adapters.HomeAdapter
import com.example.tccgarcom.databinding.FragmentHomeBinding
import com.example.tccgarcom.models.SectionData
import com.example.tccgarcom.models.WaiterData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class HomeFragment : Fragment() {
    // Variáveis do Firebase
    private lateinit var database: FirebaseDatabase // Conexão com o BD
    private lateinit var auth: FirebaseAuth // Conexão com o serviço de autenticação

    // Variáveis para os chamados
    private lateinit var waiterData: WaiterData // Coleção de dados do garçom (usuário)
    lateinit var homeAdapter: HomeAdapter // Adaptador da RecyclerView que exibe os botões

    // Variáveis de navegação
    private lateinit var binding: FragmentHomeBinding // Conexão com os elementos da tela
    private lateinit var navController: NavController // Controlador de navegação

    // Variáveis do WebSocket (comunicação app/hardware)
    lateinit var webSocketClient: WebSocketClient // Cliente WebSocket
    private var isWebSocketConnected = false // Armazena o status da conexão com o WebSocket, usado para evitar redundância de conexões

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Faz o processo de Inflate da View
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Desconecta o webSocket ao fechar ("destruir") a View
    override fun onDestroyView() {
        super.onDestroyView()

        // Se o cliente WebSocket estiver inicializado e o status armazenado confirmar:
        if (this::webSocketClient.isInitialized && isWebSocketConnected) {
            // Desconecta do webSocket
            webSocketClient.disconnect()
            // Atualiza o status da conexão
            isWebSocketConnected = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view) // Captura as instâncias do Firebase, inicia o controlador de navegação e configura o botão de logout
        setLogOutClickListener()
        getWaiterData() // Captura os dados do garçom e associa aos Id's dos setores
        setSectionsValueEventListener() // Usa os Id's dos setores para capturar seus dados
        setCallsValueEventListener()
        setWebSocketClientConfig() // Configura o cliente webSocket para essa View
        setRecyclerViewConfig() // Configura o LayoutManager da RecyclerView e seu adaptador
    }

    private fun init(view: View) {
        database = FirebaseDatabase.getInstance() // Captura uma referência a uma instância do Firebase Database
        auth = FirebaseAuth.getInstance() // Captura uma referênca a uma instância do Firebase Auth
        navController = Navigation.findNavController(view) // Inicia o controlador de navegação
    }
    private fun setLogOutClickListener() {
        // Define um ouvinte de clique para o botão de logout
        binding.imgBtnLogout.setOnClickListener {
            // Desconecta o usuário
            auth.signOut()
            // Verifica se o usuário foi realmente desconectado e o envia para a tela de login
            if (auth.currentUser == null) {
                navController.navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }

    private fun getWaiterData() {
        // Usa o ID do usuário para capturar seu email
        val waiterId = auth.currentUser!!.uid
        val waiterEmail = auth.currentUser!!.email

        // Guarda ID e email em uma classe de dados própria
        waiterData = WaiterData(waiterId, waiterEmail!!, "")
        setUsersValueEventListener(waiterId)
    }
    private fun setUsersValueEventListener(waiterId: String) {
        // Acessa os registros do usuário no BD
        database.reference.child("Users").child(waiterId).addValueEventListener( object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userDataMap = snapshot.getValue<Map<String, Any>>()
                val userName = userDataMap?.get("name").toString()
                waiterData.waiterName = userName

                for (section in snapshot.children) {
                    // Cria uma lista vazia (ou limpa a existente) para guardar as informações dos setores
                    waiterData.waiterSections = mutableListOf()
                    // Percorre todos os ID's armazenadas no registro do usuário e as adiciona na lista de setores como SectionData's, apenas com seus ID's
                    for (sectionKey in section.children) {
                        waiterData.waiterSections.add( SectionData(sectionKey.value.toString()) )
                    }
                }

                binding.textViewWaiterName.text = getText(R.string.txt_welcome).toString() + userName
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setSectionsValueEventListener() {
        // Acessa o registro dos setores no BD
        database.reference.child("Sections").addValueEventListener( object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Percorre todos os setores armazenados
                for (sectionData in snapshot.children) {
                    // Repete uma vez para cada setor na lista de setores (preenchida em getWaiterData())
                    for (section in waiterData.waiterSections) {
                        // Se o ID do setor armazenado no BD for igual ao ID da lista
                        if (sectionData.key == section.sectionId) {
                            // Captura o nome do setor
                            val sectionName = sectionData.getValue<Map<String, Any>>()?.get("name")
                            // Captura as mesas do setor e as coloca em uma lista
                            val sectionTablesList = sectionData.child("tables").getValue<List<Int>>()
                            val sectionTables = mutableListOf<Int>()
                            if (sectionTablesList != null) {
                                for (table in sectionTablesList) {
                                    sectionTables.add(table)
                                }
                            }

                            // Atualiza os dados do setor na lista
                            section.sectionName = sectionName.toString()
                            section.sectionTables = sectionTables
                        }
                    }
                }
                // Atualiza a RecyclerView com os novos dados
                homeAdapter.notifyDataSetChanged()
                // Se o WebSocket estiver conectado, passa os dados dos setores
                if (isWebSocketConnected) {
                    webSocketClient.waiterSections = waiterData.waiterSections
                }

                binding.textViewWaiterSections.text = buildString {
                    for (section in waiterData.waiterSections) {
                        this.append("[")
                        this.append(section.sectionName)
                        this.append("] ")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setCallsValueEventListener() {
        database.reference.child("LastCalls").addValueEventListener ( object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.callsRecyclerView.adapter = null
                homeAdapter = HomeAdapter(waiterData, webSocketClient)
                binding.callsRecyclerView.adapter = homeAdapter
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setWebSocketClientConfig() {
        // Se o WebSocket não estiver conectado:
        if (!isWebSocketConnected) {
            // Inicia o cliente e passa os setores. Na criação, os setores estarão vazios.
            // Eles serão atualizados quando os dados forem capturados em getSectionData()
            webSocketClient = WebSocketClient(waiterData.waiterSections)
            // Conecta ao servidor local
            webSocketClient.connect("ws://192.168.0.5/ws", requireContext())
            // Atualiza o estado do WebSocket
            isWebSocketConnected = true
        }
    }
    private fun setRecyclerViewConfig() {
        // Configurações necessárias do layout da RecyclerView
        binding.callsRecyclerView.setHasFixedSize(true)
        binding.callsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Configura o adaptador da RecyclerView
        homeAdapter = HomeAdapter(waiterData, webSocketClient) // Cria uma instância do adaptador, passa os dados do garçom e o cliente WebSocket
        binding.callsRecyclerView.adapter = homeAdapter // Conecta a RecyclerView ao seu adaptador
    }

}
