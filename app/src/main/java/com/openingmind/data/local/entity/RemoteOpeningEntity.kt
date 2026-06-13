package com.openingmind.data.local.entity

import androidx.room.Entity
import com.openingmind.domain.model.Repertoire

@Entity(
    tableName = "remote_openings",
    primaryKeys = ["id", "language"]
)
data class RemoteOpeningEntity(
    val id: Int,
    val ecoCode: String,
    val name: String,
    val notation: String,
    val description: String,
    val language: String
) {
    fun toDomain(): Repertoire = Repertoire(id, ecoCode, name, notation, description)

    companion object {
        fun fromDomain(domain: Repertoire, language: String): RemoteOpeningEntity =
            RemoteOpeningEntity(
                id = domain.id,
                ecoCode = domain.ecoCode,
                name = domain.name,
                notation = domain.notation,
                description = domain.description,
                language = language
            )
    }
}
