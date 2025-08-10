package com.example.tcc3.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.tcc3.R
import com.example.tcc3.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        navigationEvents()
    }

    private fun init(view : View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun navigationEvents(){
//       Função do botão: mudar para a tela de gerenciar garçom
        binding.imgButtonGarcons.setOnClickListener(){
            navController.navigate(R.id.action_homeFragment_to_gerenciarFragment)
        }

//        Função do botão: mudar para a tela de atribuir garçom
        binding.imgButtonAtribuir.setOnClickListener(){
            navController.navigate(R.id.action_homeFragment_to_atribuirFragment)
        }

//        Função do botão: mudar para a tela de setores
        binding.imgButtonMesas.setOnClickListener(){
            navController.navigate(R.id.action_homeFragment_to_setoresFragment)
        }

//        Função do botão: mudar para a tela de registros/logs
        binding.imgButtonLogs.setOnClickListener(){
            navController.navigate(R.id.action_homeFragment_to_logsFragment)
        }

//        Função do botão: mudar para a tela de status
        binding.imgButtonStatus.setOnClickListener(){
            navController.navigate(R.id.action_homeFragment_to_statusFragment)
        }

        binding.imgBtnLogout.setOnClickListener {
            auth.signOut()
            if (auth.currentUser == null) {
                navController.navigate(R.id.action_homeFragment_to_entrarFragment)
            }
        }
    }

}