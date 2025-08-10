package com.example.tcc3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.databinding.FragmentSectionItemBinding
import com.example.tcc3.models.SectionData

class SectionAdapter (private val list: MutableList<SectionData>): RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    inner class SectionViewHolder(val binding: FragmentSectionItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var listener: SectionAdapterClicksInterface? = null
    fun setListener(listener: SectionAdapterClicksInterface) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = FragmentSectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        with(holder) {
            with (list[position]) {
                binding.txtSectionName.text = this.sectionName

                binding.btnDeleteSection.setOnClickListener {
                    listener?.onDeleteSectionBtnClicked(this)
                }

                binding.btnEditSection.setOnClickListener {
                    listener?.onEditSectionBtnClicked(this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface SectionAdapterClicksInterface {
        fun onDeleteSectionBtnClicked(sectionData: SectionData)
        fun onEditSectionBtnClicked(sectionData: SectionData)
    }
}