package com.example.tcc3.views.fragments

import android.content.Context
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
import com.example.tcc3.adapters.StatusAdapter
import com.example.tcc3.databinding.FragmentStatusBinding
import com.example.tcc3.websocket.WebSocketClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var binding: FragmentStatusBinding
    private lateinit var navController: NavController

    private lateinit var webSocketClient: WebSocketClient
    private var isWebSocketConnected = false // Flag to check connection status

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        webSocketClientConfigure()
        recyclerViewConfigure()
        navigationEvents()
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun webSocketClientConfigure() {
        // Configurar o WebSocketClient apenas se ainda não estiver conectado
        if (!isWebSocketConnected) {
            webSocketClient = WebSocketClient()
            webSocketClient.connectWebSocket("ws://192.168.0.5/ws")
            isWebSocketConnected = true
        }
    }

    private fun recyclerViewConfigure() {
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val totalTables = preferences?.getString("totalTables", "0")!!.toInt()

        binding.statusRecyclerView.setHasFixedSize(true)
        binding.statusRecyclerView.layoutManager = LinearLayoutManager(context)

        val statusAdapter = StatusAdapter(totalTables, webSocketClient)
        binding.statusRecyclerView.adapter = statusAdapter
    }

    private fun navigationEvents() {
        // Função do botão: mudar para a tela Home
        binding.imgButtonHome.setOnClickListener {
            navController.navigate(R.id.action_statusFragment_to_homeFragment)
        }

        // Função do botão: mudar para a tela Logs
        binding.imgButtonLogs.setOnClickListener {
            navController.navigate(R.id.action_statusFragment_to_logsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fechar a conexão WebSocket se necessário quando o fragmento for destruído
        if (this::webSocketClient.isInitialized && isWebSocketConnected) {
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }
}
