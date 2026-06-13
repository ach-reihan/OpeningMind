package com.openingmind.domain.usecase

import com.openingmind.domain.repository.RepertoireRepository
import javax.inject.Inject

class GetAIChessAdviceUseCase @Inject constructor(
    private val repository: RepertoireRepository
) {
    suspend operator fun invoke(prompt: String, systemPrompt: String): String {
        return repository.getAIChessAdvice(prompt, systemPrompt)
    }
}