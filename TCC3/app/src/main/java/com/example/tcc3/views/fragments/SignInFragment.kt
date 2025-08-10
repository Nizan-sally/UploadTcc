package com.example.tcc3.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.tcc3.R
import com.example.tcc3.databinding.FragmentSignInBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignInFragment : Fragment() {

    // Variáveis de tela e navegação
    private lateinit var binding : FragmentSignInBinding // Conexão com a tela
    private lateinit var navigation: NavController // Gerenciador da navegação entre telas

    // Variáveis do Firebase
    private lateinit var auth: FirebaseAuth // Conexão com Firebase Auth
    private lateinit var database: FirebaseDatabase // Conexão com Firebase Realtime Database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    // Inicia as conexões e controladores fundamentais
    private fun init(view:View){
        navigation = Navigation.findNavController(view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    // Atribui funções aos eventos de clique
    private fun registerEvents() {
        // Elemento: link para se increver no app
        // Ação: leva para a tela de registro
        binding.textViewInscrever.setOnClickListener {
            navigation.navigate(R.id.action_entrarFragment_to_registrarFragment)
        }

        // Elemento: botão de efetuar login
        // Ação: chama a função que verifica o login, informando as caixas de texto de senha e email
        binding.imageButtonLogin.setOnClickListener {
            verifyEmailAndPassword(binding.txtInputEmail, binding.txtInputPassword)
        }

        // Elemento: link para recuperar senha
        // Ação: chama a função que lida com recuperação de senha
        binding.txtForgotPass.setOnClickListener {
            forgotPassword(binding.txtInputEmail)
        }
    }

    private fun verifyEmailAndPassword(emailInput: TextInputEditText, passwordInput: TextInputEditText) {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    val userDataReference = database.reference.child("Users").child(authResult.result.user!!.uid)
                    userDataReference.get().addOnCompleteListener { getRoleResult ->
                        if (getRoleResult.isSuccessful){
                            val role = getRoleResult.result.child("role").value
                            if (role == "adm"){
                                Toast.makeText(context, R.string.msg_authenticated, Toast.LENGTH_SHORT).show()
                                navigation.navigate(R.id.action_entrarFragment_to_homeFragment)
                            } else{
                                Toast.makeText(context, R.string.msg_user_do_not_have_permission, Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        } else {
                            Toast.makeText(context, getRoleResult.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, R.string.msg_wrong_password, Toast.LENGTH_SHORT).show()
                }
            }
        } else{
            Toast.makeText(context, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show()
        }
    }

    private fun forgotPassword(emailInput: TextInputEditText) {
        val email = emailInput.text.toString().trim()

        if (email.isNotEmpty()){
            auth.sendPasswordResetEmail(email, ActionCodeSettings.zzb()).addOnSuccessListener{
                Toast.makeText(context, R.string.msg_forgot_password, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show()
        }
    }
}