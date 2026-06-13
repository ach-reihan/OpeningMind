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
    private val repository: RepertoireRepository,
    private val userPreferences: com.openingmind.data.local.UserPreferences
) : ViewModel() {

    val lastLocalRepertoire: StateFlow<String?> = userPreferences.lastLocalRepertoire
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastDictionaryOpening: StateFlow<String?> = userPreferences.lastDictionaryOpening
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastAiAdvice: StateFlow<String?> = userPreferences.lastAiAdvice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQueryKamus = MutableStateFlow("")
    val searchQueryKamus: StateFlow<String> = _searchQueryKamus

    private val _searchQueryRepertoar = MutableStateFlow("")
    val searchQueryRepertoar: StateFlow<String> = _searchQueryRepertoar

    val localRepertoires: StateFlow<List<Repertoire>> = kotlinx.coroutines.flow.combine(
        getLocalRepertoiresUseCase(),
        _searchQueryRepertoar
    ) { repertoires, query ->
        if (query.isBlank()) repertoires
        else repertoires.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.ecoCode.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allRemoteOpenings = MutableStateFlow<List<Repertoire>>(emptyList())
    val remoteOpenings: StateFlow<List<Repertoire>> = kotlinx.coroutines.flow.combine(
        _allRemoteOpenings,
        _searchQueryKamus
    ) { openings, query ->
        if (query.isBlank()) openings
        else openings.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.ecoCode.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRemoteLoading = MutableStateFlow(false)
    val isRemoteLoading: StateFlow<Boolean> = _isRemoteLoading

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    private val _selectedRemoteOpening = MutableStateFlow<Repertoire?>(null)
    val selectedRemoteOpening: StateFlow<Repertoire?> = _selectedRemoteOpening

    val formEco = MutableStateFlow("")
    val formName = MutableStateFlow("")
    val formNotation = MutableStateFlow("")
    val formDescription = MutableStateFlow("")
    val editingId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            userPreferences.language.collect { lang ->
                android.util.Log.d("RepertoireVM", "Language changed to: $lang, refreshing remote data")
                _isRemoteLoading.value = true
                try {
                    _allRemoteOpenings.value = repository.getRemoteOpenings(lang)
                } catch (e: Exception) {
                    android.util.Log.e("RepertoireVM", "Failed to load openings", e)
                } finally {
                    _isRemoteLoading.value = false
                }
            }
        }
    }

    fun fetchRemoteOpenings() {
        viewModelScope.launch {
            val lang = userPreferences.language.stateIn(viewModelScope, SharingStarted.Eagerly, "in").value
            _isRemoteLoading.value = true
            try {
                _allRemoteOpenings.value = repository.getRemoteOpenings(lang)
            } catch (e: Exception) {
                android.util.Log.e("RepertoireVM", "Failed to load openings", e)
            } finally {
                _isRemoteLoading.value = false
            }
        }
    }

    fun updateSearchQueryKamus(query: String) {
        _searchQueryKamus.value = query
    }

    fun updateSearchQueryRepertoar(query: String) {
        _searchQueryRepertoar.value = query
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

    fun askAIChessAdvisor(prompt: String, systemPrompt: String, thinkingMsg: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = thinkingMsg
            val response = getAIChessAdviceUseCase(prompt, systemPrompt)
            _aiResponse.value = response
            _isAiLoading.value = false
            userPreferences.saveLastAiAdvice(response)
        }
    }

    fun selectRemoteOpening(opening: Repertoire) {
        _selectedRemoteOpening.value = opening
        viewModelScope.launch {
            userPreferences.saveLastDictionaryOpening(opening.name)
        }
    }

    fun selectLocalRepertoire(repertoire: Repertoire) {
        viewModelScope.launch {
            userPreferences.saveLastLocalRepertoire(repertoire.name)
        }
    }

    fun clearSelectedRemoteOpening() {
        _selectedRemoteOpening.value = null
    }
}