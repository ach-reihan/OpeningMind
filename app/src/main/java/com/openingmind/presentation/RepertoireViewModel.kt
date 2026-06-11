package com.openingmind.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openingmind.domain.model.Repertoire
import com.openingmind.domain.repository.RepertoireRepository
import com.openingmind.domain.usecase.GetAIChessAdviceUseCase
import com.openingmind.domain.usecase.GetLocalRepertoiresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepertoireViewModel @Inject constructor(
    private val getLocalRepertoiresUseCase: GetLocalRepertoiresUseCase,
    private val getAIChessAdviceUseCase: GetAIChessAdviceUseCase,
    private val repository: RepertoireRepository
) : ViewModel() {

    val localRepertoires: StateFlow<List<Repertoire>> = getLocalRepertoiresUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _remoteOpenings = MutableStateFlow<List<Repertoire>>(emptyList())
    val remoteOpenings: StateFlow<List<Repertoire>> = _remoteOpenings

    private val _isRemoteLoading = MutableStateFlow(false)
    val isRemoteLoading: StateFlow<Boolean> = _isRemoteLoading

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    val formEco = MutableStateFlow("")
    val formName = MutableStateFlow("")
    val formNotation = MutableStateFlow("")
    val formDescription = MutableStateFlow("")
    val editingId = MutableStateFlow<Int?>(null)

    init {
        fetchRemoteOpenings()
    }

    fun fetchRemoteOpenings() {
        viewModelScope.launch {
            _isRemoteLoading.value = true
            _remoteOpenings.value = repository.getRemoteOpenings()
            _isRemoteLoading.value = false
        }
    }

    fun startEditing(repertoire: Repertoire) {
        editingId.value = repertoire.id
        formEco.value = repertoire.ecoCode
        formName.value = repertoire.name
        formNotation.value = repertoire.notation
        formDescription.value = repertoire.description
    }

    fun clearForm() {
        editingId.value = null
        formEco.value = ""
        formName.value = ""
        formNotation.value = ""
        formDescription.value = ""
    }

    fun saveRepertoire() {
        viewModelScope.launch {
            val currentId = editingId.value
            val repertoire = Repertoire(
                id = currentId ?: 0,
                ecoCode = formEco.value,
                name = formName.value,
                notation = formNotation.value,
                description = formDescription.value
            )

            if (currentId == null) {
                repository.insertRepertoire(repertoire)
            } else {
                repository.updateRepertoire(repertoire)
            }
            clearForm()
        }
    }

    fun deleteRepertoire(repertoire: Repertoire) {
        viewModelScope.launch {
            repository.deleteRepertoire(repertoire)
        }
    }

    fun askAIChessAdvisor(prompt: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Grandmaster sedang berpikir..."
            val response = getAIChessAdviceUseCase(prompt)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }
}