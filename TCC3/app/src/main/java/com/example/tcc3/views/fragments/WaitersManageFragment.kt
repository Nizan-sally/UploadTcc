package com.example.tcc3.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tcc3.R
import com.example.tcc3.adapters.WaiterAdapter
import com.example.tcc3.databinding.FragmentWaitersManageBinding
import com.example.tcc3.firebase.FirebaseHelper // Importando o FirebaseHelper
import com.example.tcc3.models.WaiterData
import com.example.tcc3.views.popups.AddWaiterPopupFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

class WaitersManageFragment : Fragment(), AddWaiterPopupFragment.SaveWaiterBtnClickListener, WaiterAdapter.WaiterAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth // autenticação do Firebase
    private lateinit var database: FirebaseDatabase // conexão com o Firebase
    private lateinit var firebaseHelper: FirebaseHelper // instância do FirebaseHelper

    private lateinit var apiService: ApiService
    private lateinit var navController: NavController // controle de navegação
    private lateinit var binding: FragmentWaitersManageBinding // conexão com a tela (xml)
    private var popUpFragment: AddWaiterPopupFragment? = null // conexão entre tela e PopUp de registro
    private lateinit var adapter: WaiterAdapter // adaptador da classe
    private lateinit var mList: MutableList<WaiterData> // dados dos garçons em forma de lista

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Transformando xml em uma View "codificável" (processo de "inflate")
        binding = FragmentWaitersManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        recyclerViewConfig()
        retrofitConfig()
        getDataFromFirebase()
        registerEvents()
        navigationEvents()
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseHelper = FirebaseHelper() // Inicializando o FirebaseHelper
    }

    private fun retrofitConfig() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://allevat.pythonanywhere.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun recyclerViewConfig() {
        binding.reclyclerView.setHasFixedSize(true)
        binding.reclyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = WaiterAdapter(mList)
        adapter.setListener(this)
        binding.reclyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        firebaseHelper.getWaiters(
            onDataReceived = { waiters ->
                mList.clear()
                mList.addAll(waiters)
                adapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun registerEvents() {
        binding.btnOpenRegisterPopup.setOnClickListener {
            if (popUpFragment != null) {
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            }
            popUpFragment = AddWaiterPopupFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(childFragmentManager, AddWaiterPopupFragment.TAG)
        }
    }

    private fun navigationEvents() {
        binding.imgButtonLogs.setOnClickListener {
            navController.navigate(R.id.action_gerenciarFragment_to_logsFragment)
        }
        binding.imgButtonStatus.setOnClickListener {
            navController.navigate(R.id.action_gerenciarFragment_to_statusFragment)
        }
        binding.imgButtonHome.setOnClickListener {
            navController.navigate(R.id.action_gerenciarFragment_to_homeFragment)
        }
    }

    override fun onSaveWaiter(
        waiterEmail: String, txtWaiterEmail: TextInputEditText,
        waiterName: String, txtWaiterName: TextInputEditText,
        waiterPassword: String, txtWaiterPassword: TextInputEditText
    ) {
        firebaseHelper.createWaiter(
            email = waiterEmail,
            password = waiterPassword,
            onSuccess = { userId ->
                val userData = mapOf("email" to waiterEmail, "name" to waiterName, "role" to "waiter")
                firebaseHelper.saveWaiterData(userId, userData) { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(context, R.string.msg_garcom_cadastrado, Toast.LENGTH_SHORT).show()
                        txtWaiterEmail.text = null
                        txtWaiterName.text = null
                        txtWaiterPassword.text = null
                        popUpFragment!!.dismiss()
                    } else {
                        Toast.makeText(context, "Erro ao salvar dados do garçom.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onUpdateWaiter(
        waiterData: WaiterData,
        txtWaiterEmail: TextInputEditText,
        txtWaiterName: TextInputEditText,
        txtWaiterPassword: TextInputEditText
    ) {
        sendEditUserRequest(apiService, waiterData)
        // Atualiza dados do garçom no Firebase
        val userData = mapOf("email" to waiterData.waiterEmail, "name" to waiterData.waiterName, "role" to "waiter")
        firebaseHelper.saveWaiterData(waiterData.waiterId, userData) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, R.string.msg_garcom_atualizado, Toast.LENGTH_SHORT).show()
                txtWaiterEmail.text = null
                txtWaiterName.text = null
                txtWaiterPassword.text = null
                popUpFragment!!.dismiss()
            } else {
                Toast.makeText(context, "Erro ao atualizar dados do garçom.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDeleteWaiterBtnClicked(waiterData: WaiterData) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Popup_Theme_TCC3)
            .setTitle("Deletar")
            .setMessage("Deletar garçom? Essa ação não pode ser desfeita.")
            .setPositiveButton("Sim") { _, _ ->
                // Usando o FirebaseHelper para deletar o garçom
                firebaseHelper.deleteWaiter(waiterData.waiterId) { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(context, "Garçom deletado com sucesso.", Toast.LENGTH_SHORT).show()
                        getDataFromFirebase() // Atualiza a lista de garçons
                    } else {
                        Toast.makeText(context, "Erro ao deletar garçom.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    override fun onEditWaiterBtnClicked(waiterData: WaiterData) {
        if (popUpFragment != null) {
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
        }
        popUpFragment = AddWaiterPopupFragment.newInstance(waiterData.waiterId, waiterData.waiterEmail, waiterData.waiterName, "")
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddWaiterPopupFragment.TAG)
    }

    interface ApiService {
        @POST
        suspend fun deleteUser(@Url url: String, @Body userId: String): Response<Void>

        @POST
        suspend fun editUser(@Url url: String, @Body waiterJson: Map<String, String>): Response<Void>
    }

    private fun sendEditUserRequest(apiService: ApiService, waiterData: WaiterData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newWaiterData = mapOf("uid" to waiterData.waiterId, "password" to waiterData.waiterPassword)
                val response = apiService.editUser("edit_user", newWaiterData)
                if (response.isSuccessful) {
                    // Atualiza os dados do garçom no Firebase
                    val userReference = database.reference.child("Users")
                    val map = hashMapOf<String, Any>()
                    map[waiterData.waiterId] = waiterData
                    userReference.updateChildren(map)
                } else {
                    // Lidar com o erro
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    println(errorMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


