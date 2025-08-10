package com.example.tccgarcom.models

// Classe de dados dos setores
// Id obrigatório ao criar instância, nome e mesas podem ser adicionados depois
data class SectionData(
    val sectionId: String,
    var sectionName: String = "",
    var sectionTables: MutableList<Int> = mutableListOf()
)

