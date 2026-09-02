package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Study Subject and Topic Management in the Exam Preparation module.
 */
sealed interface StudySubjectUiState {
    data object Idle : StudySubjectUiState
    data object Loading : StudySubjectUiState
    data class Success(val message: String) : StudySubjectUiState
    data class Error(val errorMessage: String) : StudySubjectUiState
}

/**
 * ViewModel dedicated to managing Study Subjects and Topics for the Exam Preparation module.
 */
class StudySubjectViewModel(
    application: Application,
    private val repository: AppRepository = AppRepository(
        AppDatabase.getDatabase(application, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)).appDao()
    )
) : AndroidViewModel(application) {

    // UI State for operations
    private val _uiState = MutableStateFlow<StudySubjectUiState>(StudySubjectUiState.Idle)
    val uiState: StateFlow<StudySubjectUiState> = _uiState.asStateFlow()

    // Selected Subject ID for filtered views
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()

    // Selected Topic ID
    private val _selectedTopicId = MutableStateFlow<String?>(null)
    val selectedTopicId: StateFlow<String?> = _selectedTopicId.asStateFlow()

    // All Subjects Stream
    val allSubjects: StateFlow<List<StudySubject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Subjects Stream
    val activeSubjects: StateFlow<List<StudySubject>> = repository.activeSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Topics Stream
    val allTopics: StateFlow<List<StudyTopic>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Subject entity derived flow
    val selectedSubject: StateFlow<StudySubject?> = combine(allSubjects, _selectedSubjectId) { subjects, id ->
        if (id == null) subjects.firstOrNull() else subjects.find { it.subjectId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Topics for the currently selected subject
    val topicsForSelectedSubject: StateFlow<List<StudyTopic>> = combine(allTopics, _selectedSubjectId) { topics, subjectId ->
        if (subjectId == null) topics else topics.filter { it.subjectId == subjectId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Topics for the currently selected subject
    val activeTopicsForSelectedSubject: StateFlow<List<StudyTopic>> = combine(topicsForSelectedSubject, _selectedSubjectId) { topics, _ ->
        topics.filter { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently Selected Topic entity
    val selectedTopic: StateFlow<StudyTopic?> = combine(allTopics, _selectedTopicId) { topics, id ->
        topics.find { it.topicId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.ensureDataSeeded()
            // Set initial selected subject if available
            allSubjects.collectLatest { subjects ->
                if (_selectedSubjectId.value == null && subjects.isNotEmpty()) {
                    _selectedSubjectId.value = subjects.first().subjectId
                }
            }
        }
    }

    fun selectSubject(subjectId: String?) {
        _selectedSubjectId.value = subjectId
        _selectedTopicId.value = null // Reset topic on subject change
    }

    fun selectTopic(topicId: String?) {
        _selectedTopicId.value = topicId
    }

    fun clearUiState() {
        _uiState.value = StudySubjectUiState.Idle
    }

    // --- Subject Management ---

    fun addSubject(
        subjectId: String,
        name: String,
        icon: String = "📚",
        displayOrder: Int = 1,
        isActive: Boolean = true
    ) {
        val trimmedId = subjectId.trim().uppercase()
        val trimmedName = name.trim()

        if (trimmedId.isEmpty() || trimmedName.isEmpty()) {
            _uiState.value = StudySubjectUiState.Error("विषय ID और नाम दर्ज करना अनिवार्य है।")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                val newSubject = StudySubject(
                    subjectId = trimmedId,
                    name = trimmedName,
                    icon = icon.ifBlank { "📚" },
                    displayOrder = displayOrder,
                    isActive = isActive
                )
                repository.insertSubject(newSubject)
                _selectedSubjectId.value = trimmedId
                _uiState.value = StudySubjectUiState.Success("विषय सफलता से जोड़ा गया!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun updateSubject(subject: StudySubject) {
        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                repository.updateSubject(subject)
                _uiState.value = StudySubjectUiState.Success("विषय सफलतापूर्वक अपडेट हुआ!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun toggleSubjectActive(subject: StudySubject) {
        viewModelScope.launch {
            try {
                repository.updateSubject(subject.copy(isActive = !subject.isActive))
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun deleteSubject(subject: StudySubject) {
        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                repository.deleteSubject(subject)
                if (_selectedSubjectId.value == subject.subjectId) {
                    _selectedSubjectId.value = allSubjects.value.firstOrNull { it.subjectId != subject.subjectId }?.subjectId
                }
                _uiState.value = StudySubjectUiState.Success("विषय हटा दिया गया!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    // --- Topic Management ---

    fun addTopic(
        topicId: String,
        subjectId: String,
        topicName: String,
        description: String = "",
        displayOrder: Int = 1,
        isActive: Boolean = true
    ) {
        val trimmedTopicId = topicId.trim().uppercase()
        val trimmedSubjectId = subjectId.trim()
        val trimmedName = topicName.trim()

        if (trimmedTopicId.isEmpty() || trimmedSubjectId.isEmpty() || trimmedName.isEmpty()) {
            _uiState.value = StudySubjectUiState.Error("टॉपिक ID, विषय और टॉपिक नाम आवश्यक हैं।")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                val newTopic = StudyTopic(
                    topicId = trimmedTopicId,
                    subjectId = trimmedSubjectId,
                    topicName = trimmedName,
                    description = description.trim(),
                    displayOrder = displayOrder,
                    isActive = isActive
                )
                repository.insertTopic(newTopic)
                _selectedTopicId.value = trimmedTopicId
                _uiState.value = StudySubjectUiState.Success("टॉपिक सफलतापूर्वक जोड़ा गया!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun updateTopic(topic: StudyTopic) {
        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                repository.updateTopic(topic)
                _uiState.value = StudySubjectUiState.Success("टॉपिक सफलतापूर्वक अपडेट हुआ!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun toggleTopicActive(topic: StudyTopic) {
        viewModelScope.launch {
            try {
                repository.updateTopic(topic.copy(isActive = !topic.isActive))
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }

    fun deleteTopic(topic: StudyTopic) {
        viewModelScope.launch {
            try {
                _uiState.value = StudySubjectUiState.Loading
                repository.deleteTopic(topic)
                if (_selectedTopicId.value == topic.topicId) {
                    _selectedTopicId.value = null
                }
                _uiState.value = StudySubjectUiState.Success("टॉपिक हटा दिया गया!")
            } catch (e: Exception) {
                _uiState.value = StudySubjectUiState.Error("त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}")
            }
        }
    }
}
