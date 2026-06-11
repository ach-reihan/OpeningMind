package com.openingmind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openingmind.domain.model.Repertoire

@Entity(tableName = "repertoires")
data class RepertoireEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ecoCode: String,
    val name: String,
    val notation: String,
    val description: String
) {
    fun toDomain(): Repertoire = Repertoire(id, ecoCode, name, notation, description)

    companion object {
        fun fromDomain(domain: Repertoire): RepertoireEntity =
            RepertoireEntity(domain.id, domain.ecoCode, domain.name, domain.notation, domain.description)
    }
}