package com.example.tcc3.models

/*
Definindo classe de dados dos Garçons (Waiters)
* waiterId = Id do garçom -> Administrado pelo Firebase
* waiterEmail = Email do garçom
* waiterPassword = Senha do garçom
*/
data class WaiterData(
    val waiterId: String,
    var waiterEmail: String,
    var waiterName: String,
    var waiterPassword: String,
    var waiterSections: MutableList<SectionData> = mutableListOf()
)
