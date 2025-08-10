package com.example.tcc3.views.popups

import com.example.tcc3.adapters.TableAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tcc3.databinding.FragmentAddSectionPopupBinding
import com.example.tcc3.models.SectionData
import com.example.tcc3.models.TableData
import com.example.tcc3.utils.InputManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddSectionPopupFragment (private var totalTables: Int) : DialogFragment() {

    private lateinit var binding: FragmentAddSectionPopupBinding // Elemento de conexão com a tela (xml)

    private lateinit var listener: ConfirmSectionChangeBtnClickListener // Ouvinte

    private var sectionData: SectionData? = null
    private var selectedTables: MutableList<Int> = mutableListOf()

    fun setListener(listener: ConfirmSectionChangeBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "AddSectionPopupFragment"

        @JvmStatic
        fun newInstance(sectionData: SectionData, totalTables: Int) = AddSectionPopupFragment(totalTables).apply {
            arguments = Bundle().apply {
                putString("sectionId", sectionData.sectionId)
                putString("sectionName", sectionData.sectionName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddSectionPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataToEdit()
        addSectionEvents()
        tableEvents()
    }

    private fun getDataToEdit() {
        if (arguments != null) {
            val sectionId = arguments?.getString("sectionId").toString()
            val sectionName = arguments?.getString("sectionName").toString()

            FirebaseDatabase.getInstance().reference.child("Sections").child(sectionId).child("tables").addValueEventListener( object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (table in snapshot.children) {
                        selectedTables.add(table.value.toString().toInt())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            sectionData = SectionData (
                sectionId,
                sectionName,
                selectedTables
            )

            binding.txtInputSectionName.setText(sectionData?.sectionName)
        }
    }

    private fun tableEvents() {
        val tablesRecyclerView = binding.recyclerViewMesas
        tablesRecyclerView.layoutManager = GridLayoutManager(this.context,4)

        val tables = (1..totalTables).map { TableData(it) }
        val adapter = TableAdapter(tables, selectedTables) { position, isChecked -> }

        tablesRecyclerView.adapter = adapter
    }

    private fun addSectionEvents() {
        binding.btnConfirmSectionChange.setOnClickListener {
            val sectionName = binding.txtInputSectionName.text.toString().trim()

            if (!InputManager(requireContext()).hasEmptyData(listOf(sectionName))) {
                if (sectionData == null) {
                    listener.onSaveSection(sectionName, binding.txtInputSectionName, selectedTables)
                } else {
                    sectionData?.sectionName = sectionName
                    listener.onUpdateSection(sectionData!!, binding.txtInputSectionName, selectedTables)
                }
            }
        }
    }

    interface ConfirmSectionChangeBtnClickListener{
        fun onSaveSection(sectionName: String, txtSectionName: TextInputEditText, selectedTables: MutableList<Int>)
        fun onUpdateSection(sectionData: SectionData, txtSectionName: TextInputEditText, selectedTables: MutableList<Int>)
    }
}