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
import com.example.tcc3.databinding.FragmentSignUpBinding
import com.example.tcc3.utils.InputManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun registerEvents() {

        binding.textViewEntrar.setOnClickListener {
            navControl.navigate(R.id.action_registrarFragment_to_entrarFragment)
        }

        binding.imageButtonSave.setOnClickListener {
            val email = binding.textInputEmail.text.toString().trim()
            val name = binding.textInputName.text.toString().trim()
            val password = binding.textInputPassword.text.toString().trim()
            val confirmPassword = binding.textInputRePass.text.toString().trim()

            val inputManager = InputManager(requireContext())
            if (!inputManager.hasEmptyData(listOf(email, name, password, confirmPassword))) {
                if (inputManager.isEmailCorrectlyFormatted(email)) {
                    if (inputManager.doesConfirmPasswordMatch(password, confirmPassword)) {
                        if (inputManager.isPasswordStrong(password)) {
                            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createUserResult ->
                                if (createUserResult.isSuccessful) {
                                    val userReference = database.reference.child("Users").child(createUserResult.result.user?.uid.toString())
                                    val userRole: Map<String, String> = mapOf("name" to name, "role" to "adm")
                                    userReference.updateChildren(userRole).addOnCompleteListener { saveRoleResult ->
                                        if (saveRoleResult.isSuccessful){
                                            Toast.makeText(context, R.string.msg_registrado, Toast.LENGTH_SHORT).show()
                                            navControl.navigate(R.id.action_registrarFragment_to_homeFragment)
                                        }
                                        else{
                                            Toast.makeText(context, createUserResult.exception?.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, createUserResult.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}