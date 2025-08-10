package com.example.tcc3.models

data class SectionData (
    val sectionId: String,
    var sectionName: String,
    var tables: MutableList<Int> = mutableListOf()
)