package com.example.tcc3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tcc3.databinding.FragmentTableItemBinding
import com.example.tcc3.models.TableData

class TableAdapter (private val tablesList: List<TableData>, private var selectedTables: MutableList<Int>, private var onCheckedChangeListener: (Int, Boolean) -> Unit) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    inner class TableViewHolder(val binding: FragmentTableItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = FragmentTableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val tablesListItem = tablesList[position]
        if (tablesListItem.tableId in selectedTables) {
            tablesListItem.isChecked = true
        }
        with(holder) {
            with(binding) {
                with(checkBoxTable) {
                    text = tablesListItem.tableId.toString()
                    isChecked = tablesListItem.isChecked
                    setOnCheckedChangeListener { table, isChecked ->
                        table.isChecked = isChecked
                        if (checkBoxTable.text.toString().toInt() !in selectedTables && isChecked) {
                            tablesListItem.isChecked = true
                            selectedTables.add(checkBoxTable.text.toString().toInt())
                        }
                        if (checkBoxTable.text.toString().toInt() in selectedTables && !isChecked) {
                            tablesListItem.isChecked = false
                            selectedTables.remove(checkBoxTable.text.toString().toInt())
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tablesList.size
    }

}