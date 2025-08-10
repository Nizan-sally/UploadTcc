package com.example.tccgarcom.models

// Classe de dados do garçom (usuário)
// Armazena Id, email e uma lista de setores (em formato de SectionData)
data class WaiterData(
    val waiterId: String,
    val waiterEmail: String,
    var waiterName: String,
    var waiterSections: MutableList<SectionData> = mutableListOf()
)