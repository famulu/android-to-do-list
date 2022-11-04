package au.edu.swin.sdmd.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ToDoDetailViewModel(toDoId: Long) : ViewModel() {
    private val toDoRepository: ToDoRepository = ToDoRepository.get()
    private val _toDo: MutableStateFlow<ToDo?> = MutableStateFlow(null)
    val toDo: StateFlow<ToDo?> = _toDo.asStateFlow()

    init {
        viewModelScope.launch {
            _toDo.value = toDoRepository.loadById(toDoId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        toDo.value?.let { toDoRepository.updateToDo(it) }
    }

    fun updateToDo(onUpdate: (ToDo) -> ToDo) {
        _toDo.update { oldToDo ->
            oldToDo?.let {
                onUpdate(it)
            }
        }
    }
}

class ToDoDetailViewModelFactory(private val toDoId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ToDoDetailViewModel(toDoId) as T
    }
}