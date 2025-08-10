package com.example.tcc3.views.popups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.tcc3.databinding.FragmentAddWaiterPopupBinding
import com.example.tcc3.models.WaiterData
import com.example.tcc3.utils.InputManager
import com.google.android.material.textfield.TextInputEditText

class AddWaiterPopupFragment : DialogFragment() {

    private lateinit var binding: FragmentAddWaiterPopupBinding // Conexão com elementos xml
    private lateinit var listener: SaveWaiterBtnClickListener // Ouvinte de cliques
    private var waiterData : WaiterData? = null // Classe de dados dos garçons

    // Define um ouvinte
    fun setListener(listener: SaveWaiterBtnClickListener){
        this.listener = listener
    }

    companion object{
        const val TAG = "AddGarcomPopupFragment"

        @JvmStatic
        fun newInstance(waiterId: String, waiterEmail: String, waiterName:String, waiterPassword: String) = AddWaiterPopupFragment().apply {
            arguments = Bundle().apply {
                putString("waiterId", waiterId)
                putString("waiterEmail", waiterEmail)
                putString("waiterName", waiterName)
                putString("waiterPassword", waiterPassword)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddWaiterPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Preenche os campos de texto se algum argumento tiver sido passado ao acionar o Popup (no caso de edição)
        if (arguments != null) {
            binding.txtInputWaiterEmail.isEnabled = false

            waiterData = WaiterData(
                arguments?.getString("waiterId").toString(),
                arguments?.getString("waiterEmail").toString(),
                arguments?.getString("waiterName").toString(),
                arguments?.getString("waiterPassword").toString()
            )

            binding.txtInputWaiterEmail.setText(waiterData?.waiterEmail)
            binding.txtInputWaiterName.setText(waiterData?.waiterName)
            binding.txtInputPassword.setText(waiterData?.waiterPassword)
            binding.txtInputConfirmPassword.setText(waiterData?.waiterPassword)
        }
        registerEvents()
    }

    private fun registerEvents(){
        // Adiciona ou atualiza os dados
        binding.btnAddWaiter.setOnClickListener{
            val waiterEmail = binding.txtInputWaiterEmail.text.toString().trim()
            val waiterName = binding.txtInputWaiterName.text.toString().trim()
            val waiterPassword = binding.txtInputPassword.text.toString().trim()
            val waiterConfirmPassword = binding.txtInputConfirmPassword.text.toString().trim()

            val inputManager = InputManager(requireContext())
            if (!inputManager.hasEmptyData(listOf(waiterEmail, waiterName, waiterPassword, waiterConfirmPassword))) {
                if (inputManager.isEmailCorrectlyFormatted(waiterEmail)) {
                    if (inputManager.doesConfirmPasswordMatch(waiterPassword, waiterConfirmPassword)) {
                        if (inputManager.isPasswordStrong(waiterPassword)) {
                            if (waiterData == null){
                                listener.onSaveWaiter(
                                    waiterEmail, binding.txtInputWaiterEmail,
                                    waiterName, binding.txtInputWaiterName,
                                    waiterPassword, binding.txtInputPassword,
                                    )
                            } else{
                                waiterData?.waiterEmail = waiterEmail
                                waiterData?.waiterName = waiterName
                                waiterData?.waiterPassword = waiterPassword
                                listener.onUpdateWaiter(
                                    waiterData!!,
                                    binding.txtInputWaiterEmail,
                                    binding.txtInputWaiterName,
                                    binding.txtInputPassword
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    interface SaveWaiterBtnClickListener {
        fun onSaveWaiter(
            waiterEmail: String, txtWaiterEmail: TextInputEditText,
            waiterName: String, txtWaiterName: TextInputEditText,
            waiterPassword: String, txtWaiterPassword: TextInputEditText
        )
        fun onUpdateWaiter(
            waiterData: WaiterData,
            txtWaiterEmail: TextInputEditText,
            txtWaiterName: TextInputEditText,
            txtWaiterPassword: TextInputEditText,
        )
    }
}