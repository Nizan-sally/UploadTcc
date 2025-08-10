package com.example.tccgarcom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.tccgarcom.R
import com.example.tccgarcom.databinding.FragmentLoginBinding
import com.example.tccgarcom.utils.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.log

class LoginFragment : Fragment() {

    // Variáveis de tela e navegação
    private lateinit var binding: FragmentLoginBinding // Conexão com a tela
    private lateinit var navigation: NavController // Controlador da navegação entre telas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Faz o processo de Inflate da View
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        setClickListeners()
    }

    private fun init(view:View){
        navigation = Navigation.findNavController(view) // Inicia o controlador de navegação
    }

    private fun setClickListeners() {
        // Define um ouvinte para o botão de login
        binding.imageButtonLogin.setOnClickListener {
            // Captura os valores de email e senha inseridos na tela
            val email = binding.txtInputEmail.text.toString().trim()
            val password = binding.txtInputPassword.text.toString().trim()

            // Cria uma instância da classe LoginManager
            val loginManager = LoginManager(requireContext())
            // Faz a verificação do login
            loginManager.doLoginVerification(email, password) {
                navigation.navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }
}