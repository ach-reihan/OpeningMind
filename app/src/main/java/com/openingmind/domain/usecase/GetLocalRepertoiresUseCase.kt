package com.openingmind.domain.usecase

import com.openingmind.domain.model.Repertoire
import com.openingmind.domain.repository.RepertoireRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalRepertoiresUseCase @Inject constructor(
    private val repository: RepertoireRepository
) {
    operator fun invoke(): Flow<List<Repertoire>> {
        return repository.getLocalRepertoires()
    }
}