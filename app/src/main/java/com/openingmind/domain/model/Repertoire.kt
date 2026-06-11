package com.openingmind.domain.model

data class Repertoire(
    val id: Int = 0,
    val ecoCode: String,
    val name: String,
    val notation: String,
    val description: String
)